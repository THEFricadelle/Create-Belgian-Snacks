/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import java.util.concurrent.CompletableFuture;

import be.thefricadelle.belgiansnacks.data.recipe.BSCompactingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSCrushingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSFryingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSMechanicalCraftingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSMillingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSMixingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSPressingRecipeGen;
import be.thefricadelle.belgiansnacks.data.recipe.BSSequencedAssemblyRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class BSDatagen {
    private BSDatagen() {
    }

    // Registrate owns en_us; fr_fr has its own provider since Registrate only generates one locale.
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new BSFrenchLangProvider(output));
        generator.addProvider(event.includeClient(), new BSSpriteSourceProvider(output, registries, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new BSSoundDefinitionsProvider(output, event.getExistingFileHelper()));

        generator.addProvider(event.includeServer(), new BSCrushingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSMillingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSMixingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSCompactingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSPressingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSFryingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSMechanicalCraftingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new BSSequencedAssemblyRecipeGen(output, registries));
    }
}
