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

import com.simibubi.create.api.data.recipe.CuttingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class BSCuttingRecipeGen extends CuttingRecipeGen {
    // v2 (section 5.1): a mechanical saw cuts a potato into four portions of fries. Any mod's potato
    // in c:crops/potato works; NeoForge always puts the vanilla one in.
    GeneratedRecipe RAW_FRIES = create("raw_fries", b -> b
        .require(BSTags.C_POTATOES)
        .output(BSItems.RAW_FRIES.get(), 4)
        .duration(50));

    public BSCuttingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
