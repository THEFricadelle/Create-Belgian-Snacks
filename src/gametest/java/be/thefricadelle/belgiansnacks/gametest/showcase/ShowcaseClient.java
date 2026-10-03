/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.showcase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opens the showcase world from the title screen, creating it (flat, creative, peaceful, no
 * structures) the first time. In check mode, once the server reports, it films each station from a
 * fixed viewpoint, writes the report next to the screenshots and quits.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class ShowcaseClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Frames given to the chunks around a new viewpoint before its screenshot.
    private static final int SETTLE_TICKS = 40;

    private record Shot(String name, double x, double y, double z, float yaw, float pitch) {
    }

    private static boolean opened;
    private static boolean filming;
    private static final Deque<Shot> SHOTS = new ArrayDeque<>();
    private static int wait;

    private ShowcaseClient() {
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (Showcase.MODE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!opened && mc.screen instanceof TitleScreen) {
            opened = true;
            open(mc);
            return;
        }
        if (Showcase.MODE.equals("check") && mc.player != null && Showcase.checkReport != null) {
            film(mc);
        }
    }

    private static void open(Minecraft mc) {
        if (Files.isDirectory(mc.gameDirectory.toPath().resolve("saves").resolve(Showcase.WORLD))) {
            mc.createWorldOpenFlows().openWorld(Showcase.WORLD, () -> mc.setScreen(new TitleScreen()));
            return;
        }
        LevelSettings settings = new LevelSettings(Showcase.WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
            new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(Showcase.WORLD, settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()
                .createWorldDimensions(),
            mc.screen);
    }

    // Viewpoints, relative to the spawn: an overview, then each tier.
    private static void plan() {
        BlockPos t1 = Showcase.TIER1;
        BlockPos t2 = Showcase.TIER2;
        BlockPos t3 = Showcase.TIER3;
        SHOTS.add(new Shot("showcase-overview", -4.5, 24, -14, 0, 42));
        SHOTS.add(new Shot("showcase-gallery", 0.5, 1.5, 2, 0, 5));
        SHOTS.add(new Shot("showcase-tier1", t1.getX() + 8.5, 6, t1.getZ() - 2, 45, 25));
        SHOTS.add(new Shot("showcase-tier2", t2.getX() - 6.5, 8, t2.getZ() - 3, -45, 25));
        SHOTS.add(new Shot("showcase-tier3", t3.getX() + 9.5, 10, t3.getZ() + 8, 110, 25));
    }

    private static void film(Minecraft mc) {
        if (!filming) {
            filming = true;
            mc.options.hideGui = true;
            plan();
            next(mc);
            return;
        }
        if (--wait > 0) {
            return;
        }
        Shot shot = SHOTS.poll();
        if (shot != null) {
            Screenshot.grab(mc.gameDirectory, shot.name() + ".png", mc.getMainRenderTarget(), message -> {
            });
            next(mc);
            return;
        }
        try {
            Files.writeString(mc.gameDirectory.toPath().resolve("showcase-report.txt"), Showcase.checkReport, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[showcase] could not write the report", e);
        }
        mc.stop();
    }

    // Moves the player (on the integrated server, so the chunks follow) to the next viewpoint.
    private static void next(Minecraft mc) {
        Shot shot = SHOTS.peek();
        if (shot == null) {
            wait = 1;
            return;
        }
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            var level = server.overworld();
            BlockPos o = level.getSharedSpawnPos();
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(level, o.getX() + shot.x(), o.getY() + shot.y(), o.getZ() + shot.z(), shot.yaw(), shot.pitch());
        });
        wait = SETTLE_TICKS;
    }
}
