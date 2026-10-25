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

import com.simibubi.create.AllItems;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
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
            .withCondition(new TagEmptyCondition(BSTags.C_ONIONS))),

        // v2 base ingredients (section 5.7). Every basin recipe here has an item: a fluid-only one would
        // take the milk or the water of any other line waiting for its items (docs/03, M13 rules).
        CASSONADE = create("cassonade", b -> b
            .require(Items.SUGAR).require(Items.SUGAR)
            .output(BSItems.CASSONADE.get())
            .requiresHeat(HeatCondition.HEATED)),

        // Sea salt from kelp boiled down; only where no mod of the pack makes salt already.
        SALT = create("salt", b -> b
            .require(Fluids.WATER, 500).require(Items.KELP)
            .output(BSItems.SALT.get())
            .requiresHeat(HeatCondition.HEATED)
            .withCondition(new TagEmptyCondition(BSTags.C_SALT))),

        CREAM = create("cream", b -> b
            .require(Tags.Fluids.MILK, 1000).require(BSTags.SALTS)
            .output(BSFluids.CREAM.get().getSource(), 250)),

        // Salted butter, churned from cream; packs with a butter of their own use theirs.
        BUTTER = create("butter", b -> b
            .require(BSFluids.CREAM.get(), 250).require(BSTags.SALTS)
            .output(BSItems.BUTTER.get(), 2)
            .withCondition(new TagEmptyCondition(BSTags.C_BUTTER))),

        MATON = create("maton", b -> b
            .require(Tags.Fluids.MILK, 500).require(BSFluids.VINEGAR.get(), 50).require(BSTags.SALTS)
            .output(BSItems.MATON.get())),

        VINEGAR = create("vinegar", b -> b
            .require(Items.APPLE).require(Items.APPLE).require(Items.APPLE).require(Fluids.WATER, 250)
            .output(BSFluids.VINEGAR.get().getSource(), 250)
            .duration(1200)),

        LIEGE_SYRUP = create("liege_syrup", b -> b
            .require(Items.APPLE).require(Items.APPLE).require(Items.APPLE).require(Items.SUGAR).require(Tags.Items.FOODS_BERRY)
            .output(BSFluids.LIEGE_SYRUP.get().getSource(), 250)
            .requiresHeat(HeatCondition.HEATED)),

        // Three items (flour, butter, the nutmeg of the spices): it beats the butter dough of other mods.
        BECHAMEL = create("bechamel", b -> b
            .require(Tags.Fluids.MILK, 500).require(AllItems.WHEAT_FLOUR.get()).require(BSTags.BUTTERS).require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.BECHAMEL.get().getSource(), 500)
            .requiresHeat(HeatCondition.HEATED)),

        SAUCE_LAPIN = create("sauce_lapin", b -> sauceLapin(b.require(BSTags.C_ONIONS))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_ONIONS)))),
        SAUCE_LAPIN_FROM_BEETROOT = create("sauce_lapin_from_beetroot", b -> sauceLapin(b.require(Items.BEETROOT))
            .withCondition(new TagEmptyCondition(BSTags.C_ONIONS))),

        BREWERS_YEAST = create("brewers_yeast", b -> b
            .require(Items.BREAD).require(Items.SUGAR).require(Fluids.WATER, 250)
            .output(BSItems.BREWERS_YEAST.get(), 2)),

        // Doughs start from Create's dough, never from flour and water: Create already mixes those into dough.
        BRIOCHE_DOUGH = create("brioche_dough", b -> b
            .require(AllItems.DOUGH.get()).require(BSTags.BUTTERS).require(Tags.Items.EGGS).require(Items.SUGAR)
            .require(BSItems.BREWERS_YEAST.get())
            .output(BSItems.BRIOCHE_DOUGH.get(), 2)),

        PITA_DOUGH = create("pita_dough", b -> b
            .require(AllItems.DOUGH.get()).require(BSItems.BREWERS_YEAST.get())
            .output(BSItems.PITA_DOUGH.get(), 2)),

        NOODLES = create("noodles", b -> b
            .require(AllItems.DOUGH.get()).require(Tags.Items.EGGS)
            .output(BSItems.NOODLES.get(), 2)),

        BRINED_HAM = create("brined_ham", b -> b
            .require(Items.PORKCHOP).require(BSTags.SALTS)
            .output(BSItems.BRINED_HAM.get())),

        RAW_MERINGUE = create("raw_meringue", b -> b
            .require(Tags.Items.EGGS).require(Items.SUGAR)
            .output(BSItems.RAW_MERINGUE.get(), 2)),

        // v2 crops (section 5.8): two powders, vinegar and water.
        MUSTARD = create("mustard", b -> b
            .require(BSCrops.MUSTARD_POWDER.get()).require(BSCrops.MUSTARD_POWDER.get())
            .require(BSFluids.VINEGAR.get(), 100).require(Fluids.WATER, 100)
            .output(BSFluids.MUSTARD.get().getSource(), 250)),

        // v2 cooking beers (section 5.9). Create's mixer takes processing_time into account, scaled by its
        // speed: 2400 ticks of gueuze last about 54 s at 64 rpm.
        WORT = create("wort", b -> b
            .require(BSItems.MALT.get()).require(BSItems.MALT.get()).require(BSCrops.HOPS.get()).require(Fluids.WATER, 1000)
            .output(BSFluids.WORT.get().getSource(), 1000)
            .requiresHeat(HeatCondition.HEATED)),

        BLONDE_BEER = create("blonde_beer", b -> b
            .require(BSFluids.WORT.get(), 1000).require(BSItems.BREWERS_YEAST.get())
            .output(BSFluids.BLONDE_BEER.get().getSource(), 1000)
            .duration(600)),

        // One item more than the blonde: with cassonade in the basin, Create makes the brown one.
        BROWN_BEER = create("brown_beer", b -> b
            .require(BSFluids.WORT.get(), 1000).require(BSItems.CASSONADE.get()).require(BSItems.BREWERS_YEAST.get())
            .output(BSFluids.BROWN_BEER.get().getSource(), 1000)
            .duration(600)),

        // Lambic is brewed with unmalted wheat and left to wild yeasts: no brewer's yeast, a long wait.
        GUEUZE = create("gueuze", b -> b
            .require(BSFluids.WORT.get(), 1000).require(Items.WHEAT)
            .output(BSFluids.GUEUZE.get().getSource(), 1000)
            .duration(2400)),

        KRIEK = create("kriek", b -> kriek(b, BSTags.CHERRIES)
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.CHERRIES)))),
        KRIEK_FROM_SWEET_BERRIES = create("kriek_from_sweet_berries", b -> kriek(b, null)
            .withCondition(new TagEmptyCondition(BSTags.CHERRIES))),

        // A vinegar mother turns beer sour: an apple stands in for it.
        VINEGAR_FROM_BEER = create("vinegar_from_beer", b -> b
            .require(BSFluids.BLONDE_BEER.get(), 1000).require(Items.APPLE)
            .output(BSFluids.VINEGAR.get().getSource(), 1000)
            .duration(2400)),

        // v2 sauces that waited for vinegar and mustard (M16).
        TARTARE = create("tartare", b -> b
            .require(BSFluids.MAYONNAISE.get(), 250).require(BSFluids.PICKLES.get(), 50).require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.TARTARE.get().getSource(), 300)),

        // Piccalilli: vegetables in a mustard vinegar, cooked.
        PICKLES = create("pickles", b -> b
            .require(BSFluids.VINEGAR.get(), 250).require(BSFluids.MUSTARD.get(), 50)
            .require(Items.CARROT).require(BSCrops.BRUSSELS_SPROUT.get()).require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.PICKLES.get().getSource(), 250)
            .requiresHeat(HeatCondition.HEATED)),

        BRAZIL = create("brazil", b -> brazil(b.require(BSTags.PINEAPPLES))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.PINEAPPLES)))),
        BRAZIL_FROM_MELON = create("brazil_from_melon", b -> brazil(b.require(Items.MELON_SLICE))
            .withCondition(new TagEmptyCondition(BSTags.PINEAPPLES))),

        // v2 friterie snacks (section 5.10). No egg in any of these basins: Create: Food turns a lone egg
        // into liquid egg in any basin (docs/11). Overlaps inside our menu follow the M13 rule 3: the
        // recipe with more items wins, so the boulet paste (five) beats the cervelas mix (three).
        BOULET_PASTE = create("boulet_paste", b -> b
            .require(BSTags.MINCED_PORK).require(BSTags.MINCED_BEEF).require(BSTags.C_BREAD_CRUMBS)
            .require(BSItems.BELGIAN_SPICES.get()).require(BSTags.SALTS)
            .output(BSSnacks.BOULET_PASTE.get(), 2)),

        BOULETS_LIEGEOIS = create("boulets_liegeois", b -> b
            .require(BSSnacks.BOULET.get()).require(BSSnacks.BOULET.get()).require(Items.BOWL).require(BSFluids.SAUCE_LAPIN.get(), 250)
            .output(BSSnacks.BOULETS_LIEGEOIS.get())
            .requiresHeat(HeatCondition.HEATED)),

        SHRIMP_CROQUETTE_MIX = create("shrimp_croquette_mix", b -> shrimpCroquette(b.require(BSTags.SHRIMPS).require(BSTags.SHRIMPS))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.SHRIMPS)))),
        // Grey shrimps are a North Sea thing; packs without any get cod croquettes.
        SHRIMP_CROQUETTE_MIX_FROM_COD = create("shrimp_croquette_mix_from_cod", b -> shrimpCroquette(b.require(Items.COD).require(Items.COD))
            .withCondition(new TagEmptyCondition(BSTags.SHRIMPS))),

        CHEESE_CROQUETTE_MIX = create("cheese_croquette_mix", b -> b
            .require(BSTags.C_FOODS_CHEESE).require(BSFluids.BECHAMEL.get(), 250)
            .output(BSSnacks.CHEESE_CROQUETTE_MIX.get(), 2)),

        CHICKEN_CROQUETTE_MIX = create("chicken_croquette_mix", b -> b
            .require(BSTags.MINCED_CHICKEN).require(BSTags.MINCED_CHICKEN).require(BSFluids.BECHAMEL.get(), 250)
            .output(BSSnacks.CHICKEN_CROQUETTE_MIX.get(), 2)),

        LUCIFER_MIX = create("lucifer_mix", b -> b
            .require(BSTags.MINCED_PORK).require(BSFluids.SAMOURAI.get(), 50).require(BSFluids.BECHAMEL.get(), 100)
            .output(BSSnacks.LUCIFER_MIX.get(), 2)),

        POTATO_CROQUETTE_MIX = create("potato_croquette_mix", b -> b
            .require(BSTags.C_POTATOES).require(BSTags.C_POTATOES).require(BSTags.BUTTERS).require(BSTags.SALTS)
            .output(BSSnacks.POTATO_CROQUETTE_MIX.get(), 2)
            .requiresHeat(HeatCondition.HEATED)),

        CERVELAS_MIX = create("cervelas_mix", b -> b
            .require(BSTags.MINCED_PORK).require(BSTags.MINCED_BEEF).require(BSItems.BELGIAN_SPICES.get())
            .output(BSSnacks.CERVELAS_MIX.get(), 2)),

        MEXICANO_MIX = create("mexicano_mix", b -> b
            .require(BSTags.MINCED_BEEF).require(BSTags.MINCED_BEEF).require(BSItems.BELGIAN_SPICES.get()).require(BSFluids.SAMOURAI.get(), 50)
            .output(BSSnacks.MEXICANO_MIX.get(), 2)),

        // Five items, like the boulet paste: neither hides inside the fricadelle paste (four).
        VIANDELLE_MIX = create("viandelle_mix", b -> b
            .require(BSTags.MINCED_PORK).require(BSTags.MINCED_CHICKEN).require(BSTags.C_BREAD_CRUMBS)
            .require(BSItems.BELGIAN_SPICES.get()).require(BSTags.SALTS)
            .output(BSSnacks.VIANDELLE_MIX.get(), 2)),

        FILET_AMERICAIN = create("filet_americain", b -> b
            .require(BSTags.MINCED_BEEF).require(BSTags.MINCED_BEEF).require(BSItems.BELGIAN_SPICES.get())
            .require(BSFluids.MAYONNAISE.get(), 100).require(BSFluids.MUSTARD.get(), 25)
            .output(BSSnacks.FILET_AMERICAIN.get(), 2)),

        GYROS = create("gyros", b -> b
            .require(BSTags.MINCED_PORK).require(BSTags.MINCED_PORK).require(BSItems.BELGIAN_SPICES.get())
            .output(BSSnacks.GYROS.get(), 2)
            .requiresHeat(HeatCondition.HEATED)),

        BAMI_MIX = create("bami_mix", b -> b
            .require(BSTags.NOODLES).require(BSTags.NOODLES).require(BSTags.MINCED_PORK).require(BSCrops.LEEK.get())
            .require(BSItems.BELGIAN_SPICES.get())
            .output(BSSnacks.BAMI_MIX.get(), 2)),

        // Cold on purpose: in Arcadia a heated basin cooks lone rice into a fluid first.
        NASI_MIX = create("nasi_mix", b -> nasi(b.require(BSTags.C_RICE).require(BSTags.C_RICE))
            .withCondition(new NotCondition(new TagEmptyCondition(BSTags.C_RICE)))),
        NASI_MIX_FROM_WHEAT = create("nasi_mix_from_wheat", b -> nasi(b.require(Items.WHEAT).require(Items.WHEAT))
            .withCondition(new TagEmptyCondition(BSTags.C_RICE))),

        RAW_CHICKEN_NUGGETS = create("raw_chicken_nuggets", b -> b
            .require(BSTags.MINCED_CHICKEN).require(BSTags.MINCED_CHICKEN).require(BSTags.C_BREAD_CRUMBS).require(BSTags.SALTS)
            .output(BSSnacks.RAW_CHICKEN_NUGGETS.get(), 4)),

        RAW_CHICKEN_CRUNCH = create("raw_chicken_crunch", b -> b
            .require(BSTags.MINCED_CHICKEN).require(BSTags.MINCED_CHICKEN).require(BSItems.BELGIAN_SPICES.get())
            .require(BSSnacks.CORN_FLAKES.get()).require(BSSnacks.CORN_FLAKES.get())
            .output(BSSnacks.RAW_CHICKEN_CRUNCH.get(), 2)),
        // No corn to flake: bread crumbs make the crust.
        RAW_CHICKEN_CRUNCH_FROM_BREAD_CRUMBS = create("raw_chicken_crunch_from_bread_crumbs", b -> b
            .require(BSTags.MINCED_CHICKEN).require(BSTags.MINCED_CHICKEN).require(BSItems.BELGIAN_SPICES.get())
            .require(BSTags.C_BREAD_CRUMBS).require(BSTags.C_BREAD_CRUMBS)
            .output(BSSnacks.RAW_CHICKEN_CRUNCH.get(), 2)
            .withCondition(new TagEmptyCondition(BSTags.C_CORN))),

        BATTERED_ONION_RINGS = create("battered_onion_rings", b -> b
            .require(BSSnacks.RAW_ONION_RINGS.get()).require(BSSnacks.RAW_ONION_RINGS.get()).require(BSSnacks.RAW_ONION_RINGS.get())
            .require(AllItems.WHEAT_FLOUR.get()).require(BSFluids.BLONDE_BEER.get(), 250)
            .output(BSSnacks.BATTERED_ONION_RINGS.get(), 3)),

        APPLE_COMPOTE = create("apple_compote", b -> b
            .require(Items.APPLE).require(Items.APPLE).require(Items.APPLE).require(Items.SUGAR).require(Items.BOWL)
            .output(BSItems.APPLE_COMPOTE.get())
            .requiresHeat(HeatCondition.HEATED));

    public BSMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> kriek(StandardProcessingRecipe.Builder<MixingRecipe> b,
                                                                       net.minecraft.tags.TagKey<net.minecraft.world.item.Item> cherries) {
        b.require(BSFluids.GUEUZE.get(), 1000);
        for (int i = 0; i < 4; i++) {
            if (cherries == null) {
                b.require(Items.SWEET_BERRIES);
            } else {
                b.require(cherries);
            }
        }
        return b.output(BSFluids.KRIEK.get().getSource(), 1000).duration(600);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> brazil(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSFluids.MAYONNAISE.get(), 250)
            .require(BSFluids.CURRY_KETCHUP.get(), 50)
            .output(BSFluids.BRAZIL.get().getSource(), 300);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> shrimpCroquette(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSFluids.BECHAMEL.get(), 250).output(BSSnacks.SHRIMP_CROQUETTE_MIX.get(), 2);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> nasi(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSTags.MINCED_PORK).require(BSCrops.LEEK.get()).require(BSItems.BELGIAN_SPICES.get())
            .output(BSSnacks.NASI_MIX.get(), 2);
    }

    private static StandardProcessingRecipe.Builder<MixingRecipe> sauceLapin(StandardProcessingRecipe.Builder<MixingRecipe> b) {
        return b.require(BSFluids.LIEGE_SYRUP.get(), 250)
            .require(BSItems.BELGIAN_SPICES.get())
            .output(BSFluids.SAUCE_LAPIN.get().getSource(), 250)
            .requiresHeat(HeatCondition.HEATED);
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
