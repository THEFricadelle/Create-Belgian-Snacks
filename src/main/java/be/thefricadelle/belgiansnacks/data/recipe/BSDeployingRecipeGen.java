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

import com.simibubi.create.api.data.recipe.DeployingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

// Deploying: the first ingredient is the item on the belt or depot, the second the one the deployer
// holds, used up unless the recipe keeps it.
public class BSDeployingRecipeGen extends DeployingRecipeGen {
    // V-D3: one portion of fries per cone; the cone is used up.
    GeneratedRecipe CONE_OF_FRIES = create("cone_of_fries", b -> b
        .require(BSTags.FRIES)
        .require(BSItems.PAPER_CONE.get())
        .output(BSItems.CONE_OF_FRIES.get()));

    // V-D4: beef tallow fries get a cone of their own.
    GeneratedRecipe CONE_OF_FRIES_BEEF_TALLOW = create("cone_of_fries_beef_tallow", b -> b
        .require(BSItems.FRIES_BEEF_TALLOW.get())
        .require(BSItems.PAPER_CONE.get())
        .output(BSItems.CONE_OF_FRIES_BEEF_TALLOW.get()));

    public BSDeployingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
