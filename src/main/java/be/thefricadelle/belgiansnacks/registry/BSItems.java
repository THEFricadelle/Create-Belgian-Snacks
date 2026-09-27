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
import com.tterrag.registrate.util.nullness.NonNullFunction;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FricadelleItem;
import be.thefricadelle.belgiansnacks.content.food.UltimateFricadelleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
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
