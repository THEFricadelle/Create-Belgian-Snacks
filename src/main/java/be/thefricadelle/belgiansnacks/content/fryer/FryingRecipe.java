/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.fryer;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * One item fried in a fat. The fat is the recipe's single fluid ingredient, its amount is per item,
 * and heat_requirement / processing_time are Create's own fields.
 */
public class FryingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {
    public FryingRecipe(ProcessingRecipeParams params) {
        super(BSRecipeTypes.FRYING, params);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return !input.isEmpty() && ingredients.get(0).test(input.getItem(0));
    }

    /** The fat and its amount per fried item, or null for a recipe that needs none. */
    public SizedFluidIngredient getFat() {
        return fluidIngredients.isEmpty() ? null : fluidIngredients.get(0);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }
}
