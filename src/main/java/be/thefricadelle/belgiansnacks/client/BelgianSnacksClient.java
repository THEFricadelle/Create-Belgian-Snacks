/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.client.ponder.BSPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only entry point, constructed after the common one on a client. */
@Mod(value = BelgianSnacks.MOD_ID, dist = Dist.CLIENT)
public class BelgianSnacksClient {
    public BelgianSnacksClient(IEventBus modEventBus) {
        BSPartialModels.init();
        // As Create does: Ponder plugins are added once the client is set up.
        modEventBus.addListener((FMLClientSetupEvent event) -> PonderIndex.addPlugin(new BSPonderPlugin()));
    }
}
