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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import be.thefricadelle.belgiansnacks.BelgianSnacks;

/**
 * v2 friterie snacks (docs/13, sections 5.3 to 5.5 and 5.10): what a Belgian frituur serves beside
 * its fries. Raw items go through the fryer; finished snacks are food, so the Supreme Grinder counts
 * them (V-D1). Transitional items of sequenced assembly are blacklisted like tiers 2 and 3.
 */
public final class BSSnacks {
    /** French name of every item of this class, read by the French language provider. */
    public static final Map<ItemEntry<? extends Item>, String> FRENCH = new LinkedHashMap<>();

    // Mitraillette: half a baguette, a meat, fries and a sauce.
    public static final ItemEntry<Item>
        HALF_BAGUETTE = ingredient("half_baguette", "Half Baguette", "Demi-baguette");
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_MITRAILLETTE = transitional("incomplete_mitraillette", "Incomplete Mitraillette", "Mitraillette en cours");
    public static final ItemEntry<Item>
        MITRAILLETTE = snack("mitraillette", "Mitraillette", "Mitraillette", BSFoods.MITRAILLETTE);

    // Boulets: Liège meatballs, alone or in their sauce.
    public static final ItemEntry<Item>
        BOULET_PASTE = ingredient("boulet_paste", "Boulet Paste", "Pâte à boulets"),
        RAW_BOULET = ingredient("raw_boulet", "Raw Boulet", "Boulet cru"),
        BOULET = snack("boulet", "Boulet", "Boulet", BSFoods.BOULET, Tags.Items.FOODS_COOKED_MEAT),
        BOULETS_LIEGEOIS = snack("boulets_liegeois", "Boulets Liégeois", "Boulets à la liégeoise", BSFoods.BOULETS_LIEGEOIS, b -> b.properties(p -> p.stacksTo(16)));

    // Breaded snacks share one transitional item: Create tells them apart by the recipe it stores.
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_BREADED = transitional("incomplete_breaded", "Breading in Progress", "Panure en cours");
    public static final ItemEntry<Item>
        SHRIMP_CROQUETTE_MIX = ingredient("shrimp_croquette_mix", "Shrimp Croquette Filling", "Appareil à croquettes de crevettes"),
        CHEESE_CROQUETTE_MIX = ingredient("cheese_croquette_mix", "Cheese Croquette Filling", "Appareil à croquettes au fromage"),
        CHICKEN_CROQUETTE_MIX = ingredient("chicken_croquette_mix", "Chicken Croquette Filling", "Appareil à croquettes de poulet"),
        LUCIFER_MIX = ingredient("lucifer_mix", "Lucifer Filling", "Appareil à lucifer"),
        POTATO_CROQUETTE_MIX = ingredient("potato_croquette_mix", "Potato Croquette Filling", "Appareil à croquettes de pommes de terre"),
        CHICKEN_STRIPS = ingredient("chicken_strips", "Chicken Strips", "Aiguillettes de poulet"),
        FISH_STRIPS = ingredient("fish_strips", "Fish Strips", "Bâtonnets de poisson crus"),
        CHEESE_STRIPS = ingredient("cheese_strips", "Cheese Strips", "Bâtonnets de fromage crus"),
        RAW_SHRIMP_CROQUETTE = ingredient("raw_shrimp_croquette", "Raw Shrimp Croquette", "Croquette de crevettes crue"),
        RAW_CHEESE_CROQUETTE = ingredient("raw_cheese_croquette", "Raw Cheese Croquette", "Croquette au fromage crue"),
        RAW_CHICKEN_CROQUETTE = ingredient("raw_chicken_croquette", "Raw Chicken Croquette", "Croquette de poulet crue"),
        RAW_LUCIFER = ingredient("raw_lucifer", "Raw Lucifer", "Lucifer cru"),
        RAW_POTATO_CROQUETTE = ingredient("raw_potato_croquette", "Raw Potato Croquette", "Croquette de pommes de terre crue"),
        RAW_CHICKEN_FINGERS = ingredient("raw_chicken_fingers", "Raw Chicken Fingers", "Chicken fingers crus"),
        RAW_FISH_STICKS = ingredient("raw_fish_sticks", "Raw Fish Sticks", "Bâtonnets de poisson panés crus"),
        RAW_CHEESE_STICKS = ingredient("raw_cheese_sticks", "Raw Cheese Sticks", "Bâtonnets de fromage panés crus"),
        SHRIMP_CROQUETTE = snack("shrimp_croquette", "Shrimp Croquette", "Croquette de crevettes", BSFoods.CROQUETTE, Tags.Items.FOODS_COOKED_FISH),
        CHEESE_CROQUETTE = snack("cheese_croquette", "Cheese Croquette", "Croquette au fromage", BSFoods.CROQUETTE),
        CHICKEN_CROQUETTE = snack("chicken_croquette", "Chicken Croquette", "Croquette de poulet", BSFoods.CROQUETTE, Tags.Items.FOODS_COOKED_MEAT),
        LUCIFER = snack("lucifer", "Lucifer", "Lucifer", BSFoods.LUCIFER, Tags.Items.FOODS_COOKED_MEAT),
        POTATO_CROQUETTE = snack("potato_croquette", "Potato Croquette", "Croquette de pommes de terre", BSFoods.POTATO_CROQUETTE),
        CHICKEN_FINGERS = snack("chicken_fingers", "Chicken Fingers", "Chicken fingers", BSFoods.CROQUETTE, Tags.Items.FOODS_COOKED_MEAT),
        FISH_STICKS = snack("fish_sticks", "Fish Sticks", "Bâtonnets de poisson", BSFoods.CROQUETTE, Tags.Items.FOODS_COOKED_FISH),
        CHEESE_STICKS = snack("cheese_sticks", "Cheese Sticks", "Bâtonnets de fromage", BSFoods.POTATO_CROQUETTE);

