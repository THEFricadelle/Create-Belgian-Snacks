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

import com.simibubi.create.api.data.recipe.CompactingRecipeGen;

import com.simibubi.create.AllItems;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.world.item.Items;
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

    // v2 base ingredients (section 5.7); each recipe has an item, so none takes another line's milk.
    GeneratedRecipe PEARL_SUGAR = create("pearl_sugar", b -> b
        .require(Items.SUGAR).require(Items.SUGAR).require(Items.SUGAR).require(Items.SUGAR)
        .output(BSItems.PEARL_SUGAR.get(), 2));

    GeneratedRecipe YOUNG_HERVE_CHEESE = create("young_herve_cheese", b -> b
        .require(Tags.Fluids.MILK, 1000).require(BSTags.SALTS)
        .output(BSItems.YOUNG_HERVE_CHEESE.get()));

    GeneratedRecipe PUFF_PASTRY = create("puff_pastry", b -> b
        .require(AllItems.DOUGH.get()).require(BSTags.BUTTERS)
        .output(BSItems.PUFF_PASTRY.get()));

    GeneratedRecipe VANILLA_ICE_CREAM = create("vanilla_ice_cream", b -> b
        .require(BSFluids.CREAM.get(), 250).require(Items.SUGAR).require(Items.ICE)
        .output(BSItems.VANILLA_ICE_CREAM.get(), 2));

    public BSCompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
