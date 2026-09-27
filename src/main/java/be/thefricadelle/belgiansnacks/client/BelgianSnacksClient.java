/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Client-only entry point, constructed after the common one on a client. */
@Mod(value = BelgianSnacks.MOD_ID, dist = Dist.CLIENT)
public class BelgianSnacksClient {
    public BelgianSnacksClient(IEventBus modEventBus) {
        BSPartialModels.init();
    }
}
