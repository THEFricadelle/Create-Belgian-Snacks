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
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/** Models drawn on their own, rotated by Flywheel or the block entity renderer. */
public final class BSPartialModels {
    public static final PartialModel GRINDER_BLADES = PartialModel.of(BelgianSnacks.asResource("block/supreme_grinder/blades"));

    private BSPartialModels() {
    }

    public static void init() {
        // Loads the class so every partial above is known before models bake.
    }
}
