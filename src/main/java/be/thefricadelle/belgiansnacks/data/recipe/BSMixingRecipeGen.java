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

import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

public class BSMixingRecipeGen extends MixingRecipeGen {
    GeneratedRecipe
        // Tags, not items: any mod's minced meat and bread crumbs work.
        FRICADELLE_PASTE = create("fricadelle_paste", b -> b
            .require(BSTags.MINCED_PORK)
            .require(BSTags.MINCED_BEEF)
            .require(BSTags.MINCED_CHICKEN)
            .require(BSTags.C_BREAD_CRUMBS)
            .output(BSItems.FRICADELLE_PASTE.get(), 2)),

        MELTED_BEEF_TALLOW = create("melted_beef_tallow", b -> b
            .require(BSItems.BEEF_TALLOW.get())
            .require(BSItems.BEEF_TALLOW.get())
            .output(BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 250)
            .requiresHeat(HeatCondition.HEATED)),

        MAYONNAISE = create("mayonnaise", b -> b
            .require(Tags.Items.EGGS)
            .require(BSTags.FRYING_OILS, 100)
            .output(BSFluids.MAYONNAISE.get().getSource(), 250)),

        CURRY_KETCHUP = create("curry_ketchup", b -> curryKetchup(b.require(BSTags.C_TOMATOES))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_TOMATOES)))),

        // Fallback (D7, provisional) for packs without any tomato.
        CURRY_KETCHUP_FROM_BEETROOT = create("curry_ketchup_from_beetroot", b -> curryKetchup(b.require(Items.BEETROOT))
            .withCondition(new TagEmptyCondition(BSTags.C_TOMATOES))),

        // v2 sauces (section 5.2), each with a vanilla twin for packs without the ingredient.
        ANDALOUSE = create("andalouse", b -> andalouse(b.require(BSTags.C_TOMATOES))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_TOMATOES)))),
        ANDALOUSE_FROM_BEETROOT = create("andalouse_from_beetroot", b -> andalouse(b.require(Items.BEETROOT))
            .withCondition(new TagEmptyCondition(BSTags.C_TOMATOES))),

        SAMOURAI = create("samourai", b -> samourai(b.require(BSTags.HOT_PEPPERS))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.HOT_PEPPERS)))),
        // V-D5: no pepper in the pack, blaze powder brings the burn.
        SAMOURAI_FROM_BLAZE_POWDER = create("samourai_from_blaze_powder", b -> samourai(b.require(Items.BLAZE_POWDER))
            .withCondition(new TagEmptyCondition(BSTags.HOT_PEPPERS))),

        AMERICAINE = create("americaine", b -> americaine(b.require(BSTags.C_ONIONS))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_ONIONS)))),
        AMERICAINE_FROM_BEETROOT = create("americaine_from_beetroot", b -> americaine(b.require(Items.BEETROOT))
            .withCondition(new TagEmptyCondition(BSTags.C_ONIONS)));

    public BSMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> andalouse(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSFluids.MAYONNAISE.get(), 250)
            .require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.ANDALOUSE.get().getSource(), 250);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> samourai(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSFluids.MAYONNAISE.get(), 250)
            .require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.SAMOURAI.get().getSource(), 250)
            .requiresHeat(HeatCondition.HEATED);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> americaine(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSFluids.MAYONNAISE.get(), 250)
            .require(BSFluids.CURRY_KETCHUP.get(), 50)
            .output(BSFluids.AMERICAINE.get().getSource(), 300);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> curryKetchup(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(Items.SUGAR)
            .require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.CURRY_KETCHUP.get().getSource(), 250)
            .requiresHeat(HeatCondition.HEATED);
    }
}
