/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.client.GrinderMissingScreen;
import be.thefricadelle.belgiansnacks.command.BSCommands;
import be.thefricadelle.belgiansnacks.compat.jei.GrindingGoalCategory;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.network.GrinderMissingRequestPayload;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * M6.5 in the real Arcadia pack (tools/arcadia_smoke.py). Client A creates a world and checks
 * recipes, KubeJS edits, recipe conflicts, Create Heat JS, the grinder and JEI, then opens the world
 * to LAN; client B joins and both feed the same grinder. Each writes arcadia-smoke-report.txt.
 * Inert unless {@code -Dcreate_belgian_snacks.arcadiaSmoke=A} or {@code =B}.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class ArcadiaSmokeRun {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ROLE = System.getProperty("create_belgian_snacks.arcadiaSmoke", "");
    private static final boolean ENABLED = ROLE.equals("A") || ROLE.equals("B");
    static final String WORLD = "belgian-snacks-arcadia";
    private static final int LAN_PORT = 25611;
    private static final int TIMEOUT_TICKS = 20 * 60 * 20;
    private static final int STEP_TIMEOUT = 20 * 120;
    // Waiting on the other client, which loads the whole pack first.
    private static final int OTHER_CLIENT_TIMEOUT = 20 * 60 * 15;
    private static final String KUBEJS_RECIPE = "kubejs_test/frying_potato";

    // Present in Arcadia 2.0.32 whatever else the pack does (docs/11).
    private static final Set<String> ALWAYS_LOADED = Set.of(
        "crushing/porkchop", "crushing/beef", "crushing/chicken", "crushing/bread", "milling/dried_kelp",
        "mixing/fricadelle_paste", "mixing/melted_beef_tallow", "mixing/mayonnaise", "pressing/fricadelle_paste",
        "mechanical_crafting/fryer", "mechanical_crafting/supreme_grinder", KUBEJS_RECIPE,
        "frying/the_fricadelle", "sequenced_assembly/raw_the_fricadelle");
    // Removed by the KubeJS test script; disabled because Create Crafts & Additions makes seed oil;
    // the beetroot fallback of tier 2, since Farmer's Delight brings onions.
    private static final Set<String> NEVER_LOADED = Set.of("frying/fricadelle", "compacting/frying_oil_from_seeds",
        "sequenced_assembly/raw_the_fricadelle_from_beetroot");
    // Vanilla crafting-style types: the ones Polymorph arbitrates.
    private static final Set<RecipeType<?>> POLYMORPH_TYPES = Set.of(RecipeType.CRAFTING, RecipeType.SMELTING,
        RecipeType.BLASTING, RecipeType.SMOKING, RecipeType.CAMPFIRE_COOKING, RecipeType.STONECUTTING, RecipeType.SMITHING);

    private static final List<String> REPORT = Collections.synchronizedList(new ArrayList<>());
    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static final AtomicInteger PENDING = new AtomicInteger();
    private static boolean planned;
    private static boolean finished;
    private static boolean joined;
    private static int ticks;
    private static int wait;
    private static int waited;
    private static final AtomicInteger FAILURES = new AtomicInteger();

    private static BlockPos fryerPos;
    private static BlockPos hopperGrinderPos;
    private static BlockPos sharedGrinderPos;
    private static List<ResourceLocation> hopperFoods = List.of();

    private ArcadiaSmokeRun() {
    }

    // A step not ready within timeoutTicks fails with what diagnostic says, and the run goes on.
    private record Step(String name, int delayTicks, BooleanSupplier ready, Runnable action, int timeoutTicks,
                        java.util.function.Supplier<String> diagnostic) {
    }

    @FunctionalInterface
    private interface Check {
        String run() throws Exception;
    }

    // ------------------------------------------------------------------ runner

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || finished) {
            return;
        }
        if (!planned) {
            if (ROLE.equals("A")) {
                planHost();
            } else {
                planGuest();
            }
            planned = true;
        }
        ticks++;
        Minecraft mc = Minecraft.getInstance();
        if (ticks > TIMEOUT_TICKS) {
            Step stuck = STEPS.peek();
            fail("runner.timeout", "stuck waiting on " + (stuck == null ? "nothing" : stuck.name()));
            finish();
            return;
        }
        if (joined && (mc.getConnection() == null || mc.level == null || mc.player == null)) {
            Step stuck = STEPS.peek();
            fail("runner.disconnected", "connection lost while waiting on " + (stuck == null ? "nothing" : stuck.name()));
            finish();
            return;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.peek();
        if (step == null) {
            finish();
            return;
        }
        if (PENDING.get() > 0 || !step.ready().getAsBoolean()) {
            if (PENDING.get() == 0 && ++waited > step.timeoutTicks()) {
                STEPS.poll();
                waited = 0;
                String why;
                try {
                    why = step.diagnostic().get();
                } catch (Throwable t) {
                    why = "diagnostic threw " + t;
                }
                fail(step.name(), "not ready after " + step.timeoutTicks() / 20 + " s: " + why);
            }
            return;
        }
        waited = 0;
        STEPS.poll();
        LOGGER.info("[arcadia-smoke {}] {}", ROLE, step.name());
        try {
            step.action().run();
        } catch (Throwable t) {
            LOGGER.error("[arcadia-smoke {}] step {} threw", ROLE, step.name(), t);
            fail(step.name(), "threw " + t);
        }
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delayTicks();
    }

    // The integrated server of A hands each player the food they will feed the shared grinder.
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ROLE.equals("A") || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean guest = player.getName().getString().endsWith("B");
        ResourceLocation food = guest ? guestFood() : ResourceLocation.parse("minecraft:bread");
        // The last hotbar slot, the one a starter kit is least likely to fill; the client selects it.
        player.getInventory().setItem(8, new ItemStack(BuiltInRegistries.ITEM.get(food)));
        // A LAN join lands anywhere within the spawn radius: put the guest beside the host, near the machines.
        if (guest) {
            player.getServer().getPlayerList().getPlayers().stream().filter(other -> other != player).findFirst()
                .ifPresent(host -> player.teleportTo(host.getX() + 1, host.getY(), host.getZ() + 1));
        }
    }

    private static ResourceLocation guestFood() {
        return guestFoodOn(FoodIndex.server());
    }

    // Same answer on the host's server and on the guest's client: both hold the same sorted index.
    private static ResourceLocation guestFoodOn(FoodIndex.Snapshot index) {
        return index.ids().stream().filter(id -> id.getNamespace().equals("farmersdelight")).findFirst()
            .orElse(ResourceLocation.parse("minecraft:carrot"));
    }

    // ------------------------------------------------------------------ host (A)

    private static void planHost() {
        Minecraft mc = Minecraft.getInstance();
        step("title", 0, () -> mc.getOverlay() == null && mc.screen != null && ticks > 40, ArcadiaSmokeRun::createWorld);
        step("world", 60, () -> mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null
            && FoodIndex.server().size() > 0, () -> {
            joined = true;
            onServer(ArcadiaSmokeRun::serverChecks);
        });
        step("place", 5, () -> true, () -> {
            BlockPos base = mc.player.blockPosition();
            fryerPos = base.east(3);
            hopperGrinderPos = base.west(3);
            sharedGrinderPos = base.north(3);
            onServer(ArcadiaSmokeRun::placeMachines);
        });
        step("kubejs.fried", 0, () -> fried() >= 4,
            () -> pass("kubejs.fryingWorks", "a real fryer fried 4 potatoes with the recipe KubeJS added"),
            STEP_TIMEOUT, ArcadiaSmokeRun::fryerState);
        step("grinder.hopper", 0, () -> grinderAt(hopperGrinderPos) != null && grinderAt(hopperGrinderPos).getCount() >= hopperFoods.size(),
            () -> pass("grinder.packFoods", "a hopper fed " + hopperFoods + " from the pack's mods"));
        step("grinder.fill", 5, () -> true, () -> onServer(server -> {
            SupremeGrinderBlockEntity grinder = (SupremeGrinderBlockEntity) server.overworld().getBlockEntity(hopperGrinderPos);
            List<ResourceLocation> ids = FoodIndex.server().ids();
            grinder.setConsumed(ids.subList(0, GrinderProgress.goal(BSConfig.grinderTheFricadelleRatio(), ids.size())));
        }));
        step("grinder.paste", 0, () -> grinderAt(hopperGrinderPos) != null
            && grinderAt(hopperGrinderPos).getOutput().getStackInSlot(0).is(BSItems.EXCEPTIONAL_PASTE.get()), () -> {
            SupremeGrinderBlockEntity grinder = grinderAt(hopperGrinderPos);
            pass("grinder.goal", "Exceptional Paste at " + grinder.getGoal() + " of " + grinder.getTotal() + " foods");
            // Out of the way before B joins: one grinder left, the shared one.
            onServer(server -> {
                server.overworld().setBlockAndUpdate(hopperGrinderPos.above(), Blocks.AIR.defaultBlockState());
                server.overworld().setBlockAndUpdate(hopperGrinderPos.east(), Blocks.AIR.defaultBlockState());
                server.overworld().setBlockAndUpdate(hopperGrinderPos, Blocks.AIR.defaultBlockState());
            });
        });
        step("shot.world", 20, () -> true, () -> {
            mc.player.setYRot(180f);
            mc.player.setXRot(35f);
            screenshot("arcadia-machines");
        });
        step("missing.ask", 10, () -> true, () -> PacketDistributor.sendToServer(new GrinderMissingRequestPayload(sharedGrinderPos)));
        step("missing.check", 0, () -> mc.screen instanceof GrinderMissingScreen, () -> check("grinder.missingScreen", () -> {
            GrinderMissingScreen screen = (GrinderMissingScreen) mc.screen;
            int total = FoodIndex.client().size();
            require(screen.missingCount() == total, "lists " + screen.missingCount() + " of " + total);
            require(screen.drawnPerFrame() < total, "draws all " + total + " icons every frame");
            return total + " missing foods listed, " + screen.drawnPerFrame() + " icons drawn per frame";
        }));
        step("missing.shot", 20, () -> true, () -> screenshot("arcadia-missing"));
        step("missing.close", 5, () -> true, () -> mc.setScreen(null));
        step("jei", 20, () -> SmokeJeiPlugin.runtime != null, ArcadiaSmokeRun::checkJei);
        step("jei.frying", 10, () -> true, () -> showOutput(new ItemStack(Items.BAKED_POTATO)));
        step("jei.frying.shot", 30, () -> true, () -> screenshot("arcadia-jei-baked-potato"));
        step("jei.grinding", 10, () -> true, () -> SmokeJeiPlugin.runtime.getRecipesGui().showTypes(List.of(GrindingGoalCategory.TYPE)));
        step("jei.grinding.shot", 30, () -> true, () -> screenshot("arcadia-jei-grinding"));
        step("jei.close", 5, () -> true, () -> mc.setScreen(null));
        step("export", 5, () -> true, () -> onServer(server -> check("foods.export", () -> {
            Path csv = FMLPaths.GAMEDIR.get().resolve("arcadia-foods.csv");
            int written = BSCommands.exportTo(server, csv);
            ArcadiaExportRun.writeSummary(csv);
            return written + " foods exported";
        })));
        step("lan.open", 5, () -> true, ArcadiaSmokeRun::openToLan);
        step("lan.guest", 0, () -> mc.getConnection().getOnlinePlayers().size() >= 2 && mc.level.players().size() >= 2,
            () -> pass("lan.twoPlayers", mc.level.players().size() + " players in view, "
                + mc.getConnection().getOnlinePlayers().size() + " in the tab list"),
            OTHER_CLIENT_TIMEOUT, () -> mc.getConnection().getOnlinePlayers().size() + " players online");
        feedSharedGrinder(mc);
        // The guest leaves only once its own checks are done: the host keeps the world up until then.
        step("guest.left", 0, () -> mc.getConnection().getOnlinePlayers().size() == 1,
            () -> pass("lan.guestLeft", "the guest finished and left cleanly"), OTHER_CLIENT_TIMEOUT,
            () -> mc.getConnection().getOnlinePlayers().size() + " players still online");
    }

    private static void createWorld() {
        Minecraft mc = Minecraft.getInstance();
        LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
            new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
    }

    // Server thread: everything that reads the recipe manager, tags and Create's enums.
    private static void serverChecks(MinecraftServer server) {
        List<RecipeHolder<?>> ours = server.getRecipeManager().getRecipes().stream()
            .filter(holder -> holder.id().getNamespace().equals(BelgianSnacks.MOD_ID)).toList();
        Set<String> loaded = new TreeSet<>();
        ours.forEach(holder -> loaded.add(holder.id().getPath()));

        check("recipes.loaded", () -> {
            Set<String> missing = new TreeSet<>(ALWAYS_LOADED);
            missing.removeAll(loaded);
            require(missing.isEmpty(), "missing in the pack: " + missing);
            Set<String> unexpected = new TreeSet<>(NEVER_LOADED);
            unexpected.retainAll(loaded);
            require(unexpected.isEmpty(), "loaded although removed or disabled: " + unexpected);
            // Exactly one curry ketchup: tomato when the pack has tomatoes, beetroot otherwise.
            boolean tomato = loaded.contains("mixing/curry_ketchup");
            require(tomato != loaded.contains("mixing/curry_ketchup_from_beetroot"), "curry ketchup recipes: " + loaded);
            return loaded.size() + " recipes: " + loaded;
        });
        check("kubejs.recipe", () -> {
            RecipeHolder<?> potato = server.getRecipeManager().byKey(BelgianSnacks.asResource(KUBEJS_RECIPE)).orElse(null);
            require(potato != null && potato.value() instanceof be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe,
                "KubeJS frying recipe not a frying recipe: " + potato);
            var frying = (be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe) potato.value();
            require(frying.getIngredients().get(0).test(new ItemStack(Items.POTATO)), "does not take a potato");
            require(frying.getRollableResults().get(0).getStack().is(Items.BAKED_POTATO), "does not give a baked potato");
            require(frying.getFat() != null && frying.getFat().amount() == 10, "fat: " + frying.getFat());
            return "frying/fricadelle removed, " + KUBEJS_RECIPE + " added with 10 mB of fat";
        });
        check("kubejs.tag", () -> {
            require(!FoodIndex.server().contains(ResourceLocation.parse("minecraft:apple")), "the apple is still a food");
            require(FoodIndex.server().contains(ResourceLocation.parse("minecraft:bread")), "the bread left too");
            return "the apple added to grinder/blacklist left the food index (" + FoodIndex.server().size() + " foods)";
        });
        check("recipes.polymorph", () -> {
            List<String> vanillaTyped = ours.stream().filter(holder -> POLYMORPH_TYPES.contains(holder.value().getType()))
                .map(holder -> holder.id().toString()).toList();
            require(vanillaTyped.isEmpty(), "recipes Polymorph would arbitrate: " + vanillaTyped);
            return "none of our " + ours.size() + " recipes is of a crafting table, furnace or stonecutter type";
        });
        check("recipes.conflicts", () -> {
            List<String> conflicts = new ArrayList<>();
            for (RecipeHolder<?> holder : ours) {
                for (RecipeHolder<?> other : server.getRecipeManager().getRecipes()) {
                    if (other.id().getNamespace().equals(BelgianSnacks.MOD_ID) || !conflict(holder.value(), other.value(), server)) {
                        continue;
                    }
                    conflicts.add(holder.id().getPath() + " vs " + other.id());
                }
            }
            require(conflicts.isEmpty(), "same input, other result: " + conflicts);
            return "no recipe of the pack takes the same input as one of ours";
        });
        check("heat.createHeatJs", () -> {
            List<String> levels = new ArrayList<>();
            for (HeatLevel heat : HeatLevel.values()) {
                String key = FryerBlockEntity.heatKey(heat);
                levels.add(heat.name() + "=" + key.substring(key.lastIndexOf('.') + 1));
                for (HeatCondition condition : HeatCondition.values()) {
                    condition.testBlazeBurner(heat);
                    condition.visualizeAsBlazeBurner();
                }
            }
            return HeatLevel.values().length + " heat levels, " + HeatCondition.values().length + " conditions, all handled: " + levels;
        });
        // Create's own answers for its three conditions and five levels: the fryer must give them even
        // where the pack's testBlazeBurner (rewritten by Create Heat JS) does not.
        check("heat.fryerRules", () -> {
            List<String> packDiffers = new ArrayList<>();
            List<String> fryerDiffers = new ArrayList<>();
            for (HeatLevel heat : List.of(HeatLevel.NONE, HeatLevel.SMOULDERING, HeatLevel.FADING, HeatLevel.KINDLED, HeatLevel.SEETHING)) {
                boolean hot = heat != HeatLevel.NONE && heat != HeatLevel.SMOULDERING;
                boolean[] create = {true, hot, heat == HeatLevel.SEETHING};
                HeatCondition[] conditions = {HeatCondition.NONE, HeatCondition.HEATED, HeatCondition.SUPERHEATED};
                for (int i = 0; i < 3; i++) {
                    if (conditions[i].testBlazeBurner(heat) != create[i]) {
                        packDiffers.add(conditions[i] + " on " + heat);
                    }
                    if (FryerBlockEntity.heatAllows(conditions[i], heat) != create[i]) {
                        fryerDiffers.add(conditions[i] + " on " + heat);
                    }
                }
            }
            require(fryerDiffers.isEmpty(), "the fryer differs from Create: " + fryerDiffers);
            return "the fryer follows Create's rules; the pack's testBlazeBurner differs on " + packDiffers;
        });
        check("foods.index", () -> {
            FoodIndex.Snapshot index = FoodIndex.server();
            int goal = GrinderProgress.goal(BSConfig.grinderTheFricadelleRatio(), index.size());
            return index.size() + " foods, THE_ goal " + goal + ", fingerprint " + index.fingerprint();
        });
    }

    // Two recipes of the same type that both accept our inputs: the machine could pick either.
    private static boolean conflict(Recipe<?> ours, Recipe<?> other, MinecraftServer server) {
        if (ours.getType() != other.getType() && !(ours instanceof ShapedRecipe && other instanceof CraftingRecipe)) {
            return false;
        }
        if (ours instanceof ShapedRecipe shaped) {
            List<ItemStack> grid = new ArrayList<>();
            for (Ingredient ingredient : shaped.getIngredients()) {
                grid.add(ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0]);
            }
            CraftingInput input = CraftingInput.of(shaped.getWidth(), shaped.getHeight(), grid);
            return matches(other, input, server);
        }
        if (ours instanceof ProcessingRecipe<?, ?> mine && other instanceof ProcessingRecipe<?, ?> theirs) {
            if (mine.getIngredients().size() != theirs.getIngredients().size()
                || mine.getFluidIngredients().size() != theirs.getFluidIngredients().size()) {
                return false;
            }
            return covers(mine.getIngredients(), theirs.getIngredients()) && covers(theirs.getIngredients(), mine.getIngredients());
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean matches(Recipe other, CraftingInput input, MinecraftServer server) {
        try {
            return other instanceof CraftingRecipe && other.matches(input, server.overworld());
        } catch (RuntimeException e) {
            return false;
        }
    }

    // Every ingredient of a has one in b that accepts its first item.
    private static boolean covers(NonNullList<Ingredient> a, NonNullList<Ingredient> b) {
        for (Ingredient ingredient : a) {
            ItemStack[] items = ingredient.getItems();
            if (items.length == 0) {
                return false;
            }
            if (b.stream().noneMatch(candidate -> candidate.test(items[0]))) {
                return false;
            }
        }
        return true;
    }

    // Server thread. A fryer on a never-fading burner with oil and 4 potatoes (the KubeJS recipe), a
    // grinder fed by a hopper holding three foods from the pack's mods, and the grinder both players share.
    private static void placeMachines(MinecraftServer server) {
        var level = server.overworld();
        level.setBlockAndUpdate(fryerPos.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
            .setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED));
        if (level.getBlockEntity(fryerPos.below()) instanceof BlazeBurnerBlockEntity burner) {
            burner.isCreative = true;
        }
        level.setBlockAndUpdate(fryerPos, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = (FryerBlockEntity) level.getBlockEntity(fryerPos);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 2000), IFluidHandler.FluidAction.EXECUTE);
        fryer.getItemCapability().insertItem(0, new ItemStack(Items.POTATO, 4), false);

        List<ResourceLocation> picked = new ArrayList<>();
        for (String mod : List.of("createfood", "farmersdelight", "culturaldelights")) {
            FoodIndex.server().ids().stream().filter(id -> id.getNamespace().equals(mod)).findFirst().ifPresent(picked::add);
        }
        hopperFoods = List.copyOf(picked);
        for (BlockPos grinderPos : List.of(hopperGrinderPos, sharedGrinderPos)) {
            level.setBlockAndUpdate(grinderPos, BSBlocks.SUPREME_GRINDER.getDefaultState());
            level.setBlockAndUpdate(grinderPos.above(), AllBlocks.CREATIVE_MOTOR.getDefaultState()
                .setValue(BlockStateProperties.FACING, Direction.DOWN));
            ((CreativeMotorBlockEntity) level.getBlockEntity(grinderPos.above())).generatedSpeed.setValue(64);
        }
        BlockPos hopperPos = hopperGrinderPos.east();
        level.setBlockAndUpdate(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.WEST));
        HopperBlockEntity hopper = (HopperBlockEntity) level.getBlockEntity(hopperPos);
        for (int i = 0; i < picked.size(); i++) {
            hopper.setItem(i, new ItemStack(BuiltInRegistries.ITEM.get(picked.get(i))));
        }
    }

    // What the fryer is waiting for, as the client sees it.
    private static String fryerState() {
        Minecraft mc = Minecraft.getInstance();
        if (fryerPos == null || !(mc.level.getBlockEntity(fryerPos) instanceof FryerBlockEntity fryer)) {
            return "no fryer at " + fryerPos + ", block " + (fryerPos == null ? "?" : mc.level.getBlockState(fryerPos));
        }
        var recipe = mc.level.getRecipeManager().byKey(BelgianSnacks.asResource(KUBEJS_RECIPE))
            .map(holder -> (be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe) holder.value()).orElse(null);
        String heatRule = recipe == null ? "no KubeJS recipe on the client"
            : "recipe needs " + recipe.getRequiredHeat() + ", testBlazeBurner(" + fryer.heatBelow() + ") = "
                + recipe.getRequiredHeat().testBlazeBurner(fryer.heatBelow());
        return heatRule + "; status " + fryer.status() + ", heat " + fryer.heatBelow() + " from " + mc.level.getBlockState(fryerPos.below())
            + ", input " + fryer.getInput().getStackInSlot(0) + ", basket " + fryer.getBasket()
            + ", fat " + fryer.getTank().getPrimaryHandler().getFluid().getAmount() + " mB "
            + BuiltInRegistries.FLUID.getKey(fryer.getTank().getPrimaryHandler().getFluid().getFluid())
            + ", output " + fryer.getOutput().getStackInSlot(0) + " / " + fryer.getOutput().getStackInSlot(1);
    }

    private static int fried() {
        Minecraft mc = Minecraft.getInstance();
        if (fryerPos == null || !(mc.level.getBlockEntity(fryerPos) instanceof FryerBlockEntity fryer)) {
            return 0;
        }
        int total = 0;
        for (int slot = 0; slot < fryer.getOutput().getSlots(); slot++) {
            if (fryer.getOutput().getStackInSlot(slot).is(Items.BAKED_POTATO)) {
                total += fryer.getOutput().getStackInSlot(slot).getCount();
            }
        }
        return total;
    }

    private static void checkJei() {
        IJeiRuntime jei = SmokeJeiPlugin.runtime;
        check("jei.categories", () -> {
            Set<String> ours = new TreeSet<>();
            jei.getRecipeManager().createRecipeCategoryLookup().get().forEach(category -> {
                if (category.getRecipeType().getUid().getNamespace().equals(BelgianSnacks.MOD_ID)) {
                    ours.add(category.getRecipeType().getUid().getPath());
                }
            });
            require(ours.containsAll(Set.of("frying", "grinding_goal")), "our JEI categories: " + ours);
            return "categories " + ours;
        });
        check("jei.kubejsFrying", () -> {
            Set<String> shown = new TreeSet<>();
            jei.getRecipeManager().createRecipeCategoryLookup().get().forEach(category ->
                jei.getRecipeManager().createRecipeLookup(category.getRecipeType()).get().forEach(recipe -> {
                    if (recipe instanceof RecipeHolder<?> holder && holder.id().getNamespace().equals(BelgianSnacks.MOD_ID)) {
                        shown.add(holder.id().getPath());
                    }
                }));
            require(shown.contains(KUBEJS_RECIPE), "the KubeJS recipe is not in JEI: " + shown);
            require(!shown.contains("frying/fricadelle"), "JEI still shows the removed recipe");
            return shown.size() + " of our recipes shown, the KubeJS one included";
        });
        check("jei.transitionItem", () -> {
            boolean listed = jei.getIngredientManager().getAllItemStacks().stream()
                .anyMatch(stack -> stack.is(BSItems.INCOMPLETE_THE_FRICADELLE.get()));
            return "incomplete_the_fricadelle " + (listed ? "listed" : "hidden") + " in the JEI item list";
        });
    }

    private static void showOutput(ItemStack stack) {
        IJeiRuntime jei = SmokeJeiPlugin.runtime;
        jei.getRecipesGui().show(jei.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
    }

    // Offline dev accounts cannot be verified by Mojang: the LAN server must not ask.
    private static void openToLan() {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        server.setUsesAuthentication(false);
        boolean open = server.publishServer(GameType.CREATIVE, true, LAN_PORT);
        check("lan.open", () -> {
            require(open, "could not open port " + LAN_PORT);
            Files.writeString(FMLPaths.GAMEDIR.get().resolve("arcadia-lan-open.txt"), Integer.toString(LAN_PORT), StandardCharsets.UTF_8);
            return "world open to LAN on port " + LAN_PORT;
        });
    }

    // ------------------------------------------------------------------ guest (B)

    private static void planGuest() {
        Minecraft mc = Minecraft.getInstance();
        step("joined", 0, () -> mc.player != null && mc.level != null && mc.screen == null && mc.getConnection() != null, () -> {
            joined = true;
            pass("lan.joined", "joined the LAN world as " + mc.player.getName().getString());
        }, OTHER_CLIENT_TIMEOUT, () -> "screen " + (mc.screen == null ? "none" : mc.screen.getClass().getSimpleName()));
        step("foodIndex", 20, () -> FoodIndex.client().size() > 0, () -> pass("foods.index",
            FoodIndex.client().size() + " foods, fingerprint " + FoodIndex.client().fingerprint()));
        step("fryer", 0, () -> findFryer() != null, () -> {
            fryerPos = findFryer();
            FryerBlockEntity fryer = (FryerBlockEntity) mc.level.getBlockEntity(fryerPos);
            require(fryer.getTank().getPrimaryHandler().getFluidAmount() > 0, "no fat seen");
            pass("lan.fryerSynced", "fryer at " + fryerPos.toShortString() + " with " + fryer.getTank().getPrimaryHandler().getFluidAmount()
                + " mB and " + fried() + " baked potatoes");
        });
        step("grinder", 0, () -> findGrinder() != null, () -> sharedGrinderPos = findGrinder());
        feedSharedGrinder(mc);
        step("missing.ask", 5, () -> true, () -> PacketDistributor.sendToServer(new GrinderMissingRequestPayload(sharedGrinderPos)));
        step("missing.check", 0, () -> mc.screen instanceof GrinderMissingScreen, () -> check("lan.missingScreen", () -> {
            int expected = FoodIndex.client().size() - 2;
            int listed = ((GrinderMissingScreen) mc.screen).missingCount();
            require(listed == expected, "lists " + listed + ", expected " + expected);
            return listed + " missing foods sent by the host";
        }));
        step("missing.shot", 20, () -> true, () -> screenshot("arcadia-b-missing"));
        step("missing.close", 5, () -> true, () -> mc.setScreen(null));
    }

    // Both players: feed the shared grinder the food the host gave, then wait until it counts both.
    private static void feedSharedGrinder(Minecraft mc) {
        // The pack hands out a starter kit on join, which can take the hand: select the food's slot.
        step("grinder.pick", 5, () -> foodSlot() >= 0, () -> {
            int slot = foodSlot();
            mc.player.getInventory().selected = slot;
            mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(slot));
        }, STEP_TIMEOUT, () -> "hotbar " + java.util.stream.IntStream.range(0, 9)
            .mapToObj(i -> mc.player.getInventory().getItem(i).toString()).toList());
        step("grinder.feed", 10, () -> true, () -> {
            ItemStack held = mc.player.getMainHandItem();
            require(FoodIndex.client().contains(BuiltInRegistries.ITEM.getKey(held.getItem())), "no food in hand: " + held);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(sharedGrinderPos), Direction.UP, sharedGrinderPos, false));
        });
        step("grinder.shared", 0, () -> grinderAt(sharedGrinderPos) != null && grinderAt(sharedGrinderPos).getCount() == 2,
            () -> pass("lan.grinderShared", "both players' foods counted here: 2 / " + grinderAt(sharedGrinderPos).getGoal()
                + " of " + grinderAt(sharedGrinderPos).getTotal()));
    }

    // Hotbar slot holding the food the host gave this player, or -1.
    private static int foodSlot() {
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation wanted = ROLE.equals("B") ? guestFoodOn(FoodIndex.client()) : ResourceLocation.parse("minecraft:bread");
        for (int slot = 0; slot < 9; slot++) {
            if (BuiltInRegistries.ITEM.getKey(mc.player.getInventory().getItem(slot).getItem()).equals(wanted)) {
                return slot;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------ helpers

    private static void step(String name, int delay, BooleanSupplier ready, Runnable action) {
        step(name, delay, ready, action, STEP_TIMEOUT, () -> "no detail");
    }

    private static void step(String name, int delay, BooleanSupplier ready, Runnable action, int timeout,
                             java.util.function.Supplier<String> diagnostic) {
        STEPS.add(new Step(name, delay, ready, action, timeout, diagnostic));
    }

    // Runs on the integrated server; the next step waits until it is done.
    private static void onServer(java.util.function.Consumer<MinecraftServer> task) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        PENDING.incrementAndGet();
        server.execute(() -> {
            try {
                task.accept(server);
            } catch (Throwable t) {
                LOGGER.error("[arcadia-smoke {}] server task threw", ROLE, t);
                fail("server.task", "threw " + t);
            } finally {
                PENDING.decrementAndGet();
            }
        });
    }

    private static SupremeGrinderBlockEntity grinderAt(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        return pos != null && mc.level.getBlockEntity(pos) instanceof SupremeGrinderBlockEntity grinder ? grinder : null;
    }

    private static BlockPos findFryer() {
        return find(FryerBlockEntity.class);
    }

    private static BlockPos findGrinder() {
        return find(SupremeGrinderBlockEntity.class);
    }

    private static BlockPos find(Class<?> type) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos origin = mc.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            if (type.isInstance(mc.level.getBlockEntity(pos))) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static void screenshot(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, "smoke-" + name + ".png", mc.getMainRenderTarget(),
            message -> LOGGER.info("[arcadia-smoke {}] screenshot {}: {}", ROLE, name, message.getString()));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void check(String name, Check check) {
        try {
            pass(name, check.run());
        } catch (Throwable t) {
            fail(name, t.getMessage() == null ? t.toString() : t.getMessage());
            if (!(t instanceof AssertionError)) {
                LOGGER.error("[arcadia-smoke {}] {} threw", ROLE, name, t);
            }
        }
    }

    private static void pass(String name, String detail) {
        REPORT.add("PASS " + name + " - " + detail);
        LOGGER.info("[arcadia-smoke {}] PASS {} - {}", ROLE, name, detail);
    }

    private static void fail(String name, String detail) {
        FAILURES.incrementAndGet();
        REPORT.add("FAIL " + name + " - " + detail);
        LOGGER.error("[arcadia-smoke {}] FAIL {} - {}", ROLE, name, detail);
    }

    private static void finish() {
        finished = true;
        List<String> lines = new ArrayList<>(REPORT);
        lines.add(FAILURES.get() == 0 ? "RESULT PASS " + REPORT.size() + " checks" : "RESULT FAIL " + FAILURES.get() + " of " + REPORT.size() + " checks failed");
        try {
            Files.write(FMLPaths.GAMEDIR.get().resolve("arcadia-smoke-report.txt"), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[arcadia-smoke {}] could not write the report", ROLE, e);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.disconnect();
        }
        mc.stop();
    }
}
