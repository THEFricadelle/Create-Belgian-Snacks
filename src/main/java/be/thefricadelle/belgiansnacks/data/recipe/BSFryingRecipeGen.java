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

import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class BSFryingRecipeGen extends StandardProcessingRecipeGen<FryingRecipe> {
    // The fat amount is per fried item: a batch of 16 uses 160 mB.
    GeneratedRecipe FRICADELLE = create("fricadelle", b -> b
        .require(BSItems.RAW_FRICADELLE.get())
        .require(BSTags.FRYING_OILS, 10)
        .output(BSItems.FRICADELLE.get())
        .duration(100)
        .requiresHeat(HeatCondition.HEATED));

    // Tier 2 (D20): any frying fat, heated, twice as long and more fat than tier 1.
    GeneratedRecipe THE_FRICADELLE = create("the_fricadelle", b -> b
        .require(BSItems.RAW_THE_FRICADELLE.get())
        .require(BSTags.FRYING_OILS, 25)
        .output(BSItems.THE_FRICADELLE.get())
        .duration(200)
        .requiresHeat(HeatCondition.HEATED));

    // Tier 3: beef tallow only, superheated, one at a time (the raw item carries fryer/one_at_a_time).
    GeneratedRecipe ULTIMATE_FRICADELLE = create("ultimate_fricadelle", b -> b
        .require(BSItems.RAW_ULTIMATE_FRICADELLE.get())
        .require(BSFluids.MELTED_BEEF_TALLOW.get(), 250)
        .output(BSItems.ULTIMATE_FRICADELLE.get())
        .duration(600)
        .requiresHeat(HeatCondition.SUPERHEATED));

    public BSFryingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return BSRecipeTypes.FRYING;
    }
}
