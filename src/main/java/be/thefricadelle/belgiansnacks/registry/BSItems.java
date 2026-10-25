/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FricadelleItem;
import be.thefricadelle.belgiansnacks.content.food.UltimateFricadelleItem;
import net.minecraft.core.component.DataComponents;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.Tags;

public final class BSItems {
    // Tier 1: Fricadelle
    public static final ItemEntry<Item>
        // Also in the convention tags so other mods' recipes accept ours.
        MINCED_PORK = ingredient("minced_pork", "Minced Pork")
            .tag(BSTags.MINCED_MEATS, BSTags.MINCED_PORK, BSTags.C_GROUND_PORK).register(),
        MINCED_BEEF = ingredient("minced_beef", "Minced Beef")
            .tag(BSTags.MINCED_MEATS, BSTags.MINCED_BEEF, BSTags.C_GROUND_BEEF, BSTags.C_MINCED_BEEF).register(),
        MINCED_CHICKEN = ingredient("minced_chicken", "Minced Chicken")
            .tag(BSTags.MINCED_MEATS, BSTags.MINCED_CHICKEN, BSTags.C_GROUND_CHICKEN).register(),
        BEEF_TALLOW = ingredient("beef_tallow", "Beef Tallow").register(),
        BREAD_CRUMBS = ingredient("bread_crumbs", "Bread Crumbs").tag(BSTags.C_BREAD_CRUMBS).register(),
        FRICADELLE_PASTE = ingredient("fricadelle_paste", "Fricadelle Paste").tag(BSTags.GRINDER_BLACKLIST).register(),
        RAW_FRICADELLE = ingredient("raw_fricadelle", "Raw Fricadelle").tag(BSTags.GRINDER_BLACKLIST).register();

    public static final ItemEntry<FricadelleItem> FRICADELLE =
        fricadelle("fricadelle", "Fricadelle", BSFoods.FRICADELLE, FricadelleItem::new).register();

    // Tier 2: THE_Fricadelle
    public static final ItemEntry<Item>
        BELGIAN_SPICES = ingredient("belgian_spices", "Belgian Spices").register(),
        EXCEPTIONAL_PASTE = ingredient("exceptional_paste", "Exceptional Paste").tag(BSTags.GRINDER_BLACKLIST).register(),
        RAW_THE_FRICADELLE = ingredient("raw_the_fricadelle", "Raw THE_Fricadelle").tag(BSTags.GRINDER_BLACKLIST).register();

    public static final ItemEntry<FricadelleItem> THE_FRICADELLE =
        fricadelle("the_fricadelle", "THE_Fricadelle", BSFoods.THE_FRICADELLE, FricadelleItem::new).register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_THE_FRICADELLE =
        REGISTRATE.item("incomplete_the_fricadelle", SequencedAssemblyItem::new)
            .lang("Incomplete THE_Fricadelle")
            .tag(BSTags.GRINDER_BLACKLIST)
            .register();

    // Tier 3: THE_FRICADELLE
    public static final ItemEntry<Item>
        ABSOLUTE_PASTE = ingredient("absolute_paste", "Absolute Paste").tag(BSTags.GRINDER_BLACKLIST).register(),
        RAW_ULTIMATE_FRICADELLE = ingredient("raw_ultimate_fricadelle", "Raw THE_FRICADELLE")
            .tag(BSTags.GRINDER_BLACKLIST, BSTags.FRYER_ONE_AT_A_TIME).register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_ULTIMATE_FRICADELLE =
        REGISTRATE.item("incomplete_ultimate_fricadelle", SequencedAssemblyItem::new)
            .lang("Incomplete THE_FRICADELLE")
            .tag(BSTags.GRINDER_BLACKLIST)
            .register();

