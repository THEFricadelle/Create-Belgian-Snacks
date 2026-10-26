/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.List;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Recipe contracts, checked against Create's own machines: recipes are looked up in the live
 * RecipeManager and basin recipes are matched against a real basin.
 * <p>
 * Runs twice: {@code runGameTestServer} without compat mods, {@code runGameTestServerCompat} with
 * Farmer's Delight. Neither has a seed-oil mod. The expectations follow {@link #farmersDelight()}.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RecipeGameTests {
    private static final String TEMPLATE = "empty";

    // Loaded whatever the compat mods; the curry ketchup twins depend on Farmer's Delight.
    private static final Set<String> ALWAYS_LOADED = Set.of(
        "crushing/porkchop", "crushing/beef", "crushing/chicken", "crushing/bread",
        "milling/dried_kelp",
        "mixing/fricadelle_paste", "mixing/melted_beef_tallow", "mixing/mayonnaise",
        "compacting/frying_oil_from_seeds",
        "pressing/fricadelle_paste",
        "frying/the_fricadelle", "frying/ultimate_fricadelle",
        "cutting/raw_fries", "frying/blanched_fries", "frying/fries", "frying/fries_beef_tallow",
        "crafting/paper_cone", "deploying/cone_of_fries", "deploying/cone_of_fries_beef_tallow",
        // M20 used oil; the biodiesel needs Create Diesel Generators, in no runtime here.
        "mixing/frying_oil_from_used_oil", "compacting/oil_filter",
        // M19 sweets; the cuberdon follows the raspberries, the rice filling the rice, below.
        "mixing/speculoos_dough", "mixing/speculoos_spread", "mixing/croustillon_dough", "mixing/couque_dough", "mixing/raw_craquelin", "mixing/raw_cramique", "mixing/bouquette_batter", "mixing/babelutte", "mixing/hot_chocolate", "pressing/raw_speculoos", "pressing/raw_couque", "pressing/flat_brioche", "crushing/speculoos_crumbs", "deploying/croustillons_sugared", "deploying/bouquette_cassonade", "deploying/raw_tarte_au_riz", "deploying/raw_pain_a_la_grecque", "filling/pistolet_speculoos", "filling/brussels_waffle_speculoos", "filling/praline", "filling/praline_kriek", "filling/dame_blanche", "filling/hot_chocolate_bottle", "frying/croustillons", "frying/bouquette", "crafting/ballotin", "sequenced_assembly/raw_tarte_au_sucre", "sequenced_assembly/raw_mattentaart", "sequenced_assembly/merveilleux", "smoking/speculoos", "smelting/speculoos", "smoking/couque_de_dinant", "smelting/couque_de_dinant", "smoking/tarte_au_riz", "smelting/tarte_au_riz", "smoking/tarte_au_sucre", "smelting/tarte_au_sucre", "smoking/mattentaart", "smelting/mattentaart", "smoking/craquelin", "smelting/craquelin", "smoking/cramique", "smelting/cramique", "smoking/pain_a_la_grecque", "smelting/pain_a_la_grecque",
        // M18 waffles and the waffle iron.
        "mixing/liege_waffle_dough", "mixing/brussels_waffle_batter", "mixing/lacquemant_batter", "waffle_baking/liege_waffle", "waffle_baking/brussels_waffle", "waffle_baking/lacquemant_wafer", "deploying/brussels_waffle_sugared", "filling/brussels_waffle_chocolate", "filling/lacquemant", "mechanical_crafting/waffle_iron",
        // M17 Belgian dishes; the rabbit follows the plums below.
        "mixing/moules_marinieres", "mixing/moules_a_la_biere", "mixing/carbonnade", "mixing/stoemp", "mixing/waterzooi", "mixing/brussels_sprouts_bacon", "mixing/asparagus_flemish", "mixing/hop_shoots_cream", "mixing/salade_liegeoise", "mixing/hochepot", "mixing/escaveche", "mixing/raw_boudin", "deploying/moules_frites", "deploying/moules_frites_from_beer", "deploying/carbonnade_frites", "deploying/stoemp_saucisse", "deploying/steak_frites", "deploying/boudin_compote", "cutting/raw_vol_au_vent_shell", "pressing/tart_shell", "sequenced_assembly/raw_chicons_au_gratin", "sequenced_assembly/vol_au_vent", "sequenced_assembly/raw_flamiche", "sequenced_assembly/raw_croque_monsieur", "smoking/vol_au_vent_shell", "smelting/vol_au_vent_shell", "smoking/chicons_au_gratin", "smelting/chicons_au_gratin", "smoking/boudin", "smelting/boudin", "smoking/flamiche", "smelting/flamiche", "smoking/croque_monsieur", "smelting/croque_monsieur",
        // M16 friterie snacks; the twins follow pineapples, shrimps, rice, corn, onions and tomatoes below.
        "mixing/tartare", "mixing/pickles", "mixing/boulet_paste", "mixing/boulets_liegeois", "mixing/cheese_croquette_mix", "mixing/chicken_croquette_mix", "mixing/lucifer_mix", "mixing/potato_croquette_mix", "mixing/cervelas_mix", "mixing/mexicano_mix", "mixing/viandelle_mix", "mixing/filet_americain", "mixing/gyros", "mixing/bami_mix", "mixing/raw_chicken_nuggets", "mixing/raw_chicken_crunch", "mixing/battered_onion_rings", "pressing/raw_boulet", "pressing/raw_cervelas", "pressing/raw_mexicano", "pressing/raw_viandelle", "pressing/raw_bami_disc", "pressing/raw_nasi_disc", "pressing/loempia_wrapper", "pressing/hot_dog_bun_dough", "pressing/raw_patty", "cutting/half_baguette", "cutting/chicken_strips", "cutting/fish_strips", "cutting/cheese_strips", "deploying/pistolet_americain", "deploying/americain_frites", "deploying/friterie_cheeseburger", "deploying/raw_chicken_wings", "filling/spicy_chicken_wings", "filling/cone_of_fries_tartare", "filling/cone_of_fries_pickles", "filling/cone_of_fries_brazil", "filling/cone_of_fries_beef_tallow_tartare", "filling/cone_of_fries_beef_tallow_pickles", "filling/cone_of_fries_beef_tallow_brazil", "frying/boulet", "frying/shrimp_croquette", "frying/cheese_croquette", "frying/chicken_croquette", "frying/lucifer", "frying/potato_croquette", "frying/chicken_fingers", "frying/fish_sticks", "frying/cheese_sticks", "frying/cervelas", "frying/mexicano", "frying/viandelle", "frying/loempia", "frying/patty", "frying/brochette", "frying/bami_disc", "frying/nasi_disc", "frying/chicken_nuggets", "frying/chicken_crunch", "frying/fried_chicken_wings", "frying/onion_rings", "smoking/hot_dog_bun", "smelting/hot_dog_bun", "sequenced_assembly/mitraillette", "sequenced_assembly/raw_shrimp_croquette", "sequenced_assembly/raw_cheese_croquette", "sequenced_assembly/raw_chicken_croquette", "sequenced_assembly/raw_lucifer", "sequenced_assembly/raw_potato_croquette", "sequenced_assembly/raw_chicken_fingers", "sequenced_assembly/raw_fish_sticks", "sequenced_assembly/raw_cheese_sticks", "sequenced_assembly/raw_loempia", "sequenced_assembly/friterie_burger", "sequenced_assembly/hot_dog", "sequenced_assembly/raw_brochette",
        // M15 beers; the kriek follows the cherries below.
        "mixing/wort", "mixing/blonde_beer", "mixing/brown_beer", "mixing/gueuze", "mixing/vinegar_from_beer",
        // M14 crops; the Botany Pots crops load only with that mod.
        "milling/buckwheat_flour", "milling/mustard_powder", "mixing/mustard", "crafting/bouchot",
        // M13 base ingredients; salt, butter, powdered sugar, raisins and the Liège sauce follow the pack below.
        "mixing/cassonade",
        "mixing/cream",
        "mixing/maton",
        "mixing/vinegar",
        "mixing/liege_syrup",
        "mixing/bechamel",
        "mixing/brewers_yeast",
        "mixing/brioche_dough",
        "mixing/pita_dough",
        "mixing/noodles",
        "mixing/brined_ham",
        "mixing/raw_meringue",
        "mixing/apple_compote",
        "compacting/pearl_sugar",
        "compacting/young_herve_cheese",
        "compacting/puff_pastry",
        "compacting/vanilla_ice_cream",
        "crushing/chocolate_shavings",
        "cutting/pistolet_dough",
        "cutting/burger_bun_dough",
        "cutting/lardons",
        "pressing/raw_pita",
        "splashing/herve_cheese",
        "splashing/sprouted_grain",
        "smoking/malt",
        "smoking/pistolet",
        "smelting/pistolet",
        "smoking/burger_bun",
        "smelting/burger_bun",
        "smoking/pita_bread",
        "smelting/pita_bread",
        "smoking/ardennes_ham",
        "smoking/meringue",
        "filling/cone_of_fries_sauce_lapin",
        "filling/cone_of_fries_beef_tallow_sauce_lapin",
        // The sauced cones (M12); the sauces themselves follow the tomato, onion and pepper tags below.
        "filling/cone_of_fries_mayonnaise", "filling/cone_of_fries_beef_tallow_mayonnaise",
        "filling/cone_of_fries_curry_ketchup", "filling/cone_of_fries_beef_tallow_curry_ketchup",
        "filling/cone_of_fries_andalouse", "filling/cone_of_fries_beef_tallow_andalouse",
        "filling/cone_of_fries_samourai", "filling/cone_of_fries_beef_tallow_samourai",
        "filling/cone_of_fries_americaine", "filling/cone_of_fries_beef_tallow_americaine");
    private static final List<String> ONION_ASSEMBLIES =
        List.of("sequenced_assembly/raw_the_fricadelle", "sequenced_assembly/raw_ultimate_fricadelle");
    private static final List<String> BEETROOT_ASSEMBLIES =
        List.of("sequenced_assembly/raw_the_fricadelle_from_beetroot", "sequenced_assembly/raw_ultimate_fricadelle_from_beetroot");
    private static final String TOMATO_KETCHUP = "mixing/curry_ketchup";
    private static final String BEETROOT_KETCHUP = "mixing/curry_ketchup_from_beetroot";

    private RecipeGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void conditionalRecipesFollowTheLoadedMods(GameTestHelper helper) {
        for (String id : ALWAYS_LOADED) {
            helper.assertTrue(find(helper, id) != null, id + " should be loaded");
        }
        boolean tomatoes = farmersDelight();
        helper.assertTrue((find(helper, TOMATO_KETCHUP) != null) == tomatoes,
            TOMATO_KETCHUP + (tomatoes ? " should be loaded with tomatoes" : " must stay off while c:crops/tomato is empty"));
        helper.assertTrue((find(helper, BEETROOT_KETCHUP) != null) != tomatoes,
            BEETROOT_KETCHUP + (tomatoes ? " must stay off when tomatoes exist" : " should be loaded as the fallback"));
        // Farmer's Delight also brings the onions of tiers 2 and 3 (D7).
        boolean onions = farmersDelight();
        for (String onion : ONION_ASSEMBLIES) {
            helper.assertTrue((find(helper, onion) != null) == onions,
                onion + (onions ? " should be loaded with onions" : " must stay off while c:crops/onion is empty"));
        }
        for (String beetroot : BEETROOT_ASSEMBLIES) {
            helper.assertTrue((find(helper, beetroot) != null) != onions,
                beetroot + (onions ? " must stay off when onions exist" : " should be loaded as the fallback"));
        }
        // v2 sauces: tomato and onion come with Farmer's Delight; no runtime here has a hot pepper (V-D5).
        twins(helper, "mixing/andalouse", "mixing/andalouse_from_beetroot", tomatoes);
        twins(helper, "mixing/americaine", "mixing/americaine_from_beetroot", onions);
        twins(helper, "mixing/samourai", "mixing/samourai_from_blaze_powder", false);
        twins(helper, "mixing/sauce_lapin", "mixing/sauce_lapin_from_beetroot", onions);
        twins(helper, "smoking/raisins", "smoking/raisins_from_sweet_berries", false);
        twins(helper, "mixing/kriek", "mixing/kriek_from_sweet_berries", false);
        // M16: Farmer's Delight brings rice, onions and tomatoes; no runtime here has pineapple, shrimp, corn or squid.
        twins(helper, "mixing/brazil", "mixing/brazil_from_melon", false);
        twins(helper, "mixing/lapin_a_la_gueuze", "mixing/lapin_a_la_gueuze_from_sweet_berries", false);
        twins(helper, "mixing/cuberdon", "mixing/cuberdon_from_sweet_berries", false);
        helper.assertTrue(find(helper, "mixing/biodiesel_from_used_oil") == null, "biodiesel without Create Diesel Generators");
        // Farmer's Delight brings rice; without it there is no rice tart.
        helper.assertTrue((find(helper, "mixing/rice_filling") != null) == farmersDelight(), "mixing/rice_filling follows the rice");
        twins(helper, "mixing/shrimp_croquette_mix", "mixing/shrimp_croquette_mix_from_cod", false);
        twins(helper, "mixing/nasi_mix", "mixing/nasi_mix_from_wheat", farmersDelight());
        twins(helper, "cutting/raw_onion_rings", "cutting/raw_onion_rings_from_beetroot", onions);
        twins(helper, "sequenced_assembly/pita", "sequenced_assembly/pita_from_beetroot", tomatoes);
        twins(helper, "cutting/corn_flakes", "mixing/raw_chicken_crunch_from_bread_crumbs", false);
        helper.assertTrue(find(helper, "cutting/raw_squid_rings") == null, "no squid here: no calamari");
        // M13 rule 2: ours only where the pack does not make it (no runtime here has Create: Food).
        helper.assertTrue((find(helper, "mixing/salt") != null) == tagEmpty(BSTags.C_SALT), "mixing/salt follows c:salt");
        helper.assertTrue((find(helper, "mixing/butter") != null) == tagEmpty(BSTags.C_BUTTER), "mixing/butter follows c:butter");
        helper.assertTrue(find(helper, "milling/powdered_sugar") != null, "milling/powdered_sugar without Create: Food");
        helper.succeed();
    }

    private static boolean tagEmpty(net.minecraft.tags.TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.getTag(tag).map(set -> set.size() == 0).orElse(true);
    }

    // M20 on a real basin: a filter cleans used oil; without one the used oil stays.
    @GameTest(template = TEMPLATE)
    public static void usedOilIsFilteredOnARealBasin(GameTestHelper helper) {
        FluidStack used = new FluidStack(BSFluids.USED_FRYING_OIL.get().getSource(), 1000);
        Recipe<?> filter = recipe(helper, "mixing/frying_oil_from_used_oil");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, List.of(BSItems.OIL_FILTER.asStack()), used), filter), "filtered");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, List.of(), used.copy()), filter), "filtered without a filter");
        assertFluidOutput(helper, "mixing/frying_oil_from_used_oil", BSFluids.FRYING_OIL.get().getSource(), 800);
        helper.succeed();
    }

    // M19 on a real basin: the cuberdon needs heat and its honey; the croustillon dough (beer) and the
    // Brussels batter (milk) never take each other's basin; the hot chocolate needs its sugar.
    @GameTest(template = TEMPLATE)
    public static void sweetsMixOnARealBasin(GameTestHelper helper) {
        List<ItemStack> berries = List.of(new ItemStack(Items.SUGAR), new ItemStack(Items.SWEET_BERRIES), new ItemStack(Items.SWEET_BERRIES));
        FluidStack honey = new FluidStack(com.simibubi.create.AllFluids.HONEY.get().getSource(), 100);
        Recipe<?> cuberdon = recipe(helper, "mixing/cuberdon_from_sweet_berries");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, berries, honey), cuberdon), "cuberdon cooked cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), true, berries, honey.copy()), cuberdon), "cuberdon on a burner");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(4, 1, 0), true, berries, null), cuberdon), "cuberdon without honey");
        List<ItemStack> dough = List.of(new ItemStack(com.simibubi.create.AllItems.WHEAT_FLOUR.get()), new ItemStack(Items.SUGAR), BSItems.BREWERS_YEAST.asStack());
        BasinBlockEntity beer = basin(helper, new BlockPos(0, 1, 2), false, dough, new FluidStack(BSFluids.BLONDE_BEER.get().getSource(), 250));
        helper.assertTrue(BasinRecipe.match(beer, recipe(helper, "mixing/croustillon_dough")), "croustillon dough with beer");
        helper.assertFalse(BasinRecipe.match(beer, recipe(helper, "mixing/brussels_waffle_batter")), "brussels batter from beer");
        BasinBlockEntity milk = basin(helper, new BlockPos(2, 1, 2), false, dough, new FluidStack(net.neoforged.neoforge.common.NeoForgeMod.MILK.get(), 250));
        helper.assertFalse(BasinRecipe.match(milk, recipe(helper, "mixing/croustillon_dough")), "croustillon dough from milk");
        BasinBlockEntity chocolate = basin(helper, new BlockPos(4, 1, 2), true, List.of(),
            new FluidStack(net.neoforged.neoforge.common.NeoForgeMod.MILK.get(), 250));
        helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(new BlockPos(4, 1, 2)), null)
            .fill(new FluidStack(com.simibubi.create.AllFluids.CHOCOLATE.get().getSource(), 100), IFluidHandler.FluidAction.EXECUTE);
        helper.assertFalse(BasinRecipe.match(chocolate, recipe(helper, "mixing/hot_chocolate")), "hot chocolate without sugar");
        helper.succeed();
    }

    // M18 on a real basin: the two batters never hide in each other's basin, the Liège dough starts
    // from the brioche dough.
    @GameTest(template = TEMPLATE)
    public static void wafflesMixOnARealBasin(GameTestHelper helper) {
        FluidStack milk = new FluidStack(net.neoforged.neoforge.common.NeoForgeMod.MILK.get(), 250);
        ItemStack flour = new ItemStack(com.simibubi.create.AllItems.WHEAT_FLOUR.get());
        List<ItemStack> brussels = List.of(flour, new ItemStack(Items.SUGAR), BSItems.BREWERS_YEAST.asStack());
        List<ItemStack> lacquemant = List.of(flour.copy(), BSItems.BUTTER.asStack(), BSItems.CASSONADE.asStack(), BSItems.BREWERS_YEAST.asStack());
        BasinBlockEntity b = basin(helper, new BlockPos(0, 1, 0), false, brussels, milk);
        helper.assertTrue(BasinRecipe.match(b, recipe(helper, "mixing/brussels_waffle_batter")), "brussels batter");
        helper.assertFalse(BasinRecipe.match(b, recipe(helper, "mixing/lacquemant_batter")), "lacquemant batter in a brussels basin");
        BasinBlockEntity l = basin(helper, new BlockPos(2, 1, 0), false, lacquemant, milk.copy());
        helper.assertTrue(BasinRecipe.match(l, recipe(helper, "mixing/lacquemant_batter")), "lacquemant batter");
        helper.assertFalse(BasinRecipe.match(l, recipe(helper, "mixing/brussels_waffle_batter")), "brussels batter in a lacquemant basin");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, List.of(BSItems.BRIOCHE_DOUGH.asStack(),
            BSItems.PEARL_SUGAR.asStack(), BSItems.BUTTER.asStack()), null), recipe(helper, "mixing/liege_waffle_dough")), "liege dough from brioche");
        helper.succeed();
    }

    // M17 on a real basin: the moules follow their liquid, the stews need their burner, the salad and the
    // marinade stay cold, and every bowl dish needs its bowl.
    @GameTest(template = TEMPLATE)
    public static void dishesMixOnARealBasin(GameTestHelper helper) {
        List<ItemStack> moules = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            moules.add(be.thefricadelle.belgiansnacks.registry.BSCrops.MUSSEL.asStack());
        }
        // The leek counts as the onion too.
        moules.addAll(List.of(be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack(), be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack(),
            new ItemStack(Items.BOWL)));
        BasinBlockEntity water = basin(helper, new BlockPos(0, 1, 0), true, moules, new FluidStack(Fluids.WATER, 250));
        helper.assertTrue(BasinRecipe.match(water, recipe(helper, "mixing/moules_marinieres")), "moules marinieres in water");
        helper.assertFalse(BasinRecipe.match(water, recipe(helper, "mixing/moules_a_la_biere")), "moules a la biere without beer");
        BasinBlockEntity beer = basin(helper, new BlockPos(2, 1, 0), true, moules, new FluidStack(BSFluids.BLONDE_BEER.get().getSource(), 250));
        helper.assertTrue(BasinRecipe.match(beer, recipe(helper, "mixing/moules_a_la_biere")), "moules a la biere in beer");
        helper.assertFalse(BasinRecipe.match(beer, recipe(helper, "mixing/moules_marinieres")), "moules marinieres without water");
        List<ItemStack> noBowl = moules.subList(0, moules.size() - 1);
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(4, 1, 0), true, noBowl, new FluidStack(Fluids.WATER, 250)),
            recipe(helper, "mixing/moules_marinieres")), "a bowl dish without its bowl");
        List<ItemStack> carbonnade = List.of(new ItemStack(Items.BEEF), new ItemStack(Items.BEEF), be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack(),
            BSItems.PISTOLET.asStack(), BSItems.CASSONADE.asStack(), new ItemStack(Items.BOWL));
        FluidStack brown = new FluidStack(BSFluids.BROWN_BEER.get().getSource(), 250);
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, carbonnade, brown), recipe(helper, "mixing/carbonnade")), "carbonnade cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), true, carbonnade, brown), recipe(helper, "mixing/carbonnade")), "carbonnade on a burner");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(4, 1, 2), false, List.of(new ItemStack(Items.BAKED_POTATO), new ItemStack(Items.BAKED_POTATO),
            BSItems.LARDONS.asStack(), be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack(), new ItemStack(Items.BOWL)),
            new FluidStack(BSFluids.VINEGAR.get().getSource(), 50)), recipe(helper, "mixing/salade_liegeoise")), "salade liegeoise cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 4), false, List.of(new ItemStack(Items.COOKED_COD),
            be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack(), new ItemStack(Items.BOWL)),
            new FluidStack(BSFluids.VINEGAR.get().getSource(), 100)), recipe(helper, "mixing/escaveche")), "escaveche from cooked cod");
        helper.succeed();
    }

    // M16 on a real basin. Overlaps inside our menu: with everything for a boulet paste in the basin,
    // the cervelas mix matches too, and Create picks the recipe with more items. Gyros cooks on a burner;
    // the nasi mix stays cold (in Arcadia a heated basin turns lone rice into a fluid).
    @GameTest(template = TEMPLATE)
    public static void friterieMixesOnARealBasin(GameTestHelper helper) {
        List<ItemStack> boulet = List.of(BSItems.MINCED_PORK.asStack(), BSItems.MINCED_BEEF.asStack(), BSItems.BREAD_CRUMBS.asStack(),
            BSItems.BELGIAN_SPICES.asStack(), BSItems.SALT.asStack());
        BasinBlockEntity full = basin(helper, new BlockPos(0, 1, 0), false, boulet, null);
        helper.assertTrue(BasinRecipe.match(full, recipe(helper, "mixing/boulet_paste")), "boulet paste");
        helper.assertTrue(BasinRecipe.match(full, recipe(helper, "mixing/cervelas_mix")), "the cervelas mix hides inside the boulet paste");
        helper.assertTrue(recipe(helper, "mixing/boulet_paste").getIngredients().size() > recipe(helper, "mixing/cervelas_mix").getIngredients().size(),
            "the boulet paste must have more items than the cervelas mix, so Create prefers it");
        helper.assertTrue(recipe(helper, "mixing/viandelle_mix").getIngredients().size() > recipe(helper, "mixing/fricadelle_paste").getIngredients().size(),
            "the viandelle mix must have more items than the fricadelle paste");
        List<ItemStack> gyros = List.of(BSItems.MINCED_PORK.asStack(), BSItems.MINCED_PORK.asStack(), BSItems.BELGIAN_SPICES.asStack());
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, gyros, null), recipe(helper, "mixing/gyros")), "gyros cooked cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), true, gyros, null), recipe(helper, "mixing/gyros")), "gyros on a burner");
        String nasi = farmersDelight() ? "mixing/nasi_mix" : "mixing/nasi_mix_from_wheat";
        ItemStack grain = farmersDelight() ? new ItemStack(item("farmersdelight", "rice")) : new ItemStack(Items.WHEAT);
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), false, List.of(grain, grain.copy(), BSItems.MINCED_PORK.asStack(),
            be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack(), BSItems.BELGIAN_SPICES.asStack()), null), recipe(helper, nasi)), nasi + " mixes cold");
        // Tartare: two sauces and the spices.
        BasinBlockEntity tartare = basin(helper, new BlockPos(1, 1, 4), false, List.of(BSItems.BELGIAN_SPICES.asStack()),
            new FluidStack(BSFluids.MAYONNAISE.get().getSource(), 250));
        helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(new BlockPos(1, 1, 4)), null)
            .fill(new FluidStack(BSFluids.PICKLES.get().getSource(), 50), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(BasinRecipe.match(tartare, recipe(helper, "mixing/tartare")), "tartare from mayonnaise, pickles and spices");
        helper.succeed();
    }

    // M15 on a real basin: wort needs heat; with cassonade beside the yeast, Create picks the brown
    // beer (the recipe with more items); the gueuze needs its wheat.
    @GameTest(template = TEMPLATE)
    public static void beersBrewOnARealBasin(GameTestHelper helper) {
        List<ItemStack> mash = List.of(BSItems.MALT.asStack(), BSItems.MALT.asStack(), be.thefricadelle.belgiansnacks.registry.BSCrops.HOPS.asStack());
        FluidStack water = new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, mash, water), recipe(helper, "mixing/wort")), "wort brewed cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), true, mash, water), recipe(helper, "mixing/wort")), "wort over a burner");
        FluidStack wort = new FluidStack(BSFluids.WORT.get().getSource(), 1000);
        BasinBlockEntity both = basin(helper, new BlockPos(0, 1, 2), false, List.of(BSItems.BREWERS_YEAST.asStack(), BSItems.CASSONADE.asStack()), wort);
        helper.assertTrue(BasinRecipe.match(both, recipe(helper, "mixing/brown_beer")), "brown beer with cassonade");
        helper.assertTrue(recipe(helper, "mixing/brown_beer").getIngredients().size() > recipe(helper, "mixing/blonde_beer").getIngredients().size(),
            "the brown beer must have more items than the blonde, so Create prefers it");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), false, List.of(), wort), recipe(helper, "mixing/gueuze")),
            "gueuze from wort alone would take any basin's wort");
        helper.succeed();
    }

    // M13 on a real basin: dairy and doughs need their item, béchamel needs heat and its three items.
    @GameTest(template = TEMPLATE)
    public static void baseIngredientsMixOnARealBasin(GameTestHelper helper) {
        FluidStack milk = new FluidStack(net.neoforged.neoforge.common.NeoForgeMod.MILK.get(), 1000);
        ItemStack salt = BSItems.SALT.asStack();
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, List.of(salt), milk), recipe(helper, "mixing/cream")),
            "cream from milk and salt");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, List.of(), milk), recipe(helper, "mixing/cream")),
            "cream from milk alone would take any line's milk");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, List.of(salt),
            new FluidStack(BSFluids.CREAM.get().getSource(), 250)), recipe(helper, "mixing/butter")), "salted butter from cream");
        List<ItemStack> roux = List.of(new ItemStack(com.simibubi.create.AllItems.WHEAT_FLOUR.get()), BSItems.BUTTER.asStack(),
            BSItems.BELGIAN_SPICES.asStack());
        FluidStack halfMilk = new FluidStack(net.neoforged.neoforge.common.NeoForgeMod.MILK.get(), 500);
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), false, roux, halfMilk), recipe(helper, "mixing/bechamel")),
            "bechamel cooked cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(1, 1, 1), true, roux, halfMilk), recipe(helper, "mixing/bechamel")),
            "bechamel over a lit burner");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 4), false, List.of(new ItemStack(com.simibubi.create.AllItems.DOUGH.get()),
            BSItems.BUTTER.asStack(), new ItemStack(Items.EGG), new ItemStack(Items.SUGAR), BSItems.BREWERS_YEAST.asStack()), null),
            recipe(helper, "mixing/brioche_dough")), "brioche dough from Create's dough");
        helper.succeed();
    }

    private static void twins(GameTestHelper helper, String main, String fallback, boolean ingredient) {
        helper.assertTrue((find(helper, main) != null) == ingredient, main + (ingredient ? " should be loaded" : " must stay off"));
        helper.assertTrue((find(helper, fallback) != null) != ingredient, fallback + (ingredient ? " must stay off" : " should be loaded"));
    }

    // v2 sauces on a real basin: mayonnaise-based, samouraï needs heat, américaine takes two fluids.
    @GameTest(template = TEMPLATE)
    public static void saucesMixOnARealBasin(GameTestHelper helper) {
        ItemStack tomato = farmersDelight() ? new ItemStack(item("farmersdelight", "tomato")) : new ItemStack(Items.BEETROOT);
        ItemStack onion = farmersDelight() ? new ItemStack(item("farmersdelight", "onion")) : new ItemStack(Items.BEETROOT);
        FluidStack mayo = new FluidStack(BSFluids.MAYONNAISE.get().getSource(), 250);
        Recipe<?> andalouse = recipe(helper, farmersDelight() ? "mixing/andalouse" : "mixing/andalouse_from_beetroot");
        Recipe<?> samourai = recipe(helper, "mixing/samourai_from_blaze_powder");
        Recipe<?> americaine = recipe(helper, farmersDelight() ? "mixing/americaine" : "mixing/americaine_from_beetroot");
        List<ItemStack> spicyTomato = List.of(tomato, BSItems.BELGIAN_SPICES.asStack());
        List<ItemStack> fire = List.of(new ItemStack(Items.BLAZE_POWDER), BSItems.BELGIAN_SPICES.asStack());
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, spicyTomato, mayo), andalouse), "andalouse mixes cold");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, spicyTomato, null), andalouse), "andalouse without mayonnaise");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, fire, mayo), samourai), "samourai mixed cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), true, fire, mayo), samourai), "samourai over a lit burner");
        BasinBlockEntity both = basin(helper, new BlockPos(1, 1, 1), false, List.of(onion), mayo);
        IFluidHandler tanks = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(new BlockPos(1, 1, 1)), null);
        helper.assertValueEqual(tanks.fill(new FluidStack(BSFluids.CURRY_KETCHUP.get().getSource(), 50), IFluidHandler.FluidAction.EXECUTE), 50,
            "curry ketchup beside the mayonnaise");
        helper.assertTrue(BasinRecipe.match(both, americaine), "americaine from mayonnaise, curry ketchup and onion");
        assertFluidOutput(helper, "mixing/americaine_from_beetroot", BSFluids.AMERICAINE.get().getSource(), 300);
        helper.succeed();
    }

    private static void assertFluidOutput(GameTestHelper helper, String id, net.minecraft.world.level.material.Fluid fluid, int amount) {
        if (find(helper, id) == null) {
            return;
        }
        var outputs = ((com.simibubi.create.content.processing.recipe.ProcessingRecipe<?, ?>) recipe(helper, id)).getFluidResults();
        helper.assertTrue(outputs.size() == 1 && outputs.get(0).getFluid() == fluid && outputs.get(0).getAmount() == amount,
            id + " gives " + outputs);
    }

    @GameTest(template = TEMPLATE)
    public static void crushingTurnsMeatIntoMinceAndBeefIntoTallow(GameTestHelper helper) {
        assertSingleInput(helper, "crushing/porkchop", Items.PORKCHOP);
        assertOutputs(helper, "crushing/porkchop", List.of(out(BSItems.MINCED_PORK.get(), 2, 1f)));
        assertSingleInput(helper, "crushing/beef", Items.BEEF);
        assertOutputs(helper, "crushing/beef", List.of(out(BSItems.MINCED_BEEF.get(), 2, 1f), out(BSItems.BEEF_TALLOW.get(), 1, 0.5f)));
        assertSingleInput(helper, "crushing/chicken", Items.CHICKEN);
        assertOutputs(helper, "crushing/chicken", List.of(out(BSItems.MINCED_CHICKEN.get(), 2, 1f)));
        assertSingleInput(helper, "crushing/bread", Items.BREAD);
        assertOutputs(helper, "crushing/bread", List.of(out(BSItems.BREAD_CRUMBS.get(), 3, 1f)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void millingAndPressing(GameTestHelper helper) {
        assertSingleInput(helper, "milling/dried_kelp", Items.DRIED_KELP);
        assertOutputs(helper, "milling/dried_kelp", List.of(out(BSItems.BELGIAN_SPICES.get(), 1, 1f), out(BSItems.BELGIAN_SPICES.get(), 1, 0.25f)));
        assertSingleInput(helper, "pressing/fricadelle_paste", BSItems.FRICADELLE_PASTE.get());
        assertOutputs(helper, "pressing/fricadelle_paste", List.of(out(BSItems.RAW_FRICADELLE.get(), 1, 1f)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pasteMixesOnlyWithAllThreeMeatsAndCrumbs(GameTestHelper helper) {
        BasinBlockEntity full = basin(helper, new BlockPos(0, 1, 0), false,
            List.of(BSItems.MINCED_PORK.asStack(), BSItems.MINCED_BEEF.asStack(), BSItems.MINCED_CHICKEN.asStack(), BSItems.BREAD_CRUMBS.asStack()), null);
        BasinBlockEntity noChicken = basin(helper, new BlockPos(2, 1, 0), false,
            List.of(BSItems.MINCED_PORK.asStack(), BSItems.MINCED_BEEF.asStack(), BSItems.BREAD_CRUMBS.asStack()), null);
        helper.assertTrue(BasinRecipe.match(full, recipe(helper, "mixing/fricadelle_paste")), "paste should mix with all ingredients");
        helper.assertFalse(BasinRecipe.match(noChicken, recipe(helper, "mixing/fricadelle_paste")), "paste must not mix without chicken");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mayonnaiseAcceptsAnyFryingOilButNotWater(GameTestHelper helper) {
        Recipe<?> mayo = recipe(helper, "mixing/mayonnaise");
        List<ItemStack> egg = List.of(new ItemStack(Items.EGG));
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, egg, new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 100)), mayo),
            "mayonnaise from frying oil");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, egg, new FluidStack(BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 100)), mayo),
            "mayonnaise from melted beef tallow");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, egg, new FluidStack(Fluids.WATER, 100)), mayo),
            "water is not an oil");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), false, egg, new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 99)), mayo),
            "99 mB is not enough");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void heatedRecipesNeedALitBlazeBurner(GameTestHelper helper) {
        List<ItemStack> tallow = List.of(BSItems.BEEF_TALLOW.asStack(2));
        ItemStack base = farmersDelight() ? new ItemStack(item("farmersdelight", "tomato")) : new ItemStack(Items.BEETROOT);
        List<ItemStack> ketchup = List.of(base, new ItemStack(Items.SUGAR), BSItems.BELGIAN_SPICES.asStack());
        Recipe<?> melting = recipe(helper, "mixing/melted_beef_tallow");
        Recipe<?> curry = recipe(helper, farmersDelight() ? TOMATO_KETCHUP : BEETROOT_KETCHUP);
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, tallow, null), melting), "tallow must not melt cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), true, tallow, null), melting), "tallow melts over a lit burner");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, ketchup, null), curry), "curry ketchup must not cook cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), true, ketchup, null), curry), "curry ketchup cooks over a lit burner");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void seedsCompactIntoFryingOilWithoutOtherOilMods(GameTestHelper helper) {
        Recipe<?> seeds = recipe(helper, "compacting/frying_oil_from_seeds");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, List.of(new ItemStack(Items.WHEAT_SEEDS, 8)), null), seeds),
            "8 seeds compact into frying oil");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, List.of(new ItemStack(Items.WHEAT_SEEDS, 7)), null), seeds),
            "7 seeds are not enough");
        helper.succeed();
    }

    // Only meaningful in the compat run; without Farmer's Delight there is nothing to interoperate with.
    @GameTest(template = TEMPLATE)
    public static void farmersDelightMinceWorksInOurPaste(GameTestHelper helper) {
        if (!farmersDelight()) {
            helper.succeed();
            return;
        }
        ItemStack theirBeef = new ItemStack(item("farmersdelight", "minced_beef"));
        helper.assertTrue(theirBeef.is(BSTags.MINCED_BEEF), "farmersdelight:minced_beef not in minced_meats/beef");
        helper.assertTrue(BSItems.MINCED_BEEF.asStack().is(BSTags.C_MINCED_BEEF), "our minced beef not in c:minced_beef");
        BasinBlockEntity basin = basin(helper, new BlockPos(0, 1, 0), false,
            List.of(BSItems.MINCED_PORK.asStack(), theirBeef, BSItems.MINCED_CHICKEN.asStack(), BSItems.BREAD_CRUMBS.asStack()), null);
        helper.assertTrue(BasinRecipe.match(basin, recipe(helper, "mixing/fricadelle_paste")), "paste should accept Farmer's Delight minced beef");
        helper.succeed();
    }

    // End to end: a real mechanical press driven by a creative motor presses paste on a depot.
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aPoweredPressTurnsPasteIntoRawFricadelle(GameTestHelper helper) {
        BlockPos depot = new BlockPos(1, 0, 1);
        BlockPos press = new BlockPos(1, 2, 1);
        BlockPos motor = new BlockPos(2, 2, 1);
        helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
        helper.setBlock(press, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.WEST));
        IItemHandler depotItems = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), null);
        helper.assertTrue(depotItems != null, "depot exposes an item handler");
        depotItems.insertItem(0, BSItems.FRICADELLE_PASTE.asStack(), false);

        helper.succeedWhen(() -> {
            boolean pressed = false;
            for (int slot = 0; slot < depotItems.getSlots(); slot++) {
                pressed |= depotItems.getStackInSlot(slot).is(BSItems.RAW_FRICADELLE.get());
            }
            helper.assertTrue(pressed, "no raw fricadelle on the depot yet");
        });
    }

    private static BasinBlockEntity basin(GameTestHelper helper, BlockPos pos, boolean heated, List<ItemStack> items, FluidStack fluid) {
        // The burner goes first: a basin caches the heat below it on first read.
        if (heated) {
            helper.setBlock(pos.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED));
        }
        helper.setBlock(pos, AllBlocks.BASIN.getDefaultState());
        BlockPos absolute = helper.absolutePos(pos);
        IItemHandler itemHandler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absolute, null);
        for (ItemStack stack : items) {
            ItemStack left = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), false);
            helper.assertTrue(left.isEmpty(), "basin refused " + stack);
        }
        if (fluid != null) {
            IFluidHandler fluidHandler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolute, null);
            int filled = fluidHandler.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
            helper.assertValueEqual(filled, fluid.getAmount(), "fluid accepted by the basin");
        }
        return helper.getBlockEntity(pos);
    }

    private static boolean farmersDelight() {
        return ModList.get().isLoaded("farmersdelight");
    }

    private static Item item(String namespace, String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (item == Items.AIR) {
            throw new AssertionError(namespace + ":" + path + " is not registered");
        }
        return item;
    }

    private static Recipe<?> find(GameTestHelper helper, String id) {
        return helper.getLevel().getRecipeManager().byKey(BelgianSnacks.asResource(id)).map(holder -> holder.value()).orElse(null);
    }

    private static Recipe<?> recipe(GameTestHelper helper, String id) {
        Recipe<?> recipe = find(helper, id);
        helper.assertTrue(recipe != null, "recipe " + id + " is not loaded");
        return recipe;
    }

    private static void assertSingleInput(GameTestHelper helper, String id, Item input) {
        var ingredients = recipe(helper, id).getIngredients();
        helper.assertValueEqual(ingredients.size(), 1, id + " ingredient count");
        helper.assertTrue(ingredients.get(0).test(new ItemStack(input)), id + " should accept " + input);
    }

    private record Out(Item item, int count, float chance) {
    }

    private static Out out(Item item, int count, float chance) {
        return new Out(item, count, chance);
    }

    private static void assertOutputs(GameTestHelper helper, String id, List<Out> expected) {
        List<ProcessingOutput> actual = ((ProcessingRecipe<?, ?>) recipe(helper, id)).getRollableResults();
        helper.assertValueEqual(actual.size(), expected.size(), id + " output count");
        for (int i = 0; i < expected.size(); i++) {
            Out want = expected.get(i);
            ProcessingOutput got = actual.get(i);
            helper.assertTrue(got.getStack().is(want.item()) && got.getStack().getCount() == want.count() && got.getChance() == want.chance(),
                id + " output " + i + " is " + got.getStack() + " at " + got.getChance() + ", expected " + want);
        }
    }
}
