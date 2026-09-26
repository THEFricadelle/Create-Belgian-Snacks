/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.network;

import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BSNetwork {
    // Bump when a payload's wire format changes, so mismatched client and server refuse to connect.
    private static final String PROTOCOL = "1";

    private BSNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(FoodIndexSyncPayload.TYPE, FoodIndexSyncPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> FoodIndex.acceptFromServer(payload.ids())));
    }
}
