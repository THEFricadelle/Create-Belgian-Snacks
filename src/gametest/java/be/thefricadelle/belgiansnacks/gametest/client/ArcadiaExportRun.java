/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.command.BSCommands;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Exports the food index of the real Arcadia pack (tools/arcadia_export.py copies its mods, KubeJS
 * scripts and configs into run/arcadia). Creates a world, exports on the integrated server, writes
 * a per-mod summary and quits. Inert unless {@code -Dcreate_belgian_snacks.arcadiaExport=true}.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class ArcadiaExportRun {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = Boolean.getBoolean("create_belgian_snacks.arcadiaExport");
    private static final String WORLD = "belgian-snacks-export";

    private enum Stage { WAIT_TITLE, WAIT_WORLD, EXPORTING, DONE }

    private static Stage stage = Stage.WAIT_TITLE;
    private static int ticks;

    private ArcadiaExportRun() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || stage == Stage.DONE) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        switch (stage) {
            case WAIT_TITLE -> {
                if (mc.getOverlay() == null && mc.screen != null && ticks > 40) {
                    LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                        new GameRules(), WorldDataConfiguration.DEFAULT);
                    mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                        registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
                    stage = Stage.WAIT_WORLD;
                }
            }
            case WAIT_WORLD -> {
                if (mc.player != null && mc.getSingleplayerServer() != null && FoodIndex.server().size() > 0) {
                    stage = Stage.EXPORTING;
                    var server = mc.getSingleplayerServer();
                    server.execute(() -> {
                        try {
                            Path csv = FMLPaths.GAMEDIR.get().resolve("arcadia-foods.csv");
                            BSCommands.exportTo(server, csv);
                            writeSummary(csv);
                        } catch (Exception e) {
                            LOGGER.error("[arcadia-export] failed", e);
                        }
                        mc.execute(() -> {
                            stage = Stage.DONE;
                            mc.level.disconnect();
                            mc.stop();
                        });
                    });
                }
            }
            default -> {
            }
        }
    }

    // Per mod: foods, and how many no recipe produces (the blacklist candidates).
    static void writeSummary(Path csv) throws Exception {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        Map<String, int[]> perMod = new TreeMap<>();
        int noRecipe = 0;
        for (String line : lines.subList(1, lines.size())) {
            String[] cells = line.split(",");
            int[] counts = perMod.computeIfAbsent(cells[1], k -> new int[2]);
            counts[0]++;
            if (cells[5].equals("false")) {
                counts[1]++;
                noRecipe++;
            }
        }
        FoodIndex.Snapshot index = FoodIndex.server();
        List<String> summary = new ArrayList<>();
        summary.add("foods " + index.size());
        summary.add("excluded " + index.excluded());
        summary.add("without_recipe " + noRecipe);
        summary.add("compute_ms_at_start " + String.format(java.util.Locale.ROOT, "%.1f", index.computeNanos() / 1_000_000.0));
        // Steady state: the start-up figure includes JIT warm-up and whatever the world load is doing.
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++) {
            best = Math.min(best, FoodIndex.compute(be.thefricadelle.belgiansnacks.config.BSConfig.grinderBlacklistedMods(),
                be.thefricadelle.belgiansnacks.config.BSConfig.grinderBlacklistedItems()).computeNanos());
        }
        summary.add("compute_ms_steady " + String.format(java.util.Locale.ROOT, "%.1f", best / 1_000_000.0));
        summary.add("mods " + perMod.size());
        perMod.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
            .forEach(e -> summary.add("mod " + e.getKey() + " " + e.getValue()[0] + " " + e.getValue()[1]));
        Files.write(FMLPaths.GAMEDIR.get().resolve("arcadia-foods-summary.txt"), summary, StandardCharsets.UTF_8);
        LOGGER.info("[arcadia-export] {} foods from {} mods, {} without a recipe", index.size(), perMod.size(), noRecipe);
    }
}
