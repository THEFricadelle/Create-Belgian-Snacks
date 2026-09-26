/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import com.tterrag.registrate.providers.ProviderType;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

public final class BSTags {
    public static final TagKey<Item> MINCED_MEATS = item("minced_meats");
    // Recipes read these instead of our items, so any mod's equivalent works (D12).
    public static final TagKey<Item> MINCED_PORK = item("minced_meats/pork");
    public static final TagKey<Item> MINCED_BEEF = item("minced_meats/beef");
    public static final TagKey<Item> MINCED_CHICKEN = item("minced_meats/chicken");
    public static final TagKey<Item> GRINDER_EXTRA_FOODS = item("grinder/extra_foods");
    public static final TagKey<Item> GRINDER_BLACKLIST = item("grinder/blacklist");
    // Fried one per batch instead of a whole stack (tier 3).
    public static final TagKey<Item> FRYER_ONE_AT_A_TIME = item("fryer/one_at_a_time");

    // Convention tags shared with Create: Food and Farmer's Delight.
    public static final TagKey<Item> C_GROUND_PORK = common("ground_pork");
    public static final TagKey<Item> C_GROUND_BEEF = common("ground_beef");
    public static final TagKey<Item> C_MINCED_BEEF = common("minced_beef");
    public static final TagKey<Item> C_GROUND_CHICKEN = common("ground_chicken");
    public static final TagKey<Item> C_BREAD_CRUMBS = common("bread_crumbs");
    public static final TagKey<Item> C_TOMATOES = common("crops/tomato");

    // Any fat tiers 1 and 2 can fry in, including other mods' plant and vegetable oils (D13).
    public static final TagKey<Fluid> FRYING_OILS = FluidTags.create(BelgianSnacks.asResource("frying_oils"));

    private BSTags() {
    }

    private static TagKey<Item> item(String path) {
        return ItemTags.create(BelgianSnacks.asResource(path));
    }

    private static TagKey<Item> common(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    // Entries that do not belong to one of our items; ours are tagged on their builder in BSItems.
    public static void register() {
        BelgianSnacks.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
            prov.addTag(MINCED_PORK).addOptionalTag(C_GROUND_PORK.location());
            // Farmer's Delight does not tag its own minced beef as c:minced_beef (Create: Food does), so it is listed directly.
            prov.addTag(MINCED_BEEF)
                .addOptional(ResourceLocation.fromNamespaceAndPath("farmersdelight", "minced_beef"))
                .addOptionalTag(C_GROUND_BEEF.location())
                .addOptionalTag(C_MINCED_BEEF.location());
            prov.addTag(MINCED_CHICKEN).addOptionalTag(C_GROUND_CHICKEN.location());
            prov.addTag(GRINDER_EXTRA_FOODS)
                .add(Items.CAKE);
            prov.addTag(GRINDER_BLACKLIST)
                .add(Items.ENCHANTED_GOLDEN_APPLE);
        });
    }
}
