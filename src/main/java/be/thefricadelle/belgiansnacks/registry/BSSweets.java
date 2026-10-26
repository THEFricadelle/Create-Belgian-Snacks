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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.DrinkItem;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

/**
 * v2 sweets (docs/13, section 5.12): biscuits, sweets, pralines, pastries, tarts and a hot chocolate.
 * Oven goods bake in front of a fan on a campfire. Finished sweets are food, counted by the Supreme Grinder.
 */
public final class BSSweets {
    /** French name of every item of this class, read by the French language provider. */
    public static final Map<ItemEntry<? extends Item>, String> FRENCH = new LinkedHashMap<>();

    // Speculoos and its spread.
    public static final ItemEntry<Item>
        SPECULOOS_DOUGH = ingredient("speculoos_dough", "Speculoos Dough", "Pâte à speculoos"),
        RAW_SPECULOOS = ingredient("raw_speculoos", "Raw Speculoos", "Speculoos cru");
    public static final ItemEntry<Item>
        SPECULOOS = baked("speculoos", "Speculoos", "Speculoos", BSFoods.SMALL_SWEET, () -> RAW_SPECULOOS),
        SPECULOOS_CRUMBS = ingredient("speculoos_crumbs", "Speculoos Crumbs", "Miettes de speculoos"),
        PISTOLET_SPECULOOS = food("pistolet_speculoos", "Pistolet with Speculoos Spread", "Pistolet au speculoos", BSFoods.PASTRY_LARGE),
        BRUSSELS_WAFFLE_SPECULOOS = food("brussels_waffle_speculoos", "Brussels Waffle with Speculoos Spread", "Gaufre de Bruxelles au speculoos",
            BSFoods.PASTRY_LARGE);

