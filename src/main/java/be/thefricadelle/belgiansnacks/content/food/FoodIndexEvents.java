/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.network.FoodIndexSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** When the server recomputes the food index, and when it sends it to players. */
public final class FoodIndexEvents {
    private FoodIndexEvents() {
    }

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(FoodIndexEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(FoodIndexEvents::onTagsUpdated);
        NeoForge.EVENT_BUS.addListener(FoodIndexEvents::onLogin);
        modEventBus.addListener(FoodIndexEvents::onConfigReload);
    }

    // The server config is loaded by now; the tag-driven pass during startup ran with its defaults.
    private static void onServerStarted(ServerStartedEvent event) {
        FoodIndex.recompute(event.getServer());
    }

    // /reload and KubeJS tag edits. The client side of this event is ignored: clients get the list.
    private static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            FoodIndex.recompute(ServerLifecycleHooks.getCurrentServer());
        }
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == BSConfig.SPEC && ServerLifecycleHooks.getCurrentServer() != null) {
            FoodIndex.recompute(ServerLifecycleHooks.getCurrentServer());
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new FoodIndexSyncPayload(FoodIndex.server().ids()));
        }
    }
}
