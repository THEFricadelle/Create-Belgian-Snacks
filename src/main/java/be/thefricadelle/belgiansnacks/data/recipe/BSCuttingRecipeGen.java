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

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.CuttingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

public class BSCuttingRecipeGen extends CuttingRecipeGen {
    // v2 (section 5.1): a mechanical saw cuts a potato into four portions of fries. Any mod's potato
    // in c:crops/potato works; NeoForge always puts the vanilla one in.
    GeneratedRecipe RAW_FRIES = create("raw_fries", b -> b
        .require(BSTags.C_POTATOES)
        .output(BSItems.RAW_FRIES.get(), 4)
        .duration(50));

    // v2 base ingredients (section 5.7): doughs cut into rolls, pork into lardons.
    GeneratedRecipe PISTOLET_DOUGH = create("pistolet_dough", b -> b
        .require(AllItems.DOUGH.get()).output(BSItems.PISTOLET_DOUGH.get(), 2).duration(50));

    GeneratedRecipe BURGER_BUN_DOUGH = create("burger_bun_dough", b -> b
        .require(BSItems.BRIOCHE_DOUGH.get()).output(BSItems.BURGER_BUN_DOUGH.get(), 2).duration(50));

    GeneratedRecipe LARDONS = create("lardons", b -> b
        .require(Items.PORKCHOP).output(BSItems.LARDONS.get(), 3).duration(50));

    // v2 friterie snacks (section 5.10).
    GeneratedRecipe
        HALF_BAGUETTE = create("half_baguette", b -> b.require(Items.BREAD).output(BSSnacks.HALF_BAGUETTE.get(), 2).duration(50)),
        CHICKEN_STRIPS = create("chicken_strips", b -> b.require(Items.CHICKEN).output(BSSnacks.CHICKEN_STRIPS.get(), 3).duration(50)),
        FISH_STRIPS = create("fish_strips", b -> b.require(Ingredient.of(Items.COD, Items.SALMON)).output(BSSnacks.FISH_STRIPS.get(), 3).duration(50)),
        CHEESE_STRIPS = create("cheese_strips", b -> b.require(BSTags.C_FOODS_CHEESE).output(BSSnacks.CHEESE_STRIPS.get(), 3).duration(50)),
        RAW_SQUID_RINGS = create("raw_squid_rings", b -> b.require(BSTags.SQUIDS).output(BSSnacks.RAW_SQUID_RINGS.get(), 3).duration(50)
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.SQUIDS)))),
        RAW_ONION_RINGS = create("raw_onion_rings", b -> b.require(BSTags.C_ONIONS).output(BSSnacks.RAW_ONION_RINGS.get(), 3).duration(50)
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_ONIONS)))),
        // D7: beetroot stands in for onions in packs without any.
        RAW_ONION_RINGS_FROM_BEETROOT = create("raw_onion_rings_from_beetroot", b -> b.require(Items.BEETROOT)
            .output(BSSnacks.RAW_ONION_RINGS.get(), 3).duration(50)
            .withCondition(new TagEmptyCondition(BSTags.C_ONIONS))),
        // Without corn, the chicken crunch takes bread crumbs (mixing/raw_chicken_crunch_from_bread_crumbs).
        CORN_FLAKES = create("corn_flakes", b -> b.require(BSTags.C_CORN).output(BSSnacks.CORN_FLAKES.get(), 2).duration(50)
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_CORN))));

    public BSCuttingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
