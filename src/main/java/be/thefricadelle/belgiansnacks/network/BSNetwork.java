/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.network;

import be.thefricadelle.belgiansnacks.client.BSClientPayloads;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BSNetwork {
    // Bump when a payload's wire format changes, so mismatched client and server refuse to connect.
    private static final String PROTOCOL = "2";

    private static final double MISSING_REACH = 8;

    private BSNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(FoodIndexSyncPayload.TYPE, FoodIndexSyncPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> FoodIndex.acceptFromServer(payload.ids())));
        registrar.playToServer(GrinderMissingRequestPayload.TYPE, GrinderMissingRequestPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> answerMissing(payload, context.player())));
        // The handler lives in a client class; this lambda never runs, nor loads it, on a dedicated server.
        registrar.playToClient(GrinderMissingResponsePayload.TYPE, GrinderMissingResponsePayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> BSClientPayloads.openMissing(payload)));
    }

    // Only for a loaded grinder within reach: the list is not secret, but a client must not make the
    // server scan or load arbitrary positions.
    private static void answerMissing(GrinderMissingRequestPayload payload, Player player) {
        BlockPos pos = payload.pos();
        if (!(player instanceof ServerPlayer serverPlayer) || !player.level().isLoaded(pos)
            || player.distanceToSqr(Vec3.atCenterOf(pos)) > MISSING_REACH * MISSING_REACH
            || !(player.level().getBlockEntity(pos) instanceof SupremeGrinderBlockEntity grinder)) {
            return;
        }
        FoodIndex.Snapshot index = FoodIndex.server();
        PacketDistributor.sendToPlayer(serverPlayer, new GrinderMissingResponsePayload(pos, grinder.missing(index), index.size()));
    }
}