    // The sausages of the frituur: mixed, pressed into shape, fried.
    public static final ItemEntry<Item>
        CERVELAS_MIX = ingredient("cervelas_mix", "Cervelas Mix", "Hachis de cervelas"),
        MEXICANO_MIX = ingredient("mexicano_mix", "Mexicano Mix", "Hachis de mexicano"),
        VIANDELLE_MIX = ingredient("viandelle_mix", "Viandelle Mix", "Hachis de viandelle"),
        RAW_CERVELAS = ingredient("raw_cervelas", "Raw Cervelas", "Cervelas cru"),
        RAW_MEXICANO = ingredient("raw_mexicano", "Raw Mexicano", "Mexicano cru"),
        RAW_VIANDELLE = ingredient("raw_viandelle", "Raw Viandelle", "Viandelle crue"),
        CERVELAS = snack("cervelas", "Cervelas", "Cervelas", BSFoods.SAUSAGE, Tags.Items.FOODS_COOKED_MEAT),
        MEXICANO = snack("mexicano", "Mexicano", "Mexicano", BSFoods.SAUSAGE, Tags.Items.FOODS_COOKED_MEAT),
        VIANDELLE = snack("viandelle", "Viandelle", "Viandelle", BSFoods.SAUSAGE, Tags.Items.FOODS_COOKED_MEAT);

    // Loempia: a spring roll, the frituur's way.
    public static final ItemEntry<Item>
        LOEMPIA_WRAPPER = ingredient("loempia_wrapper", "Loempia Wrapper", "Feuille de loempia");
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_LOEMPIA = transitional("incomplete_loempia", "Incomplete Loempia", "Loempia en cours");
    public static final ItemEntry<Item>
        RAW_LOEMPIA = ingredient("raw_loempia", "Raw Loempia", "Loempia cru"),
        LOEMPIA = snack("loempia", "Loempia", "Loempia", BSFoods.SAUSAGE);

    // Filet américain: raw seasoned beef, on a pistolet, then with fries.
    public static final ItemEntry<Item>
        FILET_AMERICAIN = snack("filet_americain", "Filet Américain", "Filet américain", BSFoods.FILET_AMERICAIN),
        PISTOLET_AMERICAIN = snack("pistolet_americain", "Pistolet Américain", "Pistolet américain", BSFoods.PISTOLET_AMERICAIN),
        AMERICAIN_FRITES = snack("americain_frites", "Américain Frites", "Américain frites", BSFoods.AMERICAIN_FRITES);

    // Burgers of the frituur.
    public static final ItemEntry<Item>
        RAW_PATTY = ingredient("raw_patty", "Raw Patty", "Steak haché cru"),
        PATTY = snack("patty", "Patty", "Steak haché", BSFoods.PATTY, Tags.Items.FOODS_COOKED_MEAT);
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_BURGER = transitional("incomplete_burger", "Incomplete Burger", "Burger en cours");
    public static final ItemEntry<Item>
        FRITERIE_BURGER = snack("friterie_burger", "Friterie Burger", "Burger de friterie", BSFoods.BURGER),
        FRITERIE_CHEESEBURGER = snack("friterie_cheeseburger", "Friterie Cheeseburger", "Cheeseburger de friterie", BSFoods.CHEESEBURGER);

    // Hot dog: a long bun, a sausage, fried onions and a sauce.
    public static final ItemEntry<Item>
        HOT_DOG_BUN_DOUGH = ingredient("hot_dog_bun_dough", "Hot Dog Bun Dough", "Pâton de pain à hot-dog"),
        HOT_DOG_BUN = REGISTRATE.item("hot_dog_bun", Item::new).lang("Hot Dog Bun")
            .recipe((context, provider) -> cook(provider, context.get(), Ingredient.of(BSSnacks.HOT_DOG_BUN_DOUGH.get()), "hot_dog_bun"))
            .register();

