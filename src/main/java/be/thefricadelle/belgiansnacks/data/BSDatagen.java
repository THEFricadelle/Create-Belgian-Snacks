/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class BSDatagen {
    private BSDatagen() {
    }

    // Registrate owns en_us; fr_fr has its own provider since Registrate only generates one locale.
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeClient(),
            new BSFrenchLangProvider(event.getGenerator().getPackOutput()));
    }
}
