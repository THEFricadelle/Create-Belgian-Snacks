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

import com.simibubi.create.api.data.recipe.CrushingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

public class BSCrushingRecipeGen extends CrushingRecipeGen {
    GeneratedRecipe
        PORKCHOP = create("porkchop", b -> b.duration(150).require(Items.PORKCHOP).output(BSItems.MINCED_PORK.get(), 2)),
        BEEF = create("beef", b -> b.duration(150).require(Items.BEEF)
            .output(BSItems.MINCED_BEEF.get(), 2)
            .output(0.5f, BSItems.BEEF_TALLOW.get(), 1)),
        CHICKEN = create("chicken", b -> b.duration(150).require(Items.CHICKEN).output(BSItems.MINCED_CHICKEN.get(), 2)),
        BREAD = create("bread", b -> b.duration(100).require(Items.BREAD).output(BSItems.BREAD_CRUMBS.get(), 3));

    public BSCrushingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
