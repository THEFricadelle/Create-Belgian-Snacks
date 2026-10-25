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
import net.minecraft.data.recipes.RecipeCategory;
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

    /** The sauces a cone can carry, id suffix -> English name; the fluids are BSFluids.CONE_SAUCES. */
    public static final Map<String, String> CONE_SAUCES = new LinkedHashMap<>();

    static {
        CONE_SAUCES.put("mayonnaise", "Mayonnaise");
        CONE_SAUCES.put("curry_ketchup", "Curry Ketchup");
        CONE_SAUCES.put("andalouse", "Andalouse");
        CONE_SAUCES.put("samourai", "Samurai");
        CONE_SAUCES.put("americaine", "Américaine");
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
    }

    // One line per sauce, on both cones that carry it.
    private static void sauceTooltip(String sauce, String line) {
        REGISTRATE.addRawLang(tooltipKey("cone_of_fries_" + sauce), line);
        REGISTRATE.addRawLang(tooltipKey("cone_of_fries_beef_tallow_" + sauce), line);
    }
}
