/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.CompactingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;

public class BSCompactingRecipeGen extends CompactingRecipeGen {
    // Both mods already compact #c:seeds into their own oil: a third recipe on the same input would
    // make the basin pick one arbitrarily. With them loaded, their oils count as frying oil (D13).
    GeneratedRecipe FRYING_OIL_FROM_SEEDS = create("frying_oil_from_seeds", b -> {
        for (int i = 0; i < 8; i++) {
            b.require(Tags.Items.SEEDS);
        }
        return b.output(BSFluids.FRYING_OIL.get().getSource(), 100)
            .whenModMissing("createaddition")
            .whenModMissing("createdieselgenerators");
    });

    public BSCompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
