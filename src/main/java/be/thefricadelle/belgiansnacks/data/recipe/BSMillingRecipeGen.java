/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.MillingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

public class BSMillingRecipeGen extends MillingRecipeGen {
    // Provisional (D6): dried kelp stands in for the salt of the spice blend.
    GeneratedRecipe DRIED_KELP = create("dried_kelp", b -> b.duration(100).require(Items.DRIED_KELP)
        .output(BSItems.BELGIAN_SPICES.get())
        .output(0.25f, BSItems.BELGIAN_SPICES.get(), 1));

    public BSMillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
