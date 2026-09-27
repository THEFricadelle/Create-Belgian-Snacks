/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

/**
 * Tier 2: the Exceptional Paste becomes a raw THE_Fricadelle on a belt, spices, mayonnaise, curry
 * ketchup, onion and a press, three times over (D4), never failing (D5).
 */
public class BSSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {
    static final int LOOPS = 3;
    static final int SAUCE_PER_STEP = 100;

    GeneratedRecipe
        RAW_THE_FRICADELLE = rawTheFricadelle("raw_the_fricadelle", Ingredient.of(BSTags.C_ONIONS),
            new NotCondition(new TagEmptyCondition(BSTags.C_ONIONS))),

        // Fallback (D7) for packs without any onion; Arcadia has onions and never loads it.
        RAW_THE_FRICADELLE_FROM_BEETROOT = rawTheFricadelle("raw_the_fricadelle_from_beetroot", Ingredient.of(Items.BEETROOT),
            new TagEmptyCondition(BSTags.C_ONIONS));

    public BSSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    // Create's builder cannot carry conditions; the recipe output adds them.
    private GeneratedRecipe rawTheFricadelle(String name, Ingredient onion, ICondition condition) {
        return register(output -> new SequencedAssemblyRecipeBuilder(asResource(name))
            .require(BSItems.EXCEPTIONAL_PASTE.get())
            .transitionTo(BSItems.INCOMPLETE_THE_FRICADELLE.get())
            .addOutput(BSItems.RAW_THE_FRICADELLE.get(), 1)
            .loops(LOOPS)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSItems.BELGIAN_SPICES.get()))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.MAYONNAISE.get(), SAUCE_PER_STEP))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.CURRY_KETCHUP.get(), SAUCE_PER_STEP))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(onion))
            .addStep(PressingRecipe::new, rb -> rb)
            .build(output.withConditions(condition)));
    }
}
