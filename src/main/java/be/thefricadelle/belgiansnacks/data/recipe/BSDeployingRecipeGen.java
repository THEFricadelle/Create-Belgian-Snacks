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
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSDishes;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

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

    // v2 friterie snacks (section 5.10).
    GeneratedRecipe PISTOLET_AMERICAIN = create("pistolet_americain", b -> b
        .require(BSItems.PISTOLET.get())
        .require(BSSnacks.FILET_AMERICAIN.get())
        .output(BSSnacks.PISTOLET_AMERICAIN.get()));

    GeneratedRecipe AMERICAIN_FRITES = create("americain_frites", b -> b
        .require(BSSnacks.PISTOLET_AMERICAIN.get())
        .require(BSTags.FRIES)
        .output(BSSnacks.AMERICAIN_FRITES.get()));

    GeneratedRecipe FRITERIE_CHEESEBURGER = create("friterie_cheeseburger", b -> b
        .require(BSSnacks.FRITERIE_BURGER.get())
        .require(BSTags.C_FOODS_CHEESE)
        .output(BSSnacks.FRITERIE_CHEESEBURGER.get()));

    // The deployer keeps its shears: one pair cuts every chicken.
    GeneratedRecipe RAW_CHICKEN_WINGS = create("raw_chicken_wings", b -> b
        .require(Items.CHICKEN)
        .require(Items.SHEARS)
        .output(BSSnacks.RAW_CHICKEN_WINGS.get(), 2)
        .toolNotConsumed());

    // v2 Belgian dishes (section 5.11): fries, a sausage or compote on a finished dish.
    GeneratedRecipe MOULES_FRITES = create("moules_frites", b -> b
        .require(BSDishes.MOULES_MARINIERES.get()).require(BSTags.FRIES).output(BSDishes.MOULES_FRITES.get()));

    GeneratedRecipe MOULES_FRITES_FROM_BEER = create("moules_frites_from_beer", b -> b
        .require(BSDishes.MOULES_A_LA_BIERE.get()).require(BSTags.FRIES).output(BSDishes.MOULES_FRITES.get()));

    GeneratedRecipe CARBONNADE_FRITES = create("carbonnade_frites", b -> b
        .require(BSDishes.CARBONNADE.get()).require(BSTags.FRIES).output(BSDishes.CARBONNADE_FRITES.get()));

    GeneratedRecipe STOEMP_SAUCISSE = create("stoemp_saucisse", b -> b
        .require(BSDishes.STOEMP.get()).require(BSTags.SAUSAGES).output(BSDishes.STOEMP_SAUCISSE.get()));

    GeneratedRecipe STEAK_FRITES = create("steak_frites", b -> b
        .require(Items.COOKED_BEEF).require(BSTags.FRIES).output(BSDishes.STEAK_FRITES.get()));

    GeneratedRecipe BOUDIN_COMPOTE = create("boudin_compote", b -> b
        .require(BSDishes.BOUDIN.get()).require(BSItems.APPLE_COMPOTE.get()).output(BSDishes.BOUDIN_COMPOTE.get()));

    // v2 waffles (section 5.6).
    GeneratedRecipe BRUSSELS_WAFFLE_SUGARED = create("brussels_waffle_sugared", b -> b
        .require(BSWaffles.BRUSSELS_WAFFLE.get()).require(BSTags.POWDERED_SUGARS).output(BSWaffles.BRUSSELS_WAFFLE_SUGARED.get()));

    public BSDeployingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }
}
