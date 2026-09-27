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

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class BSMechanicalCraftingRecipeGen extends MechanicalCraftingRecipeGen {
    // Provisional (D11): a vat (basin), a fat tank, copper walls and an iron basket, on a mechanical
    // crafter. The id is stable so a pack can replace it with KubeJS.
    GeneratedRecipe FRYER = create(BSBlocks.FRYER::get).recipe(b -> b
        .key('C', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/copper")))
        .key('I', Items.IRON_BARS)
        .key('T', AllBlocks.FLUID_TANK.get())
        .key('B', AllBlocks.BASIN.get())
        .key('P', AllItems.PRECISION_MECHANISM.get())
        .patternLine("CIC")
        .patternLine("TBP"));

    // D11: two crushing wheels in a brass casing, costly but reachable once brass is automated.
    GeneratedRecipe SUPREME_GRINDER = create(BSBlocks.SUPREME_GRINDER::get).recipe(b -> b
        .key('S', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/brass")))
        .key('M', AllItems.PRECISION_MECHANISM.get())
        .key('W', AllBlocks.CRUSHING_WHEEL.get())
        .key('C', AllBlocks.BRASS_CASING.get())
        .key('G', Tags.Items.INGOTS_GOLD)
        .key('I', Tags.Items.STORAGE_BLOCKS_IRON)
        .patternLine("SMS")
        .patternLine("WCW")
        .patternLine("GIG"));

    public BSMechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
