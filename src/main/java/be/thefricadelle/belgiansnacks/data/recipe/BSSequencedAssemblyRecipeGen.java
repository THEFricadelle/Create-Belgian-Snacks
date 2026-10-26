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
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSDishes;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
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

    // v2 friterie snacks (section 5.10): one pass each.
    GeneratedRecipe
        MITRAILLETTE = register(output -> new SequencedAssemblyRecipeBuilder(asResource("mitraillette"))
            .require(BSSnacks.HALF_BAGUETTE.get())
            .transitionTo(BSSnacks.INCOMPLETE_MITRAILLETTE.get())
            .addOutput(BSSnacks.MITRAILLETTE.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.MITRAILLETTE_MEATS))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.FRIES))
            .addStep(FillingRecipe::new, rb -> rb.require(BSTags.SAUCES, SAUCE_PER_STEP))
            .build(output)),

        // One transitional item for every breaded snack: Create tells them apart by the recipe it stores.
        RAW_SHRIMP_CROQUETTE = breaded("raw_shrimp_croquette", BSSnacks.SHRIMP_CROQUETTE_MIX.get(), BSSnacks.RAW_SHRIMP_CROQUETTE.get()),
        RAW_CHEESE_CROQUETTE = breaded("raw_cheese_croquette", BSSnacks.CHEESE_CROQUETTE_MIX.get(), BSSnacks.RAW_CHEESE_CROQUETTE.get()),
        RAW_CHICKEN_CROQUETTE = breaded("raw_chicken_croquette", BSSnacks.CHICKEN_CROQUETTE_MIX.get(), BSSnacks.RAW_CHICKEN_CROQUETTE.get()),
        RAW_LUCIFER = breaded("raw_lucifer", BSSnacks.LUCIFER_MIX.get(), BSSnacks.RAW_LUCIFER.get()),
        RAW_POTATO_CROQUETTE = breaded("raw_potato_croquette", BSSnacks.POTATO_CROQUETTE_MIX.get(), BSSnacks.RAW_POTATO_CROQUETTE.get()),
        RAW_CHICKEN_FINGERS = breaded("raw_chicken_fingers", BSSnacks.CHICKEN_STRIPS.get(), BSSnacks.RAW_CHICKEN_FINGERS.get()),
        RAW_FISH_STICKS = breaded("raw_fish_sticks", BSSnacks.FISH_STRIPS.get(), BSSnacks.RAW_FISH_STICKS.get()),
        RAW_CHEESE_STICKS = breaded("raw_cheese_sticks", BSSnacks.CHEESE_STRIPS.get(), BSSnacks.RAW_CHEESE_STICKS.get()),

        RAW_LOEMPIA = register(output -> new SequencedAssemblyRecipeBuilder(asResource("raw_loempia"))
            .require(BSSnacks.LOEMPIA_WRAPPER.get())
            .transitionTo(BSSnacks.INCOMPLETE_LOEMPIA.get())
            .addOutput(BSSnacks.RAW_LOEMPIA.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.MINCED_CHICKEN))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.get()))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.CARROT))
            .addStep(PressingRecipe::new, rb -> rb)
            .build(output)),

        // The pickles are the burger's gherkins.
        FRITERIE_BURGER = register(output -> new SequencedAssemblyRecipeBuilder(asResource("friterie_burger"))
            .require(BSItems.BURGER_BUN.get())
            .transitionTo(BSSnacks.INCOMPLETE_BURGER.get())
            .addOutput(BSSnacks.FRITERIE_BURGER.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSSnacks.PATTY.get()))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.PICKLES.get(), 50))
            .addStep(FillingRecipe::new, rb -> rb.require(BSTags.SAUCES, 50))
            .build(output)),

        HOT_DOG = register(output -> new SequencedAssemblyRecipeBuilder(asResource("hot_dog"))
            .require(BSSnacks.HOT_DOG_BUN.get())
            .transitionTo(BSSnacks.INCOMPLETE_HOT_DOG.get())
            .addOutput(BSSnacks.HOT_DOG.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.SAUSAGES))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSSnacks.ONION_RINGS.get()))
            .addStep(FillingRecipe::new, rb -> rb.require(BSTags.SAUCES, 50))
            .build(output)),

        PITA = pita("pita", Ingredient.of(BSTags.C_TOMATOES), new NotCondition(new TagEmptyCondition(BSTags.C_TOMATOES))),
        PITA_FROM_BEETROOT = pita("pita_from_beetroot", Ingredient.of(Items.BEETROOT), new TagEmptyCondition(BSTags.C_TOMATOES)),

        RAW_BROCHETTE = register(output -> new SequencedAssemblyRecipeBuilder(asResource("raw_brochette"))
            .require(Items.STICK)
            .transitionTo(BSSnacks.INCOMPLETE_BROCHETTE.get())
            .addOutput(BSSnacks.RAW_BROCHETTE.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.PORKCHOP))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.LARDONS))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.PORKCHOP))
            .build(output));

    // v2 Belgian dishes (section 5.11): one pass each.
    GeneratedRecipe
        RAW_CHICONS_AU_GRATIN = register(output -> new SequencedAssemblyRecipeBuilder(asResource("raw_chicons_au_gratin"))
            .require(be.thefricadelle.belgiansnacks.registry.BSCrops.CHICON.get())
            .transitionTo(BSDishes.INCOMPLETE_CHICONS_AU_GRATIN.get())
            .addOutput(BSDishes.RAW_CHICONS_AU_GRATIN.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.HAMS))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.BECHAMEL.get(), 250))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.C_FOODS_CHEESE))
            .build(output)),

        VOL_AU_VENT = register(output -> new SequencedAssemblyRecipeBuilder(asResource("vol_au_vent"))
            .require(BSDishes.VOL_AU_VENT_SHELL.get())
            .transitionTo(BSDishes.INCOMPLETE_VOL_AU_VENT.get())
            .addOutput(BSDishes.VOL_AU_VENT.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.COOKED_CHICKEN))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Tags.Items.MUSHROOMS))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.BECHAMEL.get(), 100))
            .build(output)),

        // Tarts share incomplete_tart: the sugar tart and the mattentaart join it in M19.
        RAW_FLAMICHE = register(output -> new SequencedAssemblyRecipeBuilder(asResource("raw_flamiche"))
            .require(BSDishes.TART_SHELL.get())
            .transitionTo(BSDishes.INCOMPLETE_TART.get())
            .addOutput(BSDishes.RAW_FLAMICHE.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.get()))
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.CREAM.get(), 250))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Tags.Items.EGGS))
            .build(output)),

        RAW_CROQUE_MONSIEUR = register(output -> new SequencedAssemblyRecipeBuilder(asResource("raw_croque_monsieur"))
            .require(Items.BREAD)
            .transitionTo(BSDishes.INCOMPLETE_CROQUE_MONSIEUR.get())
            .addOutput(BSDishes.RAW_CROQUE_MONSIEUR.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.HAMS))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.C_FOODS_CHEESE))
            .addStep(PressingRecipe::new, rb -> rb)
            .build(output));

    // v2 sweets (section 5.12): two more tarts on the shared incomplete_tart, and the merveilleux.
    GeneratedRecipe
        RAW_TARTE_AU_SUCRE = tart("raw_tarte_au_sucre", be.thefricadelle.belgiansnacks.registry.BSItems.CASSONADE.get(),
            be.thefricadelle.belgiansnacks.registry.BSSweets.RAW_TARTE_AU_SUCRE.get()),
        RAW_MATTENTAART = tart("raw_mattentaart", be.thefricadelle.belgiansnacks.registry.BSItems.MATON.get(),
            be.thefricadelle.belgiansnacks.registry.BSSweets.RAW_MATTENTAART.get()),

        MERVEILLEUX = register(output -> new SequencedAssemblyRecipeBuilder(asResource("merveilleux"))
            .require(BSItems.MERINGUE.get())
            .transitionTo(be.thefricadelle.belgiansnacks.registry.BSSweets.INCOMPLETE_MERVEILLEUX.get())
            .addOutput(be.thefricadelle.belgiansnacks.registry.BSSweets.MERVEILLEUX.get(), 1)
            .loops(1)
            .addStep(FillingRecipe::new, rb -> rb.require(BSFluids.CREAM.get(), 100))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSItems.MERINGUE.get()))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSItems.CHOCOLATE_SHAVINGS.get()))
            .build(output));

    public BSSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    // A tart shell, its filling at a deployer, then an egg to bind it.
    private GeneratedRecipe tart(String name, Item filling, Item raw) {
        return register(output -> new SequencedAssemblyRecipeBuilder(asResource(name))
            .require(BSDishes.TART_SHELL.get())
            .transitionTo(BSDishes.INCOMPLETE_TART.get())
            .addOutput(raw, 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(filling))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Tags.Items.EGGS))
            .build(output));
    }

    // Egg, then crumbs, then the press: a filling or a strip becomes a raw breaded snack.
    private GeneratedRecipe breaded(String name, Item input, Item raw) {
        return register(output -> new SequencedAssemblyRecipeBuilder(asResource(name))
            .require(input)
            .transitionTo(BSSnacks.INCOMPLETE_BREADED.get())
            .addOutput(raw, 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Tags.Items.EGGS))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSTags.C_BREAD_CRUMBS))
            .addStep(PressingRecipe::new, rb -> rb)
            .build(output));
    }

    private GeneratedRecipe pita(String name, Ingredient tomato, ICondition condition) {
        return register(output -> new SequencedAssemblyRecipeBuilder(asResource(name))
            .require(BSItems.PITA_BREAD.get())
            .transitionTo(BSSnacks.INCOMPLETE_PITA.get())
            .addOutput(BSSnacks.PITA.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(BSSnacks.GYROS.get()))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(tomato))
            .addStep(FillingRecipe::new, rb -> rb.require(BSTags.SAUCES, 50))
            .build(output.withConditions(condition)));
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