    static {
        FRENCH.put(HOT_DOG_BUN, "Pain à hot-dog");
    }

    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_HOT_DOG = transitional("incomplete_hot_dog", "Incomplete Hot Dog", "Hot-dog en cours");
    public static final ItemEntry<Item>
        HOT_DOG = snack("hot_dog", "Hot Dog", "Hot-dog", BSFoods.HOT_DOG);

    // Pita: gyros cooked in the mixer, in a pita bread.
    public static final ItemEntry<Item>
        GYROS = snack("gyros", "Gyros", "Gyros", BSFoods.PATTY, Tags.Items.FOODS_COOKED_MEAT);
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_PITA = transitional("incomplete_pita", "Incomplete Pita", "Pita en cours");
    public static final ItemEntry<Item>
        PITA = snack("pita", "Pita", "Pita", BSFoods.BURGER);

    // Brochette: pork on a stick.
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_BROCHETTE = transitional("incomplete_brochette", "Incomplete Brochette", "Brochette en cours");
    public static final ItemEntry<Item>
        RAW_BROCHETTE = ingredient("raw_brochette", "Raw Brochette", "Brochette crue"),
        BROCHETTE = snack("brochette", "Brochette", "Brochette", BSFoods.BROCHETTE, Tags.Items.FOODS_COOKED_MEAT);

    // Bamischijf and nasischijf: a breaded disc of noodles or rice.
    public static final ItemEntry<Item>
        BAMI_MIX = ingredient("bami_mix", "Bami Mix", "Appareil à bami"),
        NASI_MIX = ingredient("nasi_mix", "Nasi Mix", "Appareil à nasi"),
        RAW_BAMI_DISC = ingredient("raw_bami_disc", "Raw Bamischijf", "Bamischijf cru"),
        RAW_NASI_DISC = ingredient("raw_nasi_disc", "Raw Nasischijf", "Nasischijf cru"),
        BAMI_DISC = snack("bami_disc", "Bamischijf", "Bamischijf", BSFoods.SAUSAGE),
        NASI_DISC = snack("nasi_disc", "Nasischijf", "Nasischijf", BSFoods.SAUSAGE);

    // Calamari rings; only in packs with squid to cut.
    public static final ItemEntry<Item>
        RAW_SQUID_RINGS = ingredient("raw_squid_rings", "Raw Calamari Rings", "Anneaux de calamar crus"),
        SQUID_RINGS = snack("squid_rings", "Calamari Rings", "Calamars frits", BSFoods.CROQUETTE, Tags.Items.FOODS_COOKED_FISH);

    // Chicken: nuggets, crunchy chicken in corn flakes, wings.
    public static final ItemEntry<Item>
        RAW_CHICKEN_NUGGETS = ingredient("raw_chicken_nuggets", "Raw Chicken Nuggets", "Nuggets de poulet crus"),
        CHICKEN_NUGGETS = snack("chicken_nuggets", "Chicken Nuggets", "Nuggets de poulet", BSFoods.NUGGETS, Tags.Items.FOODS_COOKED_MEAT),
        CORN_FLAKES = ingredient("corn_flakes", "Corn Flakes", "Flocons de maïs"),
        RAW_CHICKEN_CRUNCH = ingredient("raw_chicken_crunch", "Raw Chicken Crunch", "Croquant de poulet cru"),
        CHICKEN_CRUNCH = snack("chicken_crunch", "Chicken Crunch", "Croquant de poulet", BSFoods.CHICKEN_CRUNCH, Tags.Items.FOODS_COOKED_MEAT),
        RAW_CHICKEN_WINGS = ingredient("raw_chicken_wings", "Raw Chicken Wings", "Ailes de poulet crues"),
        FRIED_CHICKEN_WINGS = snack("fried_chicken_wings", "Fried Chicken Wings", "Ailes de poulet frites", BSFoods.CROQUETTE, Tags.Items.FOODS_COOKED_MEAT),
        SPICY_CHICKEN_WINGS = snack("spicy_chicken_wings", "Spicy Chicken Wings", "Ailes de poulet épicées", BSFoods.CHICKEN_CRUNCH, Tags.Items.FOODS_COOKED_MEAT);

    // Onion rings in a blonde beer batter.
    public static final ItemEntry<Item>
        RAW_ONION_RINGS = ingredient("raw_onion_rings", "Raw Onion Rings", "Rondelles d'oignon"),
        BATTERED_ONION_RINGS = ingredient("battered_onion_rings", "Battered Onion Rings", "Rondelles d'oignon en pâte"),
        ONION_RINGS = snack("onion_rings", "Onion Rings", "Onion rings", BSFoods.NUGGETS);

