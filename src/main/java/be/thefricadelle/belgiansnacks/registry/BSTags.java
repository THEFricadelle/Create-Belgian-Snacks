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
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class BSTags {
    public static final TagKey<Item> MINCED_MEATS = item("minced_meats");
    // Ours plus Farmer's Delight's, so recipes accept either (D12).
    public static final TagKey<Item> MINCED_BEEF = item("minced_meats/beef");
    public static final TagKey<Item> GRINDER_EXTRA_FOODS = item("grinder/extra_foods");
    public static final TagKey<Item> GRINDER_BLACKLIST = item("grinder/blacklist");

    private BSTags() {
    }

    private static TagKey<Item> item(String path) {
        return ItemTags.create(BelgianSnacks.asResource(path));
    }

    // Entries that do not belong to one of our items; ours are tagged on their builder in BSItems.
    public static void register() {
        BelgianSnacks.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
            prov.addTag(MINCED_BEEF)
                .addOptional(ResourceLocation.fromNamespaceAndPath("farmersdelight", "minced_beef"));
            prov.addTag(GRINDER_EXTRA_FOODS)
                .add(Items.CAKE);
            prov.addTag(GRINDER_BLACKLIST)
                .add(Items.ENCHANTED_GOLDEN_APPLE);
        });
    }
}