    public static final ItemEntry<UltimateFricadelleItem> ULTIMATE_FRICADELLE =
        fricadelle("ultimate_fricadelle", "THE_FRICADELLE", BSFoods.ULTIMATE_FRICADELLE, UltimateFricadelleItem::new)
            .properties(p -> p.rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
            .register();

    // v2, Belgian fries (docs/13, section 5.1): cut, blanched, fried again, served in a cone.
    // Finished snacks count for the Supreme Grinder (V-D1): they are not blacklisted.
    public static final ItemEntry<Item>
        RAW_FRIES = ingredient("raw_fries", "Raw Fries").register(),
        BLANCHED_FRIES = ingredient("blanched_fries", "Blanched Fries").register(),
        FRIES = snack("fries", "Fries", BSFoods.FRIES, BSTags.FRIES).register(),
        // Not in #fries: they never end up in a plain cone.
        FRIES_BEEF_TALLOW = snack("fries_beef_tallow", "Beef Tallow Fries", BSFoods.FRIES_BEEF_TALLOW).register(),
        // A crafting recipe, not the press: in Arcadia the press already turns paper into tickets.
        // Mechanical crafters run it too.
        PAPER_CONE = ingredient("paper_cone", "Paper Cone")
            .recipe((context, provider) -> ShapedRecipeBuilder
                .shaped(RecipeCategory.FOOD, context.get(), 4)
                .pattern("P P")
                .pattern(" P ")
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", provider.has(Items.PAPER))
                .save(provider, BelgianSnacks.asResource("crafting/paper_cone")))
            .register(),
        CONE_OF_FRIES = snack("cone_of_fries", "Cone of Fries", BSFoods.CONE_OF_FRIES).register(),
        CONE_OF_FRIES_BEEF_TALLOW = snack("cone_of_fries_beef_tallow", "Cone of Beef Tallow Fries", BSFoods.CONE_OF_FRIES_BEEF_TALLOW)
            .register();

    // v2 base ingredients (docs/13, section 5.7). Ovens are Create fans on a campfire: smoking recipes.
    public static final ItemEntry<Item>
        PEARL_SUGAR = ingredient("pearl_sugar", "Pearl Sugar").register(),
        CASSONADE = ingredient("cassonade", "Cassonade").register(),
        POWDERED_SUGAR = ingredient("powdered_sugar", "Powdered Sugar").tag(BSTags.POWDERED_SUGARS).register(),
        SALT = ingredient("salt", "Salt").tag(BSTags.SALTS).register(),
        BUTTER = ingredient("butter", "Butter").tag(BSTags.BUTTERS).register(),
        YOUNG_HERVE_CHEESE = ingredient("young_herve_cheese", "Young Herve Cheese").register(),
        HERVE_CHEESE = food("herve_cheese", "Herve Cheese", BSFoods.HERVE_CHEESE).tag(BSTags.C_FOODS_CHEESE).register(),
        MATON = ingredient("maton", "Maton").register(),
        BREWERS_YEAST = ingredient("brewers_yeast", "Brewer's Yeast").register(),
        SPROUTED_GRAIN = ingredient("sprouted_grain", "Sprouted Grain").register(),
        MALT = ingredient("malt", "Malt").recipe((context, provider) -> cook(context, provider, Ingredient.of(SPROUTED_GRAIN.get()), "malt", false)).register(),
        PISTOLET_DOUGH = ingredient("pistolet_dough", "Pistolet Dough").register(),
        PISTOLET = food("pistolet", "Pistolet", BSFoods.PISTOLET).recipe((context, provider) -> cook(context, provider, Ingredient.of(PISTOLET_DOUGH.get()), "pistolet", true)).register(),
        PUFF_PASTRY = ingredient("puff_pastry", "Puff Pastry").register(),
        BRIOCHE_DOUGH = ingredient("brioche_dough", "Brioche Dough").register(),
        BURGER_BUN_DOUGH = ingredient("burger_bun_dough", "Burger Bun Dough").register(),
        BURGER_BUN = ingredient("burger_bun", "Burger Bun").recipe((context, provider) -> cook(context, provider, Ingredient.of(BURGER_BUN_DOUGH.get()), "burger_bun", true)).register(),
        PITA_DOUGH = ingredient("pita_dough", "Pita Dough").register(),
        RAW_PITA = ingredient("raw_pita", "Raw Pita").register(),
        PITA_BREAD = ingredient("pita_bread", "Pita Bread").recipe((context, provider) -> cook(context, provider, Ingredient.of(RAW_PITA.get()), "pita_bread", true)).register(),
        NOODLES = ingredient("noodles", "Noodles").tag(BSTags.NOODLES).register(),
        LARDONS = ingredient("lardons", "Lardons").tag(BSTags.LARDONS).register(),
        BRINED_HAM = ingredient("brined_ham", "Brined Ham").register(),
        ARDENNES_HAM = food("ardennes_ham", "Ardennes Ham", BSFoods.ARDENNES_HAM).tag(BSTags.HAMS).recipe((context, provider) -> cook(context, provider, Ingredient.of(BRINED_HAM.get()), "ardennes_ham", false)).register(),
        RAW_MERINGUE = ingredient("raw_meringue", "Raw Meringue").register(),
        MERINGUE = food("meringue", "Meringue", BSFoods.MERINGUE).recipe((context, provider) -> cook(context, provider, Ingredient.of(RAW_MERINGUE.get()), "meringue", false)).register(),
        RAISINS = food("raisins", "Raisins", BSFoods.RAISINS).recipe(BSItems::raisins).register(),
        APPLE_COMPOTE = food("apple_compote", "Apple Compote", BSFoods.APPLE_COMPOTE).properties(p -> p.stacksTo(16)).register(),
        CHOCOLATE_SHAVINGS = ingredient("chocolate_shavings", "Chocolate Shavings").register(),
        VANILLA_ICE_CREAM = food("vanilla_ice_cream", "Vanilla Ice Cream", BSFoods.VANILLA_ICE_CREAM).tag(BSTags.VANILLA_ICE_CREAMS).register();

    /** The sauces a cone can carry, id suffix -> English name; the fluids are BSFluids.CONE_SAUCES. */
    public static final Map<String, String> CONE_SAUCES = new LinkedHashMap<>();

    static {
        CONE_SAUCES.put("mayonnaise", "Mayonnaise");
        CONE_SAUCES.put("curry_ketchup", "Curry Ketchup");
        CONE_SAUCES.put("andalouse", "Andalouse");
        CONE_SAUCES.put("samourai", "Samurai");
        CONE_SAUCES.put("americaine", "Américaine");
        CONE_SAUCES.put("sauce_lapin", "Liège Sauce");
        CONE_SAUCES.put("tartare", "Tartare");
        CONE_SAUCES.put("pickles", "Pickles");
        CONE_SAUCES.put("brazil", "Brazil");
    }

    // v2 sauced cones (section 5.2): a Spout puts 50 mB of sauce on a plain cone.
    public static final Map<String, ItemEntry<Item>> SAUCED_CONES = sauced("cone_of_fries", "Cone of Fries", BSFoods.CONE_OF_FRIES_SAUCED);
    public static final Map<String, ItemEntry<Item>> SAUCED_TALLOW_CONES =
        sauced("cone_of_fries_beef_tallow", "Cone of Beef Tallow Fries", BSFoods.CONE_OF_FRIES_BEEF_TALLOW_SAUCED);

    private BSItems() {
    }

    private static ItemBuilder<Item, ?> ingredient(String name, String englishName) {
        return REGISTRATE.item(name, Item::new).lang(englishName);
    }

    private static <T extends FricadelleItem> ItemBuilder<T, ?> fricadelle(String name, String englishName, FoodProperties food,
                                                                        NonNullFunction<Item.Properties, T> factory) {
        return REGISTRATE.item(name, factory)
            .lang(englishName)
            .properties(p -> p.food(food))
            .tag(Tags.Items.FOODS_COOKED_MEAT, BSTags.GRINDER_BLACKLIST);
    }

    private static Map<String, ItemEntry<Item>> sauced(String cone, String englishCone, FoodProperties food) {
        Map<String, ItemEntry<Item>> cones = new LinkedHashMap<>();
        CONE_SAUCES.forEach((sauce, englishSauce) ->
            cones.put(sauce, snack(cone + "_" + sauce, englishCone + " with " + englishSauce, food).register()));
        return Collections.unmodifiableMap(cones);
    }

    // A v2 ingredient that can be eaten as it is; finished food, so the Supreme Grinder counts it.
    private static ItemBuilder<Item, ?> food(String name, String englishName, FoodProperties food) {
        return REGISTRATE.item(name, Item::new).lang(englishName).properties(p -> p.food(food));
    }

    // Smoked by a fan on a campfire (Create's bulk smoking runs vanilla smoking recipes); breads also bake in a furnace.
    private static void cook(DataGenContext<Item, Item> context, RegistrateRecipeProvider provider, Ingredient input, String name,
                             boolean alsoSmelting) {
        SimpleCookingRecipeBuilder.smoking(input, RecipeCategory.FOOD, context.get(), 0.1f, 100)
            .unlockedBy("has_" + name, RegistrateRecipeProvider.has(context.get()))
            .save(provider, BelgianSnacks.asResource("smoking/" + name));
        if (alsoSmelting) {
            SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.FOOD, context.get(), 0.1f, 200)
                .unlockedBy("has_" + name, RegistrateRecipeProvider.has(context.get()))
                .save(provider, BelgianSnacks.asResource("smelting/" + name));
        }
    }