    /** Raw item -> fried item, one frying recipe each (BSFryingRecipeGen). */
    public static final Map<ItemEntry<Item>, ItemEntry<Item>> FRIED = fried();

    private BSSnacks() {
    }

    private static Map<ItemEntry<Item>, ItemEntry<Item>> fried() {
        Map<ItemEntry<Item>, ItemEntry<Item>> map = new LinkedHashMap<>();
        map.put(RAW_BOULET, BOULET);
        map.put(RAW_SHRIMP_CROQUETTE, SHRIMP_CROQUETTE);
        map.put(RAW_CHEESE_CROQUETTE, CHEESE_CROQUETTE);
        map.put(RAW_CHICKEN_CROQUETTE, CHICKEN_CROQUETTE);
        map.put(RAW_LUCIFER, LUCIFER);
        map.put(RAW_POTATO_CROQUETTE, POTATO_CROQUETTE);
        map.put(RAW_CHICKEN_FINGERS, CHICKEN_FINGERS);
        map.put(RAW_FISH_STICKS, FISH_STICKS);
        map.put(RAW_CHEESE_STICKS, CHEESE_STICKS);
        map.put(RAW_CERVELAS, CERVELAS);
        map.put(RAW_MEXICANO, MEXICANO);
        map.put(RAW_VIANDELLE, VIANDELLE);
        map.put(RAW_LOEMPIA, LOEMPIA);
        map.put(RAW_PATTY, PATTY);
        map.put(RAW_BROCHETTE, BROCHETTE);
        map.put(RAW_BAMI_DISC, BAMI_DISC);
        map.put(RAW_NASI_DISC, NASI_DISC);
        map.put(RAW_SQUID_RINGS, SQUID_RINGS);
        map.put(RAW_CHICKEN_NUGGETS, CHICKEN_NUGGETS);
        map.put(RAW_CHICKEN_CRUNCH, CHICKEN_CRUNCH);
        map.put(RAW_CHICKEN_WINGS, FRIED_CHICKEN_WINGS);
        map.put(BATTERED_ONION_RINGS, ONION_RINGS);
        return Collections.unmodifiableMap(map);
    }

    private static ItemEntry<Item> ingredient(String name, String english, String french) {
        return french(REGISTRATE.item(name, Item::new).lang(english).register(), french);
    }

    private static ItemEntry<SequencedAssemblyItem> transitional(String name, String english, String french) {
        return french(REGISTRATE.item(name, SequencedAssemblyItem::new).lang(english).tag(BSTags.GRINDER_BLACKLIST).register(), french);
    }

    @SafeVarargs
    private static ItemEntry<Item> snack(String name, String english, String french, FoodProperties food, TagKey<Item>... tags) {
        return snack(name, english, french, food, b -> b.tag(tags));
    }

    // Every finished snack is in c:foods, directly or through a subtag NeoForge already gathers there.
    private static ItemEntry<Item> snack(String name, String english, String french, FoodProperties food,
                                         java.util.function.UnaryOperator<ItemBuilder<Item, ?>> extra) {
        ItemBuilder<Item, ?> builder = REGISTRATE.item(name, Item::new).lang(english).properties(p -> p.food(food)).tag(Tags.Items.FOODS);
        return french(extra.apply(builder).register(), french);
    }

    private static <T extends Item> ItemEntry<T> french(ItemEntry<T> entry, String french) {
        FRENCH.put(entry, french);
        return entry;
    }

    // Baked like the other breads: a fan on a campfire, or a furnace.
    static void cook(RegistrateRecipeProvider provider, Item result, Ingredient input, String name) {
        SimpleCookingRecipeBuilder.smoking(input, RecipeCategory.FOOD, result, 0.1f, 100)
            .unlockedBy("has_" + name, RegistrateRecipeProvider.has(result))
            .save(provider, BelgianSnacks.asResource("smoking/" + name));
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.FOOD, result, 0.1f, 200)
            .unlockedBy("has_" + name, RegistrateRecipeProvider.has(result))
            .save(provider, BelgianSnacks.asResource("smelting/" + name));
    }

    public static void register() {
        REGISTRATE.addRawLang(BSItems.tooltipKey("mitraillette"), "Half a baguette, a meat, fries, a sauce. Lunch.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("boulets_liegeois"), "Liège meatballs drowned in their sauce.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("lucifer"), "A croquette that bites back.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("viandelle"), "Long, thin, mysterious. A friterie classic.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("americain_frites"), "Raw beef and fries. Trust the Belgians.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("bami_disc"), "Noodles in breadcrumbs. Only in Flanders.");
    }
}