    // Sweets and chocolates.
    public static final ItemEntry<Item>
        CUBERDON = food("cuberdon", "Cuberdon", "Cuberdon", BSFoods.SMALL_SWEET),
        PRALINE = food("praline", "Praline", "Praline", BSFoods.SMALL_SWEET, BSTags.PRALINES),
        PRALINE_KRIEK = food("praline_kriek", "Kriek Praline", "Praline à la kriek", BSFoods.SMALL_SWEET, BSTags.PRALINES),
        BALLOTIN = REGISTRATE.item("ballotin", Item::new).lang("Ballotin")
            .properties(p -> p.food(BSFoods.BALLOTIN))
            .tag(Tags.Items.FOODS)
            .recipe((context, provider) -> ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, context.get())
                .pattern("PPP")
                .pattern("PXP")
                .pattern("PPP")
                .define('P', BSTags.PRALINES)
                .define('X', Items.PAPER)
                .unlockedBy("has_praline", provider.has(BSTags.PRALINES))
                .save(provider, BelgianSnacks.asResource("crafting/ballotin")))
            .register(),
        BABELUTTE = food("babelutte", "Babelutte", "Babelutte", BSFoods.SMALL_SWEET);

    static {
        FRENCH.put(BALLOTIN, "Ballotin");
    }

    // Fried pastries.
    public static final ItemEntry<Item>
        CROUSTILLON_DOUGH = ingredient("croustillon_dough", "Croustillon Dough", "Pâte à croustillons"),
        CROUSTILLONS = food("croustillons", "Croustillons", "Croustillons", BSFoods.PASTRY),
        CROUSTILLONS_SUGARED = food("croustillons_sugared", "Croustillons with Powdered Sugar", "Croustillons au sucre glace", BSFoods.PASTRY_LARGE),
        BOUQUETTE_BATTER = ingredient("bouquette_batter", "Bouquette Batter", "Pâte à bouquettes"),
        BOUQUETTE = food("bouquette", "Bouquette", "Bouquette", BSFoods.PASTRY),
        BOUQUETTE_CASSONADE = food("bouquette_cassonade", "Bouquette with Cassonade", "Bouquette à la cassonade", BSFoods.PASTRY_LARGE);

    // Oven goods.
    public static final ItemEntry<Item>
        COUQUE_DOUGH = ingredient("couque_dough", "Couque Dough", "Pâte à couque"),
        RAW_COUQUE = ingredient("raw_couque", "Raw Couque", "Couque crue");
    public static final ItemEntry<Item>
        // Hard as stone: let it melt, four times as long to eat.
        COUQUE_DE_DINANT = baked("couque_de_dinant", "Couque de Dinant", "Couque de Dinant", BSFoods.slow(BSFoods.PASTRY_LARGE, 6.4f), () -> RAW_COUQUE),
        RICE_FILLING = ingredient("rice_filling", "Rice Pudding Filling", "Appareil à tarte au riz"),
        RAW_TARTE_AU_RIZ = ingredient("raw_tarte_au_riz", "Unbaked Rice Tart", "Tarte au riz crue"),
        RAW_TARTE_AU_SUCRE = ingredient("raw_tarte_au_sucre", "Unbaked Sugar Tart", "Tarte au sucre crue"),
        RAW_MATTENTAART = ingredient("raw_mattentaart", "Unbaked Mattentaart", "Mattentaart crue");
    public static final ItemEntry<Item>
        TARTE_AU_RIZ = baked("tarte_au_riz", "Rice Tart", "Tarte au riz", BSFoods.TART, () -> RAW_TARTE_AU_RIZ),
        TARTE_AU_SUCRE = baked("tarte_au_sucre", "Sugar Tart", "Tarte au sucre", BSFoods.TART, () -> RAW_TARTE_AU_SUCRE),
        MATTENTAART = baked("mattentaart", "Mattentaart", "Mattentaart", BSFoods.TART, () -> RAW_MATTENTAART),
        RAW_CRAQUELIN = ingredient("raw_craquelin", "Raw Craquelin", "Craquelin cru"),
        RAW_CRAMIQUE = ingredient("raw_cramique", "Raw Cramique", "Cramique crue"),
        FLAT_BRIOCHE = ingredient("flat_brioche", "Flat Brioche", "Brioche aplatie"),
        RAW_PAIN_A_LA_GRECQUE = ingredient("raw_pain_a_la_grecque", "Raw Pain à la Grecque", "Pain à la grecque cru");
    public static final ItemEntry<Item>
        CRAQUELIN = baked("craquelin", "Craquelin", "Craquelin", BSFoods.PASTRY_LARGE, () -> RAW_CRAQUELIN),
        CRAMIQUE = baked("cramique", "Cramique", "Cramique", BSFoods.TART, () -> RAW_CRAMIQUE),
        PAIN_A_LA_GRECQUE = baked("pain_a_la_grecque", "Pain à la Grecque", "Pain à la grecque", BSFoods.PASTRY_LARGE, () -> RAW_PAIN_A_LA_GRECQUE);

    // Cold desserts and the hot chocolate.
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_MERVEILLEUX = REGISTRATE.item("incomplete_merveilleux", SequencedAssemblyItem::new).lang("Incomplete Merveilleux")
            .tag(BSTags.GRINDER_BLACKLIST).register();

    static {
        FRENCH.put(INCOMPLETE_MERVEILLEUX, "Merveilleux en cours");
    }

    public static final ItemEntry<Item>
        MERVEILLEUX = food("merveilleux", "Merveilleux", "Merveilleux", BSFoods.PASTRY_LARGE),
        DAME_BLANCHE = food("dame_blanche", "Dame Blanche", "Dame blanche", BSFoods.PASTRY_LARGE);
    public static final ItemEntry<DrinkItem>
        HOT_CHOCOLATE_BOTTLE = REGISTRATE.item("hot_chocolate_bottle", DrinkItem::new).lang("Bottle of Hot Chocolate")
            .properties(p -> p.food(BSFoods.inBottle(BSFoods.HOT_CHOCOLATE)).stacksTo(16))
            .tag(Tags.Items.FOODS)
            .register();

    static {
        FRENCH.put(HOT_CHOCOLATE_BOTTLE, "Bouteille de chocolat chaud");
    }

    private BSSweets() {
    }

    private static ItemEntry<Item> ingredient(String name, String english, String french) {
        ItemEntry<Item> entry = REGISTRATE.item(name, Item::new).lang(english).register();
        FRENCH.put(entry, french);
        return entry;
    }

    @SafeVarargs
    private static ItemEntry<Item> food(String name, String english, String french, FoodProperties food,
                                       net.minecraft.tags.TagKey<Item>... tags) {
        ItemEntry<Item> entry = REGISTRATE.item(name, Item::new).lang(english).properties(p -> p.food(food))
            .tag(Tags.Items.FOODS).tag(tags).register();
        FRENCH.put(entry, french);
        return entry;
    }

    // Baked by a fan on a campfire, or in a furnace.
    private static ItemEntry<Item> baked(String name, String english, String french, FoodProperties food, Supplier<ItemEntry<Item>> raw) {
        ItemEntry<Item> entry = REGISTRATE.item(name, Item::new).lang(english).properties(p -> p.food(food)).tag(Tags.Items.FOODS)
            .recipe((context, provider) -> BSSnacks.cook(provider, context.get(), Ingredient.of(raw.get().get()), name))
            .register();
        FRENCH.put(entry, french);
        return entry;
    }

    public static void register() {
        REGISTRATE.addRawLang(BSItems.tooltipKey("speculoos"), "Served with every coffee in Belgium.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("cuberdon"), "A purple cone. Crisp outside, runny inside.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("couque_de_dinant"), "Do not bite. Let it melt.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("ballotin"), "Eight pralines. Sharing is optional.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("dame_blanche"), "Vanilla, hot chocolate. Never a sundae.");
    }
}
