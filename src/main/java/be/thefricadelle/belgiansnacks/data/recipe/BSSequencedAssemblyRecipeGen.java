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
 * ketchup, onion and a press, three times over (D4), never failing (D5). Tier 3: the Absolute Paste
 * becomes a raw THE_FRICADELLE through the same belt with beef tallow first, five times over.
 */
public class BSSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {
    static final int LOOPS = 3;
    static final int SAUCE_PER_STEP = 100;
    static final int ULTIMATE_LOOPS = 5;
    static final int ULTIMATE_FLUID_PER_STEP = 250;

    GeneratedRecipe
        RAW_THE_FRICADELLE = rawTheFricadelle("raw_the_fricadelle", Ingredient.of(BSTags.C_ONIONS),
            new NotCondition(new TagEmptyCondition(BSTags.C_ONIONS))),

        // Fallback (D7) for packs without any onion; Arcadia has onions and never loads it.
        RAW_THE_FRICADELLE_FROM_BEETROOT = rawTheFricadelle("raw_the_fricadelle_from_beetroot", Ingredient.of(Items.BEETROOT),
            new TagEmptyCondition(BSTags.C_ONIONS)),

        // Tier 3 (27/09/2026): the tier 2 sequence with beef tallow first, bigger servings, five loops.
        RAW_ULTIMATE_FRICADELLE = rawUltimateFricadelle("raw_ultimate_fricadelle", Ingredient.of(BSTags.C_ONIONS),
            new NotCondition(new TagEmptyCondition(BSTags.C_ONIONS))),

        RAW_ULTIMATE_FRICADELLE_FROM_BEETROOT = rawUltimateFricadelle("raw_ultimate_fricadelle_from_beetroot", Ingredient.of(Items.BEETROOT),
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

    private GeneratedRecipe rawUltimateFricadelle(String name, Ingredient onion, ICondition condition) {
        return register(output -> new SequencedAssemblyRecipeBuilder(asResource(name))
            .require(BSItems.ABSOLUTE_PASTE.get())
            .transitionTo(BSItems.INCOMPLETE_ULTIMATE_FRICADELLE.get())
            .addOutput(BSItems.RAW_ULTIMATE_FRICADELLE.get(), 1)
            .loops(ULTIMATE_LOOPS)
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.MELTED_BEEF_TALLOW.get(), ULTIMATE_FLUID_PER_STEP))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSItems.BELGIAN_SPICES.get()))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.MAYONNAISE.get(), ULTIMATE_FLUID_PER_STEP))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.CURRY_KETCHUP.get(), ULTIMATE_FLUID_PER_STEP))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(onion))
            .addStep(PressingRecipe::new, rb -> rb)
            .build(output.withConditions(condition)));
    }
}
