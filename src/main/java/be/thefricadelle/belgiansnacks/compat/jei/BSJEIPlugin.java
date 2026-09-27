/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.jei;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

/** Found by JEI's annotation scan, so it is only ever loaded when JEI is installed. */
@JeiPlugin
public class BSJEIPlugin implements IModPlugin {
    private CreateRecipeCategory<FryingRecipe> frying;

    @Override
    public ResourceLocation getPluginUid() {
        return BelgianSnacks.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        frying = new CreateRecipeCategory.Builder<FryingRecipe>(FryingRecipe.class)
            .addTypedRecipes(BSRecipeTypes.FRYING)
            .catalyst(BSBlocks.FRYER::get)
            .catalyst(AllBlocks.BLAZE_BURNER::get)
            .doubleItemIcon(BSBlocks.FRYER.get(), BSItems.FRICADELLE.get())
            .emptyBackground(177, 100)
            .build(BelgianSnacks.asResource("frying"), FryingCategory::new);
        registration.addRecipeCategories(frying, new GrindingGoalCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        frying.registerRecipes(registration);
        registration.addRecipes(GrindingGoalCategory.TYPE, List.of(GrinderMode.values()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        frying.registerCatalysts(registration);
        registration.addRecipeCatalyst(BSBlocks.SUPREME_GRINDER.asStack(), GrindingGoalCategory.TYPE);
    }
}
