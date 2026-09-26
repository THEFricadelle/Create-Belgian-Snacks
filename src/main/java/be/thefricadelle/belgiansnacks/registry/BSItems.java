/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.Tags;

public final class BSItems {
    // Tier 1: Fricadelle
    public static final ItemEntry<Item>
        MINCED_PORK = ingredient("minced_pork", "Minced Pork").tag(BSTags.MINCED_MEATS).register(),
        MINCED_BEEF = ingredient("minced_beef", "Minced Beef").tag(BSTags.MINCED_MEATS, BSTags.MINCED_BEEF).register(),
        MINCED_CHICKEN = ingredient("minced_chicken", "Minced Chicken").tag(BSTags.MINCED_MEATS).register(),
        BEEF_TALLOW = ingredient("beef_tallow", "Beef Tallow").register(),
        BREAD_CRUMBS = ingredient("bread_crumbs", "Bread Crumbs").register(),
        FRICADELLE_PASTE = ingredient("fricadelle_paste", "Fricadelle Paste").tag(BSTags.GRINDER_BLACKLIST).register(),
        RAW_FRICADELLE = ingredient("raw_fricadelle", "Raw Fricadelle").tag(BSTags.GRINDER_BLACKLIST).register(),
        FRICADELLE = fricadelle("fricadelle", "Fricadelle", BSFoods.FRICADELLE).register();

    // Tier 2: THE_Fricadelle
    public static final ItemEntry<Item>
        BELGIAN_SPICES = ingredient("belgian_spices", "Belgian Spices").register(),
        EXCEPTIONAL_PASTE = ingredient("exceptional_paste", "Exceptional Paste").tag(BSTags.GRINDER_BLACKLIST).register(),
        RAW_THE_FRICADELLE = ingredient("raw_the_fricadelle", "Raw THE_Fricadelle").tag(BSTags.GRINDER_BLACKLIST).register(),
        THE_FRICADELLE = fricadelle("the_fricadelle", "THE_Fricadelle", BSFoods.THE_FRICADELLE).register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_THE_FRICADELLE =
        REGISTRATE.item("incomplete_the_fricadelle", SequencedAssemblyItem::new)
            .lang("Incomplete THE_Fricadelle")
            .tag(BSTags.GRINDER_BLACKLIST)
            .register();

    // Tier 3: THE_FRICADELLE
    public static final ItemEntry<Item>
        ABSOLUTE_PASTE = ingredient("absolute_paste", "Absolute Paste").tag(BSTags.GRINDER_BLACKLIST).register(),
        RAW_ULTIMATE_FRICADELLE = ingredient("raw_ultimate_fricadelle", "Raw THE_FRICADELLE").tag(BSTags.GRINDER_BLACKLIST).register(),
        ULTIMATE_FRICADELLE = fricadelle("ultimate_fricadelle", "THE_FRICADELLE", BSFoods.ULTIMATE_FRICADELLE)
            .properties(p -> p.rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
            .register();

    private BSItems() {
    }

    private static ItemBuilder<Item, ?> ingredient(String name, String englishName) {
        return REGISTRATE.item(name, Item::new).lang(englishName);
    }

    private static ItemBuilder<Item, ?> fricadelle(String name, String englishName, FoodProperties food) {
        return ingredient(name, englishName)
            .properties(p -> p.food(food))
            .tag(Tags.Items.FOODS_COOKED_MEAT, BSTags.GRINDER_BLACKLIST);
    }

    // Create's ItemDescription reads "<description id>.tooltip.summary" and shows it behind [Shift].
    public static String tooltipKey(String name) {
        return "item." + BelgianSnacks.MOD_ID + "." + name + ".tooltip.summary";
    }

    public static void register() {
        REGISTRATE.addRawLang(tooltipKey("fricadelle"), "Nobody really knows what's inside.");
        REGISTRATE.addRawLang(tooltipKey("the_fricadelle"), "Make it a spéciale, please.");
        REGISTRATE.addRawLang(tooltipKey("ultimate_fricadelle"), "It literally contains everything.");
    }
}
