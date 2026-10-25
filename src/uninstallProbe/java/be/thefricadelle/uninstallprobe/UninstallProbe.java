/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.uninstallprobe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The removal smoke run's witness (spec v2, rule R5), with no class of Create: Belgian Snacks: the
 * client opens the quick-play world, this waits until the player stands in it, checks whether the
 * mod is present as the phase expects, and quits so the world is saved. A world that cannot be
 * joined leaves no report, or a FAIL line naming the screen it stopped on.
 */
@Mod(value = "uninstall_probe", dist = Dist.CLIENT)
public final class UninstallProbe {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MOD_NAMESPACE = "create_belgian_snacks";
    // "without": the mod must be absent; "back": it must be loaded again.
    private static final String PHASE = System.getProperty("uninstall_probe.phase", "without");
    private static final int IN_WORLD_TICKS = 100;
    private static final int TIMEOUT_TICKS = 20 * 60 * 4;

    private final Set<String> screens = new LinkedHashSet<>();
    private int ticks;
    private int inWorld;
    private boolean done;

    public UninstallProbe() {
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        if (done) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (mc.screen != null) {
            screens.add(mc.screen.getClass().getName());
        }
        boolean joined = mc.player != null && mc.level != null && mc.getSingleplayerServer() != null && mc.screen == null;
        inWorld = joined ? inWorld + 1 : 0;
        if (inWorld >= IN_WORLD_TICKS) {
            finish(mc, true);
        } else if (ticks > TIMEOUT_TICKS) {
            finish(mc, false);
        }
    }

    private void finish(Minecraft mc, boolean joined) {
        done = true;
        List<String> lines = new ArrayList<>();
        int failures = 0;
        lines.add("phase " + PHASE);
        lines.add("screens seen: " + screens);
        if (joined) {
            lines.add("PASS joined - " + mc.player.getGameProfile().getName() + " at " + mc.player.blockPosition().toShortString()
                + ", " + mc.player.getInventory().items.stream().filter(stack -> !stack.isEmpty()).count() + " stacks in the inventory");
        } else {
            failures++;
            lines.add("FAIL joined - still not in the world after " + ticks + " ticks, on " + (mc.screen == null ? "no screen" : mc.screen.getClass().getName()));
        }
        boolean loaded = BuiltInRegistries.ITEM.keySet().stream().anyMatch(id -> id.getNamespace().equals(MOD_NAMESPACE));
        boolean expected = PHASE.equals("back");
        if (loaded == expected) {
            lines.add("PASS mod " + (loaded ? "loaded" : "absent"));
        } else {
            failures++;
            lines.add("FAIL mod - " + (loaded ? "loaded" : "absent") + " in phase " + PHASE);
        }
        lines.add(failures == 0 ? "RESULT PASS" : "RESULT FAIL " + failures);
        Path report = FMLPaths.GAMEDIR.get().resolve("uninstall-probe-report.txt");
        try {
            Files.write(report, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[uninstall-probe] could not write {}", report, e);
        }
        LOGGER.info("[uninstall-probe] {}", lines);
        // Save and quit, as a player leaving the world: the next phase opens what this one saved.
        if (mc.level != null) {
            mc.level.disconnect();
            mc.disconnect();
        }
        mc.stop();
    }
}