    // Grapes from the pack when there are some; sweet berries otherwise.
    private static void raisins(DataGenContext<Item, Item> context, RegistrateRecipeProvider provider) {
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(BSTags.GRAPES), RecipeCategory.FOOD, context.get(), 0.1f, 100)
            .unlockedBy("has_raisins", RegistrateRecipeProvider.has(context.get()))
            .save(provider.withConditions(new NotCondition(new TagEmptyCondition(BSTags.GRAPES))), BelgianSnacks.asResource("smoking/raisins"));
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(Items.SWEET_BERRIES), RecipeCategory.FOOD, context.get(), 0.1f, 100)
            .unlockedBy("has_raisins", RegistrateRecipeProvider.has(context.get()))
            .save(provider.withConditions(new TagEmptyCondition(BSTags.GRAPES)), BelgianSnacks.asResource("smoking/raisins_from_sweet_berries"));
    }

    // A snack of the v2 menu: food, in c:foods/fries, counted by the Supreme Grinder.
    @SafeVarargs
    private static ItemBuilder<Item, ?> snack(String name, String englishName, FoodProperties food,
                                              TagKey<Item>... extraTags) {
        ItemBuilder<Item, ?> builder = REGISTRATE.item(name, Item::new)
            .lang(englishName)
            .properties(p -> p.food(food));
        TagKey<Item>[] tags = Arrays.copyOf(extraTags, extraTags.length + 1);
        tags[extraTags.length] = BSTags.C_FOODS_FRIES;
        return builder.tag(tags);
    }

    // Create's ItemDescription reads "<description id>.tooltip.summary" and shows it behind [Shift].
    public static String tooltipKey(String name) {
        return "item." + BelgianSnacks.MOD_ID + "." + name + ".tooltip.summary";
    }

    public static void register() {
        REGISTRATE.addRawLang(tooltipKey("fricadelle"), "Nobody really knows what's inside.");
        REGISTRATE.addRawLang(tooltipKey("the_fricadelle"), "Make it a spéciale, please.");
        REGISTRATE.addRawLang(tooltipKey("ultimate_fricadelle"), "It literally contains everything.");
        REGISTRATE.addRawLang(tooltipKey("fries"), "Fried twice, as the law demands.");
        REGISTRATE.addRawLang(tooltipKey("fries_beef_tallow"), "Fried in beef fat, the way grandma did.");
        REGISTRATE.addRawLang(tooltipKey("cone_of_fries"), "Mayonnaise sold separately.");
        REGISTRATE.addRawLang(tooltipKey("cone_of_fries_beef_tallow"), "The real deal. Your cardiologist disagrees.");
        sauceTooltip("mayonnaise", "The Belgian classic. Yes, on fries.");
        sauceTooltip("curry_ketchup", "Sweet, spicy, very Belgian.");
        sauceTooltip("andalouse", "Mayonnaise that went on holiday to Spain.");
        sauceTooltip("samourai", "Hot. Keep a drink at hand.");
        sauceTooltip("americaine", "Not from America. Nobody knows why.");
        sauceTooltip("sauce_lapin", "Rabbit sauce, without the rabbit.");
        sauceTooltip("tartare", "Mayonnaise with pickles in it. Fancy.");
        sauceTooltip("pickles", "Yellow, sour, crunchy. On fries, yes.");
        sauceTooltip("brazil", "Pineapple in a sauce. Nobody in Brazil asked for it.");
        REGISTRATE.addRawLang(tooltipKey("herve_cheese"), "Your neighbours will know.");
        REGISTRATE.addRawLang(tooltipKey("pistolet"), "The Sunday morning bread roll.");
        REGISTRATE.addRawLang(tooltipKey("ardennes_ham"), "Smoked in the Ardennes. Or in front of a fan.");
    }

    // One line per sauce, on both cones that carry it.
    private static void sauceTooltip(String sauce, String line) {
        REGISTRATE.addRawLang(tooltipKey("cone_of_fries_" + sauce), line);
        REGISTRATE.addRawLang(tooltipKey("cone_of_fries_beef_tallow_" + sauce), line);
    }
}
