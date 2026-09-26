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
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
        "pressing/fricadelle_paste");
    private static final List<FluidEntry<?>> FLUIDS =
        List.of(BSFluids.FRYING_OIL, BSFluids.MELTED_BEEF_TALLOW, BSFluids.MAYONNAISE, BSFluids.CURRY_KETCHUP);

    private static final List<String> REPORT = new ArrayList<>();
    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean planned;
    private static boolean finished;
    private static int ticks;
    private static int wait;
    private static int failures;

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
        }
        ticks++;
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
