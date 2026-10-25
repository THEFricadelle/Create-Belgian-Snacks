/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
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
import net.neoforged.neoforge.common.Tags;

public final class BSTags {
    public static final TagKey<Item> MINCED_MEATS = item("minced_meats");
    // Recipes read these instead of our items, so any mod's equivalent works (D12).
    public static final TagKey<Item> MINCED_PORK = item("minced_meats/pork");
    public static final TagKey<Item> MINCED_BEEF = item("minced_meats/beef");
    public static final TagKey<Item> MINCED_CHICKEN = item("minced_meats/chicken");
    public static final TagKey<Item> GRINDER_EXTRA_FOODS = item("grinder/extra_foods");
    public static final TagKey<Item> GRINDER_BLACKLIST = item("grinder/blacklist");
    // What a plain cone takes: our fries and other mods' (v2, section 5.1).
    public static final TagKey<Item> FRIES = item("fries");
    // What makes a samurai sauce hot; blaze powder stands in when no mod brings one (V-D5).
    public static final TagKey<Item> HOT_PEPPERS = item("hot_peppers");
    // v2 base ingredients (section 5.7): ours, plus what the pack already makes (M13 rule 2).
    public static final TagKey<Item> SALTS = item("salts");
    public static final TagKey<Item> BUTTERS = item("butters");
    public static final TagKey<Item> POWDERED_SUGARS = item("powdered_sugars");
    public static final TagKey<Item> MALTING_GRAINS = item("malting_grains");
    public static final TagKey<Item> LARDONS = item("lardons");
    public static final TagKey<Item> HAMS = item("hams");
    public static final TagKey<Item> NOODLES = item("noodles");
    public static final TagKey<Item> VANILLA_ICE_CREAMS = item("vanilla_ice_creams");
    public static final TagKey<Item> GRAPES = item("grapes");
    // Fried one per batch instead of a whole stack (tier 3).
    public static final TagKey<Item> FRYER_ONE_AT_A_TIME = item("fryer/one_at_a_time");

    // Convention tags shared with Create: Food and Farmer's Delight.
    public static final TagKey<Item> C_GROUND_PORK = common("ground_pork");
    public static final TagKey<Item> C_GROUND_BEEF = common("ground_beef");
    public static final TagKey<Item> C_MINCED_BEEF = common("minced_beef");
    public static final TagKey<Item> C_GROUND_CHICKEN = common("ground_chicken");
    public static final TagKey<Item> C_BREAD_CRUMBS = common("bread_crumbs");
    public static final TagKey<Item> C_TOMATOES = common("crops/tomato");
    public static final TagKey<Item> C_ONIONS = common("crops/onion");
    public static final TagKey<Item> C_POTATOES = common("crops/potato");
    public static final TagKey<Item> C_FOODS_FRIES = common("foods/fries");
    // What the pack makes already: our recipe for it turns off when the tag is filled.
    public static final TagKey<Item> C_SALT = common("salt");
    public static final TagKey<Item> C_BUTTER = common("butter");
    public static final TagKey<Item> C_FOODS_CHEESE = common("foods/cheese");

    // Any fat tiers 1 and 2 can fry in, including other mods' plant and vegetable oils (D13).
    public static final TagKey<Fluid> FRYING_OILS = FluidTags.create(BelgianSnacks.asResource("frying_oils"));
    // Every sauce of the friterie (v2, section 5.2): what a mitraillette or a burger takes.
    public static final TagKey<Fluid> SAUCES = FluidTags.create(BelgianSnacks.asResource("sauces"));

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
            // c:foods gathers the food subtags; ours joins it like NeoForge's own.
            prov.addTag(Tags.Items.FOODS).addTag(C_FOODS_FRIES);
            prov.addTag(SALTS).addOptionalTag(C_SALT.location()).addOptionalTag(common("dusts/salt").location());
            prov.addTag(BUTTERS).addOptionalTag(C_BUTTER.location()).addOptionalTag(common("foods/butter").location());
            prov.addTag(POWDERED_SUGARS).addOptional(ResourceLocation.fromNamespaceAndPath("createfood", "powdered_sugar"));
            prov.addTag(MALTING_GRAINS).add(Items.WHEAT).addOptionalTag(common("crops/barley").location());
            prov.addTag(LARDONS).addOptionalTag(common("foods/raw_bacon").location());
            prov.addTag(HAMS).add(Items.COOKED_PORKCHOP)
                .addOptional(ResourceLocation.fromNamespaceAndPath("farmersdelight", "ham"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("farmersdelight", "smoked_ham"));
            prov.addTag(NOODLES).addOptionalTag(common("raw_pasta").location());
            prov.addTag(GRAPES).addOptionalTag(common("foods/grape").location()).addOptionalTag(common("crops/grape").location());
            prov.addTag(Tags.Items.FOODS).addTag(C_FOODS_CHEESE);
            // My Nether's Delight (Arcadia) grows bullet peppers.
            prov.addTag(HOT_PEPPERS)
                .addOptional(ResourceLocation.fromNamespaceAndPath("mynethersdelight", "bullet_pepper"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "crops/chili_pepper"));
            // Burger Mod (Arcadia) makes fries too.
            prov.addTag(FRIES).addOptional(ResourceLocation.fromNamespaceAndPath("burgermod", "fries"));
            prov.addTag(GRINDER_EXTRA_FOODS)
                .add(Items.CAKE);
            // D8: one-off or cosmetic items, not foods anyone farms. Optional, so harmless outside Arcadia.
            // The enchanted golden apple stays in on purpose.
            prov.addTag(GRINDER_BLACKLIST)
                .add(Items.OMINOUS_BOTTLE)
                .addOptional(ResourceLocation.fromNamespaceAndPath("mynethersdelight", "enchanted_golden_egg"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("artifacts", "everlasting_beef"));
        });
    }
}
