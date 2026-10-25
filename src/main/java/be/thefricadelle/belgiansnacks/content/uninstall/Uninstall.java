/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.uninstall;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.compat.solonion.SolonionPurger;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Preparing a removal of the mod (spec v2, rule R4): the purgers of other mods' data, and the
 * preparation mode that purges each player when they log in.
 */
public final class Uninstall {
    private static final List<ReferencePurger> PURGERS = new CopyOnWriteArrayList<>();

    private Uninstall() {
    }

    public static void register() {
        if (ModList.get().isLoaded(SolonionPurger.MOD_ID)) {
            PURGERS.add(new SolonionPurger());
        }
        NeoForge.EVENT_BUS.addListener(Uninstall::onLogin);
    }

    public static List<ReferencePurger> purgers() {
        return List.copyOf(PURGERS);
    }

    /** Runs every purger on this player; returns how many entries of ours went. */
    public static int purge(ServerPlayer player) {
        int removed = 0;
        for (ReferencePurger purger : PURGERS) {
            try {
                removed += purger.purge(player);
            } catch (RuntimeException e) {
                BelgianSnacks.LOGGER.warn("Could not purge {} data of {}", purger.name(), player.getGameProfile().getName(), e);
            }
        }
        return removed;
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (BSConfig.uninstallPrepare() && event.getEntity() instanceof ServerPlayer player) {
            int removed = purge(player);
            BelgianSnacks.LOGGER.info("Removal preparation: {} reference(s) to this mod removed from {}'s data", removed,
                player.getGameProfile().getName());
        }
    }
}
