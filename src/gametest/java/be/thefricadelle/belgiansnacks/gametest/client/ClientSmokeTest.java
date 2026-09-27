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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.FluidEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import be.thefricadelle.belgiansnacks.client.BSPartialModels;
import be.thefricadelle.belgiansnacks.client.GrinderMissingScreen;
import be.thefricadelle.belgiansnacks.compat.jei.GrindingGoalCategory;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlock;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.network.GrinderMissingRequestPayload;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Drives a real client and writes a pass/fail report: what the dedicated-server GameTests cannot see.
 * Checks item models and fluid sprites against the missing ones, Create tooltips, the French lang,
 * the creative tab on screen, and that JEI lists our items and shows every loaded recipe. Saves
 * screenshots of the creative tab, the placed fluids and the JEI recipe pages for a person to review.
 * <p>
 * Inert unless {@code -Dcreate_belgian_snacks.clientSmoke=true}, which only the {@code clientSmoke}
 * run sets. The report lands in {@code run/clientsmoke/smoke-report.txt}; {@code verifyClientSmoke}
 * fails the build on any {@code FAIL} line or on a missing report.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class ClientSmokeTest {
    // Every sound this client played (cleared when THE_FRICADELLE is eaten).
    private static final java.util.Set<String> SOUNDS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = Boolean.getBoolean("create_belgian_snacks.clientSmoke");
    private static final String WORLD = "belgian-snacks-smoke";
    private static final int TIMEOUT_TICKS = 20 * 60 * 5;

    // Same set as RecipeGameTests: the dev runtime has neither Farmer's Delight nor a seed-oil mod.
    private static final Set<String> LOADED_IN_DEV = Set.of(
        "crushing/porkchop", "crushing/beef", "crushing/chicken", "crushing/bread",
        "milling/dried_kelp",
        "mixing/fricadelle_paste", "mixing/melted_beef_tallow", "mixing/mayonnaise", "mixing/curry_ketchup_from_beetroot",
        "compacting/frying_oil_from_seeds",
        "pressing/fricadelle_paste",
        "frying/fricadelle", "frying/the_fricadelle",
        "mechanical_crafting/fryer", "mechanical_crafting/supreme_grinder",
        "sequenced_assembly/raw_the_fricadelle_from_beetroot",
        "frying/ultimate_fricadelle", "sequenced_assembly/raw_ultimate_fricadelle_from_beetroot");
    private static final List<FluidEntry<?>> FLUIDS =
        List.of(BSFluids.FRYING_OIL, BSFluids.MELTED_BEEF_TALLOW, BSFluids.MAYONNAISE, BSFluids.CURRY_KETCHUP);

    private static final List<String> REPORT = new ArrayList<>();
    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean planned;
    private static boolean finished;
    private static int ticks;
    private static int wait;
    private static int failures;
    private static BlockPos smokeFryer;
    private static BlockPos smokeGrinder;

    private ClientSmokeTest() {
    }

    private record Step(String name, int delayTicks, BooleanSupplier ready, Runnable action) {
        Step(String name, int delayTicks, Runnable action) {
            this(name, delayTicks, () -> true, action);
        }
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
            plan();
            planned = true;
            Minecraft.getInstance().getSoundManager().addListener((sound, events, range) -> SOUNDS.add(sound.getLocation().toString()));
        }
        ticks++;
        sampleRide();
        if (ticks > TIMEOUT_TICKS) {
            Step stuck = STEPS.peek();
            fail("runner.timeout", "stuck waiting on " + (stuck == null ? "nothing" : stuck.name()));
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
        if (!step.ready().getAsBoolean()) {
            return;
        }
        STEPS.poll();
        LOGGER.info("[smoke] {}", step.name());
        try {
            step.action().run();
        } catch (Throwable t) {
            LOGGER.error("[smoke] step {} threw", step.name(), t);
            fail(step.name(), "threw " + t);
        }
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delayTicks();
    }

    private static void plan() {
        Minecraft mc = Minecraft.getInstance();
        STEPS.add(new Step("title", 0, () -> mc.getOverlay() == null && mc.screen != null && ticks > 20, ClientSmokeTest::createWorld));
        STEPS.add(new Step("world", 40, () -> mc.player != null && mc.level != null && mc.screen == null, ClientSmokeTest::checkResources));
        STEPS.add(new Step("creative.open", 20, ClientSmokeTest::openCreativeTab));
        STEPS.add(new Step("creative.check", 20, ClientSmokeTest::checkCreativeTab));
        STEPS.add(new Step("fluids.place", 10, ClientSmokeTest::placeFluids));
        STEPS.add(new Step("fluids.shot", 40, () -> screenshot("fluids-in-world")));
        STEPS.add(new Step("fryer.place", 10, ClientSmokeTest::placeFryer));
        STEPS.add(new Step("fryer.check", 60, ClientSmokeTest::checkFryer));
        STEPS.add(new Step("fryer.shot", 10, () -> screenshot("fryer-in-world")));
        STEPS.add(new Step("grinder.place", 10, ClientSmokeTest::placeGrinder));
        // Two foods collected on the server, then synced: the client figures follow.
        STEPS.add(new Step("grinder.check", 40, () -> grinderOnClient() != null && grinderOnClient().getCount() == 2,
            ClientSmokeTest::checkGrinder));
        STEPS.add(new Step("grinder.shot", 10, () -> screenshot("grinder-in-world")));
        // A running grinder hums (played on this client every two seconds).
        STEPS.add(new Step("grinder.hum", 0, () -> SOUNDS.contains(BelgianSnacks.MOD_ID + ":grinder.running"),
            () -> check("grinder.hum", () -> "the running grinder hums")));
        // A grinder with nothing above it, seen from straight above: the blades over the mince, which
        // fills half the pit (half of every food, in ULTIMATE mode).
        STEPS.add(new Step("grinder.blades", 10, () -> {
            var server = mc.getSingleplayerServer();
            BlockPos open = smokeGrinder.south(3);
            server.execute(() -> {
                server.overworld().setBlockAndUpdate(open, BSBlocks.SUPREME_GRINDER.getDefaultState());
                if (server.overworld().getBlockEntity(open) instanceof SupremeGrinderBlockEntity grinder) {
                    var ids = be.thefricadelle.belgiansnacks.content.food.FoodIndex.server().ids();
                    grinder.setMode(be.thefricadelle.belgiansnacks.content.grinder.GrinderMode.ULTIMATE);
                    grinder.setConsumed(ids.subList(0, ids.size() / 2));
                }
            });
            mc.player.setPos(open.getX() + 0.5, open.getY() + 2.2, open.getZ() + 0.5);
            mc.player.setXRot(90f);
        }));
        STEPS.add(new Step("grinder.blades.shot", 30, () -> {
            check("grinder.mince", () -> {
                var grinder = (SupremeGrinderBlockEntity) mc.level.getBlockEntity(smokeGrinder.south(3));
                require(Math.abs(grinder.minceLevel() - 0.5f) < 0.05f, "mince level " + grinder.minceLevel() + " on the client");
                return "the pit is " + Math.round(grinder.minceLevel() * 100) + "% full on the client";
            });
            screenshot("grinder-blades");
        }));
        STEPS.add(new Step("grinder.missing.ask", 5, ClientSmokeTest::askMissing));
        STEPS.add(new Step("grinder.missing.check", 0, () -> mc.screen instanceof GrinderMissingScreen, ClientSmokeTest::checkMissingScreen));
        STEPS.add(new Step("grinder.missing.shot", 20, () -> screenshot("grinder-missing")));
        STEPS.add(new Step("grinder.missing.close", 5, () -> mc.setScreen(null)));
        // The player eats THE_FRICADELLE on the integrated server: every gag reaches this client.
        STEPS.add(new Step("ultimate.eat", 10, ClientSmokeTest::eatUltimate));
        // The visitor runs in from a few blocks ahead.
        STEPS.add(new Step("ultimate.run.shot", 4, () -> visitor() != null && visitor().getPhase() == be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.Phase.RUN,
            () -> screenshot("thefricadelle-running")));
        // Waits for the skin too: it comes from Mojang's services, a moment after the visitor appears.
        STEPS.add(new Step("ultimate.check", 5, () -> visitor() != null && visitor().getPhase() == be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.Phase.TALK
            && be.thefricadelle.belgiansnacks.client.NpcSkin.isOnline(), ClientSmokeTest::checkUltimate));
        STEPS.add(new Step("ultimate.face", 5, () -> visitor() != null, ClientSmokeTest::faceVisitor));
        STEPS.add(new Step("ultimate.shot", 10, () -> screenshot("thefricadelle-visitor")));
        // Then it takes off: follow it up for the screenshot.
        STEPS.add(new Step("ultimate.launch", 8, () -> visitor() != null && visitor().getPhase() == be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.Phase.LAUNCH,
            ClientSmokeTest::faceVisitor));
        STEPS.add(new Step("ultimate.launch.shot", 8, () -> {
            faceVisitor();
            screenshot("thefricadelle-takeoff");
        }));
        STEPS.add(new Step("ultimate.gone", 0, () -> visitor() == null, () -> check("ultimate.sounds", () -> {
            for (String sound : List.of("minecraft:item.chorus_fruit.teleport", "minecraft:entity.firework_rocket.launch",
                "minecraft:entity.generic.explode")) {
                require(SOUNDS.contains(sound), "never heard " + sound + " in " + SOUNDS);
            }
            String voice = BSSoundEvents.NPC_VOICE.getId().toString();
            require(SOUNDS.contains(voice), "never heard " + voice + " in " + SOUNDS);
            return "chorus on arrival, the recorded voice line, rocket and explosion on take-off";
        })));
        STEPS.add(new Step("jei.ready", 20, () -> SmokeJeiPlugin.runtime != null, ClientSmokeTest::checkJei));
        STEPS.add(new Step("jei.paste", 10, () -> showOutput(BSItems.FRICADELLE_PASTE.asStack())));
        STEPS.add(new Step("jei.paste.shot", 30, () -> screenshot("jei-fricadelle-paste")));
        STEPS.add(new Step("jei.raw", 10, () -> showOutput(BSItems.RAW_FRICADELLE.asStack())));
        STEPS.add(new Step("jei.raw.shot", 30, () -> screenshot("jei-raw-fricadelle")));
        STEPS.add(new Step("jei.tallow", 10, () -> showOutput(BSFluids.MELTED_BEEF_TALLOW)));
        STEPS.add(new Step("jei.tallow.shot", 30, () -> screenshot("jei-melted-beef-tallow")));
        STEPS.add(new Step("jei.mayo", 10, () -> showOutput(BSFluids.MAYONNAISE)));
        STEPS.add(new Step("jei.mayo.shot", 30, () -> screenshot("jei-mayonnaise")));
        STEPS.add(new Step("jei.ketchup", 10, () -> showOutput(BSFluids.CURRY_KETCHUP)));
        STEPS.add(new Step("jei.ketchup.shot", 30, () -> screenshot("jei-curry-ketchup")));
        STEPS.add(new Step("jei.oil", 10, () -> showOutput(BSFluids.FRYING_OIL)));
        STEPS.add(new Step("jei.oil.shot", 30, () -> screenshot("jei-frying-oil")));
        STEPS.add(new Step("jei.mince", 10, () -> showOutput(BSItems.MINCED_BEEF.asStack())));
        STEPS.add(new Step("jei.mince.shot", 30, () -> screenshot("jei-minced-beef")));
        STEPS.add(new Step("jei.spices", 10, () -> showOutput(BSItems.BELGIAN_SPICES.asStack())));
        STEPS.add(new Step("jei.spices.shot", 30, () -> screenshot("jei-belgian-spices")));
        STEPS.add(new Step("jei.frying", 10, () -> showOutput(BSItems.FRICADELLE.asStack())));
        STEPS.add(new Step("jei.frying.shot", 30, () -> screenshot("jei-frying")));
        STEPS.add(new Step("jei.fryerCraft", 10, () -> showOutput(new ItemStack(BSBlocks.FRYER.get()))));
        STEPS.add(new Step("jei.fryerCraft.shot", 30, () -> screenshot("jei-fryer-craft")));
        STEPS.add(new Step("jei.assembly", 10, () -> showOutput(BSItems.RAW_THE_FRICADELLE.asStack())));
        STEPS.add(new Step("jei.assembly.shot", 30, () -> screenshot("jei-raw-the-fricadelle")));
        STEPS.add(new Step("jei.theFrying", 10, () -> showOutput(BSItems.THE_FRICADELLE.asStack())));
        STEPS.add(new Step("jei.theFrying.shot", 30, () -> screenshot("jei-the-fricadelle")));
        STEPS.add(new Step("jei.ultimateAssembly", 10, () -> showOutput(BSItems.RAW_ULTIMATE_FRICADELLE.asStack())));
        STEPS.add(new Step("jei.ultimateAssembly.shot", 30, () -> screenshot("jei-raw-ultimate-fricadelle")));
        STEPS.add(new Step("jei.ultimateFrying", 10, () -> showOutput(BSItems.ULTIMATE_FRICADELLE.asStack())));
        STEPS.add(new Step("jei.ultimateFrying.shot", 30, () -> screenshot("jei-ultimate-fricadelle")));
        STEPS.add(new Step("jei.grinderCraft", 10, () -> showOutput(new ItemStack(BSBlocks.SUPREME_GRINDER.get()))));
        STEPS.add(new Step("jei.grinderCraft.shot", 30, () -> screenshot("jei-grinder-craft")));
        STEPS.add(new Step("jei.grinding", 10, () -> SmokeJeiPlugin.runtime.getRecipesGui().showTypes(List.of(GrindingGoalCategory.TYPE))));
        STEPS.add(new Step("jei.grinding.shot", 30, () -> screenshot("jei-grinding-goal")));
        STEPS.add(new Step("ponder.check", 10, () -> mc.setScreen(null)));
        STEPS.add(new Step("ponder.scenes", 5, ClientSmokeTest::checkPonder));
        STEPS.add(new Step("ponder.fryer", 5, () -> mc.setScreen(net.createmod.ponder.foundation.ui.PonderUI.of(new ItemStack(BSBlocks.FRYER.get())))));
        STEPS.add(new Step("ponder.fryer.belt", 0, () -> ponderBeltItems() > 0 || ponderEnded(), () -> checkPonderBelt("fryer")));
        STEPS.add(new Step("ponder.fryer.belt.shot", 3, () -> screenshot("ponder-fryer-belt")));
        STEPS.add(new Step("ponder.fryer.shot", 60, () -> screenshot("ponder-fryer")));
        STEPS.add(new Step("ponder.grinder", 5, () -> mc.setScreen(net.createmod.ponder.foundation.ui.PonderUI.of(new ItemStack(BSBlocks.SUPREME_GRINDER.get())))));
        STEPS.add(new Step("ponder.grinder.belt", 0, () -> ponderBeltItems() > 0 || ponderEnded(), () -> checkPonderBelt("grinder")));
        STEPS.add(new Step("ponder.grinder.belt.shot", 3, () -> screenshot("ponder-grinder-belt")));
        STEPS.add(new Step("ponder.grinder.shot", 60, () -> screenshot("ponder-supreme-grinder")));
        STEPS.add(new Step("ponder.line2", 5, () -> openLine(BSItems.EXCEPTIONAL_PASTE.asStack())));
        STEPS.add(new Step("ponder.line2.belt", 0, () -> ponderBeltItems() > 0 || ponderEnded(), () -> checkPonderBelt("line2")));
        STEPS.add(new Step("ponder.line2.belt.shot", 3, () -> screenshot("ponder-line2-belt")));
        STEPS.add(new Step("ponder.line2.shot", 200, () -> screenshot("ponder-the-fricadelle-line")));
        STEPS.add(new Step("ponder.line2.ride", 0, ClientSmokeTest::ponderEnded, () -> checkRide("line2")));
        STEPS.add(new Step("ponder.line3", 5, () -> openLine(BSItems.ABSOLUTE_PASTE.asStack())));
        STEPS.add(new Step("ponder.line3.shot", 120, () -> screenshot("ponder-ultimate-fricadelle-line")));
        STEPS.add(new Step("ponder.line3.ride", 0, ClientSmokeTest::ponderEnded, () -> checkRide("line3")));
        STEPS.add(new Step("close", 10, () -> mc.setScreen(null)));
    }

    // ------------------------------------------------------------------ steps

    private static void createWorld() {
        Minecraft mc = Minecraft.getInstance();
        LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
            new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            mc.screen);
    }

    private static void checkResources() {
        Minecraft mc = Minecraft.getInstance();
        check("models.items", () -> {
            BakedModel missing = mc.getModelManager().getMissingModel();
            List<String> bad = new ArrayList<>();
            for (Item item : modItems()) {
                BakedModel model = mc.getItemRenderer().getModel(new ItemStack(item), mc.level, mc.player, 0);
                if (model == missing || isMissing(model.getParticleIcon().contents().name())) {
                    bad.add(id(item));
                }
            }
            require(bad.isEmpty(), "missing model or texture: " + bad);
            return modItems().size() + " item models with real textures";
        });
        check("fluids.sprites", () -> {
            TextureAtlas atlas = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
            List<String> bad = new ArrayList<>();
            for (FluidEntry<?> entry : FLUIDS) {
                IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(entry.getType());
                for (ResourceLocation texture : List.of(ext.getStillTexture(), ext.getFlowingTexture())) {
                    if (isMissing(atlas.getSprite(texture).contents().name())) {
                        bad.add(texture.toString());
                    }
                }
            }
            require(bad.isEmpty(), "missing fluid sprites: " + bad);
            return FLUIDS.size() * 2 + " fluid sprites stitched";
        });
        check("tooltips.create", () -> {
            for (var entry : List.of(BSItems.FRICADELLE, BSItems.THE_FRICADELLE, BSItems.ULTIMATE_FRICADELLE)) {
                Item item = entry.get();
                require(TooltipModifier.REGISTRY.get(item) != null, id(item) + " has no Create tooltip modifier");
                require(ItemDescription.create(item, FontHelper.Palette.STANDARD_CREATE) != null, id(item) + " has no summary to show");
                List<String> lines = entry.asStack().getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL)
                    .stream().map(c -> c.getString()).toList();
                require(lines.size() >= 2, id(item) + " tooltip has only " + lines);
            }
            return "3 fricadelles show the Create summary hint";
        });
        check("foodIndex.synced", () -> {
            var server = be.thefricadelle.belgiansnacks.content.food.FoodIndex.server();
            var client = be.thefricadelle.belgiansnacks.content.food.FoodIndex.client();
            require(client.size() > 0, "the client never received the food index");
            require(client.ids().equals(server.ids()), "client holds " + client.size() + " foods, the server " + server.size());
            return client.size() + " foods received, identical to the server's";
        });
        check("lang.french", () -> {
            ClientLanguage fr = ClientLanguage.loadFrom(mc.getResourceManager(), List.of("en_us", "fr_fr"), false);
            ClientLanguage en = ClientLanguage.loadFrom(mc.getResourceManager(), List.of("en_us"), false);
            List<String> untranslated = new ArrayList<>();
            for (Item item : modItems()) {
                String key = item.getDescriptionId();
                require(fr.has(key), "no name for " + key);
                // The three fricadelle names are a brand, identical in every language.
                boolean brand = item == BSItems.FRICADELLE.get() || item == BSItems.THE_FRICADELLE.get()
                    || item == BSItems.ULTIMATE_FRICADELLE.get();
                if (!brand && fr.getOrDefault(key).equals(en.getOrDefault(key))) {
                    untranslated.add(key);
                }
            }
            require(untranslated.isEmpty(), "French name equals English for " + untranslated);
            require("Hachis de porc".equals(fr.getOrDefault(BSItems.MINCED_PORK.get().getDescriptionId())), "fr_fr not the one loaded");
            return modItems().size() + " items named in French";
        });
    }

    private static void openCreativeTab() {
        Minecraft mc = Minecraft.getInstance();
        setSelectedTab(BSCreativeTabs.MAIN.get());
        mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get()));
    }

    private static void checkCreativeTab() {
        Minecraft mc = Minecraft.getInstance();
        check("creative.tab", () -> {
            require(mc.screen instanceof CreativeModeInventoryScreen, "creative screen not shown");
            CreativeModeTab tab = BSCreativeTabs.MAIN.get();
            require(tab.shouldDisplay(), "our tab is hidden");
            require(((CreativeModeInventoryScreen) mc.screen).getCurrentPage().getVisibleTabs().contains(tab), "our tab is not on the shown page");
            List<String> missing = new ArrayList<>();
            for (Item item : modItems()) {
                if (tab.getDisplayItems().stream().noneMatch(stack -> stack.is(item))) {
                    missing.add(id(item));
                }
            }
            require(missing.isEmpty(), "not in the tab: " + missing);
            return tab.getDisplayItems().size() + " stacks shown";
        });
        screenshot("creative-tab");
        mc.setScreen(null);
    }

    // Places one source of each fluid in a row ahead of the player, who is turned to face them.
    private static void placeFluids() {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        BlockPos base = mc.player.blockPosition().north(4);
        var player = mc.player;
        server.execute(() -> {
            var level = server.overworld();
            for (int i = 0; i < FLUIDS.size(); i++) {
                Fluid source = FLUIDS.get(i).get().getSource();
                level.setBlockAndUpdate(base.east(i * 2 - 3), source.defaultFluidState().createLegacyBlock());
            }
        });
        player.setYRot(180f);
        player.setXRot(35f);
        mc.options.hideGui = true;
    }

    // A fryer on a creative (never fading) burner sunk in the ground, filled and loaded, two blocks south
    // of the player, who looks down into the vat.
    private static void placeFryer() {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        BlockPos fryerPos = mc.player.blockPosition().south(2);
        server.execute(() -> {
            var level = server.overworld();
            level.setBlockAndUpdate(fryerPos.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
            if (level.getBlockEntity(fryerPos.below()) instanceof BlazeBurnerBlockEntity burner) {
                burner.isCreative = true;
            }
            level.setBlockAndUpdate(fryerPos, BSBlocks.FRYER.getDefaultState());
            if (level.getBlockEntity(fryerPos) instanceof FryerBlockEntity fryer) {
                fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 2000),
                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(16), false);
            }
        });
        smokeFryer = fryerPos;
        // Hover two blocks up, flying, so the camera looks down into the vat.
        mc.player.getAbilities().flying = true;
        mc.player.setPos(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ() - 0.5);
        mc.player.setYRot(0f);
        mc.player.setXRot(48f);
        mc.options.hideGui = true;
    }

    private static void checkFryer() {
        Minecraft mc = Minecraft.getInstance();
        check("fryer.model", () -> {
            var state = BSBlocks.FRYER.getDefaultState();
            BakedModel model = mc.getBlockRenderer().getBlockModel(state);
            require(model != mc.getModelManager().getMissingModel(), "fryer block model missing");
            require(!isMissing(model.getParticleIcon().contents().name()), "fryer block texture missing");
            return "vat model with textures";
        });
        check("fryer.running", () -> {
            require(mc.level.getBlockEntity(smokeFryer) instanceof FryerBlockEntity, "no fryer block entity on the client");
            FryerBlockEntity fryer = (FryerBlockEntity) mc.level.getBlockEntity(smokeFryer);
            require(fryer.getTank().getPrimaryHandler().getFluidAmount() > 0, "client sees no fat");
            require(fryer.status() == FryerBlockEntity.Status.FRYING, "client status is " + fryer.status());
            require(fryer.getBasket().getCount() == 16, "client basket holds " + fryer.getBasket());
            return "frying 16, synced to the client";
        });
        check("fryer.goggles", () -> {
            FryerBlockEntity fryer = (FryerBlockEntity) mc.level.getBlockEntity(smokeFryer);
            List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
            require(fryer.addToGoggleTooltip(lines, false), "goggles tooltip refused");
            String text = String.join(" | ", lines.stream().map(net.minecraft.network.chat.Component::getString).toList());
            require(lines.size() >= 4, "goggles lines: " + text);
            require(!text.contains(BelgianSnacks.MOD_ID + "."), "untranslated goggles key: " + text);
            return text;
        });
    }

    // A running grinder three blocks west of the fryer, with two foods already collected.
    private static void placeGrinder() {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        BlockPos grinderPos = smokeFryer.west(3);
        server.execute(() -> {
            var level = server.overworld();
            level.setBlockAndUpdate(grinderPos, BSBlocks.SUPREME_GRINDER.getDefaultState());
            level.setBlockAndUpdate(grinderPos.above(), AllBlocks.CREATIVE_MOTOR.getDefaultState()
                .setValue(BlockStateProperties.FACING, Direction.DOWN));
            if (level.getBlockEntity(grinderPos.above()) instanceof CreativeMotorBlockEntity motor) {
                motor.generatedSpeed.setValue(64);
            }
            if (level.getBlockEntity(grinderPos) instanceof SupremeGrinderBlockEntity grinder) {
                grinder.setConsumed(FoodIndex.server().ids().subList(0, 2));
            }
        });
        smokeGrinder = grinderPos;
        // Look west and down at it.
        mc.player.setYRot(90f);
        mc.player.setXRot(30f);
    }

    private static SupremeGrinderBlockEntity grinderOnClient() {
        Minecraft mc = Minecraft.getInstance();
        return smokeGrinder != null && mc.level.getBlockEntity(smokeGrinder) instanceof SupremeGrinderBlockEntity grinder ? grinder : null;
    }

    private static void checkGrinder() {
        Minecraft mc = Minecraft.getInstance();
        check("grinder.model", () -> {
            BakedModel missing = mc.getModelManager().getMissingModel();
            for (int fill = 0; fill <= 4; fill++) {
                var state = BSBlocks.SUPREME_GRINDER.getDefaultState().setValue(SupremeGrinderBlock.FILL, fill);
                BakedModel model = mc.getBlockRenderer().getBlockModel(state);
                require(model != missing, "no model for fill " + fill);
                require(!isMissing(model.getParticleIcon().contents().name()), "missing texture for fill " + fill);
            }
            BakedModel blades = BSPartialModels.GRINDER_BLADES.get();
            require(blades != null && blades != missing, "blade partial model not loaded");
            require(!isMissing(blades.getParticleIcon().contents().name()), "blade texture missing");
            return "5 gauge models and the rotating blades";
        });
        check("grinder.synced", () -> {
            SupremeGrinderBlockEntity grinder = grinderOnClient();
            int total = FoodIndex.client().size();
            int goal = GrinderProgress.goal(BSConfig.grinderTheFricadelleRatio(), total);
            require(grinder.getTotal() == total && grinder.getGoal() == goal, "client sees " + grinder.getGoal() + " of " + grinder.getTotal());
            require(grinder.getSamples().size() == 5, "client got " + grinder.getSamples().size() + " missing examples");
            require(Math.abs(grinder.getSpeed()) == 64, "client sees " + grinder.getSpeed() + " RPM");
            return "2 / " + goal + " of " + total + " foods, 5 examples, 64 RPM";
        });
        check("grinder.goggles", () -> {
            List<Component> lines = new ArrayList<>();
            require(grinderOnClient().addToGoggleTooltip(lines, false), "goggles tooltip refused");
            String text = String.join(" | ", lines.stream().map(Component::getString).toList());
            int goal = grinderOnClient().getGoal();
            require(text.contains("2 / " + goal + " foods"), "no progress line: " + text);
            require(text.contains("THE_Fricadelle (Exceptional Paste)"), "no mode line: " + text);
            require(!text.contains(BelgianSnacks.MOD_ID + "."), "untranslated goggles key: " + text);
            return text;
        });
    }

    // The real path: the payload a sneaking player with goggles sends, answered by the server.
    private static void askMissing() {
        PacketDistributor.sendToServer(new GrinderMissingRequestPayload(smokeGrinder));
    }

    private static void checkMissingScreen() {
        Minecraft mc = Minecraft.getInstance();
        check("grinder.missingScreen", () -> {
            GrinderMissingScreen screen = (GrinderMissingScreen) mc.screen;
            int expected = FoodIndex.client().size() - 2;
            require(screen.getPos().equals(smokeGrinder), "screen for " + screen.getPos());
            require(screen.missingCount() == expected, "lists " + screen.missingCount() + " foods, expected " + expected);
            require(screen.drawnPerFrame() > 0 && screen.drawnPerFrame() <= expected, "draws " + screen.drawnPerFrame() + " icons");
            return expected + " missing foods listed, " + screen.drawnPerFrame() + " icons drawn per frame";
        });
    }

    private static final List<String> SYSTEM_CHAT = java.util.Collections.synchronizedList(new ArrayList<>());

    @SubscribeEvent
    public static void onSystemChat(net.neoforged.neoforge.client.event.ClientChatReceivedEvent.System event) {
        if (ENABLED) {
            SYSTEM_CHAT.add(event.getMessage().getString());
        }
    }

    private static void eatUltimate() {
        Minecraft mc = Minecraft.getInstance();
        SOUNDS.clear();
        var server = mc.getSingleplayerServer();
        java.util.UUID id = mc.player.getUUID();
        SYSTEM_CHAT.clear();
        mc.player.setYRot(0f);
        mc.player.setXRot(10f);
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            ItemStack food = BSItems.ULTIMATE_FRICADELLE.asStack();
            food.getItem().finishUsingItem(food, player.serverLevel(), player);
        });
    }

    private static be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc visitor() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.getEntitiesOfClass(be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.class,
            mc.player.getBoundingBox().inflate(40)).stream().findFirst().orElse(null);
    }

    // Face the visitor, from where the player floats.
    private static void faceVisitor() {
        Minecraft mc = Minecraft.getInstance();
        var npc = visitor();
        if (npc == null) {
            return;
        }
        var eye = mc.player.getEyePosition();
        double dx = npc.getX() - eye.x;
        double dy = npc.getEyeY() - eye.y;
        double dz = npc.getZ() - eye.z;
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(dz, dx)) - 90f);
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    private static void checkUltimate() {
        Minecraft mc = Minecraft.getInstance();
        check("ultimate.effects", () -> {
            require(mc.player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST), "no strength on the client");
            require(mc.player.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE), "no fire resistance on the client");
            return mc.player.getActiveEffects().size() + " effects synced to the client";
        });
        check("ultimate.chat", () -> {
            String eaten = mc.player.getName().getString() + " ate THE_FRICADELLE";
            require(SYSTEM_CHAT.stream().anyMatch(line -> line.startsWith(eaten)), "no server announcement in " + SYSTEM_CHAT);
            // The visitor speaks aloud and above its head, never in the chat.
            require(SYSTEM_CHAT.stream().noneMatch(line -> line.startsWith("<THEFricadelle> ")), "the visitor wrote in the chat: " + SYSTEM_CHAT);
            return String.join(" | ", SYSTEM_CHAT);
        });
        check("ultimate.visitor", () -> {
            var npc = visitor();
            var renderer = mc.getEntityRenderDispatcher().getRenderer(npc);
            require(renderer instanceof be.thefricadelle.belgiansnacks.client.TheFricadelleNpcRenderer, "rendered by " + renderer);
            require(npc.isCustomNameVisible() && npc.getCustomName() != null, "no line above the head");
            var skin = be.thefricadelle.belgiansnacks.client.NpcSkin.get();
            String account = be.thefricadelle.belgiansnacks.client.NpcSkin.account();
            // The author's own account: a lookup by the display name found another player's skin once.
            require("THE_Fricadelle".equals(account), "skin of account " + account + ", expected THE_Fricadelle");
            require(skin.texture() != null, "no skin texture");
            return "\"" + npc.getCustomName().getString() + "\", skin of the " + account + " account, " + skin.model() + " model";
        });
    }

    // Items on the belts of the open Ponder scene, counted on the controllers (they carry the items).
    private static int ponderBeltItems() {
        if (!(Minecraft.getInstance().screen instanceof net.createmod.ponder.foundation.ui.PonderUI ui)) {
            return 0;
        }
        var world = ui.getActiveScene().getWorld();
        int items = 0;
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 9, 5, 9)) {
            if (world.getBlockEntity(pos) instanceof com.simibubi.create.content.kinetics.belt.BeltBlockEntity belt && belt.isController()) {
                items += belt.getInventory().getTransportedItems().size();
            }
        }
        return items;
    }

    // The most items seen at once on an assembly line's belt, and the farthest one got, since it opened.
    private static int rideMaxItems;
    private static float rideMaxPosition;
    private static int rideLength;
    private static boolean rideWatching;
    private static int ridePressTicks;
    private static String rideLast = "";
    private static final StringBuilder rideTrace = new StringBuilder();
    private static final java.util.SortedSet<Float> rideStops = new java.util.TreeSet<>();

    private static void openLine(ItemStack paste) {
        rideMaxItems = 0;
        rideMaxPosition = 0;
        rideLength = 0;
        rideWatching = true;
        rideTrace.setLength(0);
        rideStops.clear();
        ridePressTicks = 0;
        Minecraft.getInstance().setScreen(net.createmod.ponder.foundation.ui.PonderUI.of(paste));
    }

    private static void sampleRide() {
        if (!rideWatching || !(Minecraft.getInstance().screen instanceof net.createmod.ponder.foundation.ui.PonderUI ui)) {
            return;
        }
        var world = ui.getActiveScene().getWorld();
        int items = 0;
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 9, 5, 9)) {
            if (world.getBlockEntity(pos) instanceof com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity press
                && press.getPressingBehaviour().running) {
                ridePressTicks = Math.max(ridePressTicks, press.getPressingBehaviour().runningTicks);
            }
            if (world.getBlockEntity(pos) instanceof com.simibubi.create.content.kinetics.belt.BeltBlockEntity belt && belt.isController()) {
                rideLength = belt.beltLength;
                for (var stack : belt.getInventory().getTransportedItems()) {
                    items++;
                    rideMaxPosition = Math.max(rideMaxPosition, stack.beltPosition);
                    String state = String.format("%.1f%s", stack.beltPosition, stack.locked ? "L" : "");
                    if (stack.locked) {
                        rideStops.add(Math.round(stack.beltPosition * 10) / 10f);
                    }
                    if (!state.equals(rideLast)) {
                        rideLast = state;
                        rideTrace.append(ui.getActiveScene().getCurrentTime()).append(':').append(state).append(' ');
                    }
                }
            }
        }
        rideMaxItems = Math.max(rideMaxItems, items);
    }

    // One item rides the line from end to end: never a copy, never past the last segment.
    private static void checkRide(String scene) {
        rideWatching = false;
        check("ponder.ride." + scene, () -> {
            require(rideMaxItems == 1, rideMaxItems + " items on the belt at once");
            require(rideMaxPosition > rideLength - 1 && rideMaxPosition < rideLength, "the item got to " + rideMaxPosition + " on a belt of " + rideLength + "; " + rideTrace);
            // It waits at the start, under each station, and at the end: every segment's centre.
            java.util.SortedSet<Float> expected = new java.util.TreeSet<>();
            for (int segment = 0; segment < rideLength; segment++) {
                expected.add(segment + 0.5f);
            }
            require(rideStops.equals(expected), "stopped at " + rideStops + ", expected " + expected + "; " + rideTrace);
            // The press goes down and all the way back up before the item moves on.
            require(ridePressTicks >= com.simibubi.create.content.kinetics.press.PressingBehaviour.CYCLE,
                "the press stroke stopped at " + ridePressTicks + " of " + com.simibubi.create.content.kinetics.press.PressingBehaviour.CYCLE);
            return "one item, stopped at " + rideStops + " on a belt of " + rideLength + ", full press stroke";
        });
    }

    private static boolean ponderEnded() {
        return Minecraft.getInstance().screen instanceof net.createmod.ponder.foundation.ui.PonderUI ui
            && ui.getActiveScene().getCurrentTime() >= ui.getActiveScene().getTotalTime() - 5;
    }

    private static void checkPonderBelt(String scene) {
        check("ponder.belt." + scene, () -> {
            var ui = (net.createmod.ponder.foundation.ui.PonderUI) Minecraft.getInstance().screen;
            var world = ui.getActiveScene().getWorld();
            List<String> belts = new java.util.ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 9, 5, 9)) {
                if (world.getBlockEntity(pos) instanceof com.simibubi.create.content.kinetics.belt.BeltBlockEntity belt) {
                    belts.add(pos.toShortString() + "->" + belt.getController().toShortString());
                }
            }
            require(ponderBeltItems() > 0, "no item ever rode a belt; belts and their controllers: " + belts);
            return ponderBeltItems() + " item(s) on the belt at tick " + ui.getActiveScene().getCurrentTime();
        });
    }

    // Every scene of ours builds without an error and finds its schematic; the tag is there.
    private static void checkPonder() {
        check("ponder.scenes", () -> {
            var access = net.createmod.ponder.foundation.PonderIndex.getSceneAccess();
            List<String> built = new ArrayList<>();
            Set<ResourceLocation> components = new TreeSet<>();
            for (var entry : access.getRegisteredEntries()) {
                if (entry.getValue().getNamespace().equals(BelgianSnacks.MOD_ID)) {
                    components.add(entry.getKey());
                    ResourceLocation schematic = entry.getValue().getSchematicLocation();
                    var template = net.createmod.ponder.foundation.registration.PonderSceneRegistry.loadSchematic(schematic);
                    require(template.getSize().getX() > 0, "no schematic " + schematic);
                }
            }
            for (ResourceLocation component : components) {
                for (var scene : access.compile(component)) {
                    built.add(component.getPath() + ":" + scene.getId().getPath());
                }
            }
            require(built.size() == 9, "scenes built: " + built);
            require(net.createmod.ponder.foundation.PonderIndex.getTagAccess().getListedTags().stream()
                .anyMatch(tag -> tag.getId().equals(BelgianSnacks.asResource("belgian_snacks"))), "no Ponder tag");
            return built.size() + " scenes built for " + components.size() + " components: " + built;
        });
    }

    private static void checkJei() {
        Minecraft.getInstance().options.hideGui = false;
        IJeiRuntime jei = SmokeJeiPlugin.runtime;
        check("jei.items", () -> {
            List<String> missing = new ArrayList<>();
            for (Item item : modItems()) {
                if (jei.getIngredientManager().getAllItemStacks().stream().noneMatch(stack -> stack.is(item))) {
                    missing.add(id(item));
                }
            }
            require(missing.isEmpty(), "not in the JEI list: " + missing);
            return modItems().size() + " items listed";
        });
        check("jei.recipes", () -> {
            Set<String> shown = new TreeSet<>();
            jei.getRecipeManager().createRecipeCategoryLookup().get().forEach(category ->
                jei.getRecipeManager().createRecipeLookup(category.getRecipeType()).get().forEach(recipe -> {
                    if (recipe instanceof RecipeHolder<?> holder && holder.id().getNamespace().equals(BelgianSnacks.MOD_ID)) {
                        shown.add(holder.id().getPath());
                    }
                }));
            Set<String> missing = new TreeSet<>(LOADED_IN_DEV);
            missing.removeAll(shown);
            require(missing.isEmpty(), "loaded but not shown in JEI: " + missing);
            require(!shown.contains("mixing/curry_ketchup"), "JEI shows the tomato recipe although it is disabled");
            return shown.size() + " recipes shown: " + shown;
        });
        check("jei.grinding", () -> {
            List<GrinderMode> modes = jei.getRecipeManager().createRecipeLookup(GrindingGoalCategory.TYPE).get().toList();
            require(modes.size() == 2, "grinding goal entries: " + modes);
            return "grinding goal shown for " + modes;
        });
    }

    private static void showOutput(ItemStack stack) {
        IJeiRuntime jei = SmokeJeiPlugin.runtime;
        jei.getRecipesGui().show(jei.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
    }

    private static void showOutput(FluidEntry<?> fluid) {
        IJeiRuntime jei = SmokeJeiPlugin.runtime;
        FluidStack stack = new FluidStack(fluid.get().getSource(), 1000);
        jei.getRecipesGui().show(jei.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT, NeoForgeTypes.FLUID_STACK, stack));
    }

    // ------------------------------------------------------------------ helpers

    /** Saves the frame to {@code screenshots/smoke-<name>.png}; for a person to review, not a check. */
    private static void screenshot(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, "smoke-" + name + ".png", mc.getMainRenderTarget(),
            message -> LOGGER.info("[smoke] screenshot {}: {}", name, message.getString()));
    }

    private static boolean isMissing(ResourceLocation sprite) {
        return sprite.equals(MissingTextureAtlasSprite.getLocation());
    }

    private static void setSelectedTab(CreativeModeTab tab) {
        try {
            Field field = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
            field.setAccessible(true);
            field.set(null, tab);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("CreativeModeInventoryScreen.selectedTab is gone", e);
        }
    }

    private static List<Item> modItems() {
        return BuiltInRegistries.ITEM.stream()
            .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(BelgianSnacks.MOD_ID))
            .toList();
    }

    private static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    // ------------------------------------------------------------------ report

    private static void check(String name, Check check) {
        try {
            String detail = check.run();
            REPORT.add("PASS " + name + " - " + detail);
            LOGGER.info("[smoke] PASS {} - {}", name, detail);
        } catch (Throwable t) {
            fail(name, t.getMessage() == null ? t.toString() : t.getMessage());
            if (!(t instanceof AssertionError)) {
                LOGGER.error("[smoke] {} threw", name, t);
            }
        }
    }

    private static void fail(String name, String detail) {
        failures++;
        REPORT.add("FAIL " + name + " - " + detail);
        LOGGER.error("[smoke] FAIL {} - {}", name, detail);
    }

    private static void finish() {
        finished = true;
        List<String> lines = new ArrayList<>(REPORT);
        lines.add(failures == 0
            ? "RESULT PASS " + REPORT.size() + " checks"
            : "RESULT FAIL " + failures + " of " + REPORT.size() + " checks failed");
        Path report = FMLPaths.GAMEDIR.get().resolve("smoke-report.txt");
        try {
            Files.write(report, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[smoke] could not write {}", report, e);
        }
        Minecraft.getInstance().stop();
    }
}
