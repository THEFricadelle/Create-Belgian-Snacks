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

import com.simibubi.create.api.data.recipe.WashingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

// A Create fan blowing through water: the washed rind of Herve cheese, grain soaked until it sprouts.
public class BSWashingRecipeGen extends WashingRecipeGen {
    GeneratedRecipe HERVE_CHEESE = create("herve_cheese", b -> b
        .require(BSItems.YOUNG_HERVE_CHEESE.get())
        .output(BSItems.HERVE_CHEESE.get()));

    GeneratedRecipe SPROUTED_GRAIN = create("sprouted_grain", b -> b
        .require(BSTags.MALTING_GRAINS)
        .output(BSItems.SPROUTED_GRAIN.get()));

    public BSWashingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
