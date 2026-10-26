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
import be.thefricadelle.belgiansnacks.content.waffle.WaffleBakingRecipe;
import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

/** v2 waffles (section 5.6): every batter bakes on a lit burner; the thick Liège dough takes longest. */
public class BSWaffleBakingRecipeGen extends StandardProcessingRecipeGen<WaffleBakingRecipe> {
    GeneratedRecipe LIEGE_WAFFLE = create("liege_waffle", b -> b
        .require(BSWaffles.LIEGE_WAFFLE_DOUGH.get())
        .output(BSWaffles.LIEGE_WAFFLE.get())
        .duration(200)
        .requiresHeat(HeatCondition.HEATED));

    GeneratedRecipe BRUSSELS_WAFFLE = create("brussels_waffle", b -> b
        .require(BSWaffles.BRUSSELS_WAFFLE_BATTER.get())
        .output(BSWaffles.BRUSSELS_WAFFLE.get())
        .duration(160)
        .requiresHeat(HeatCondition.HEATED));

    GeneratedRecipe LACQUEMANT_WAFER = create("lacquemant_wafer", b -> b
        .require(BSWaffles.LACQUEMANT_BATTER.get())
        .output(BSWaffles.LACQUEMANT_WAFER.get())
        .duration(120)
        .requiresHeat(HeatCondition.HEATED));

    public BSWaffleBakingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return BSRecipeTypes.WAFFLE_BAKING;
    }
}
