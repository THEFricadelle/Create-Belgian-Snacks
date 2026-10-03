/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import be.thefricadelle.belgiansnacks.network.GrinderMissingResponsePayload;
import net.minecraft.client.Minecraft;

/** Client ends of server payloads, kept out of the common network class. */
public final class BSClientPayloads {
    private BSClientPayloads() {
    }

    public static void openMissing(GrinderMissingResponsePayload payload) {
        Minecraft.getInstance().setScreen(new GrinderMissingScreen(payload));
    }
}
