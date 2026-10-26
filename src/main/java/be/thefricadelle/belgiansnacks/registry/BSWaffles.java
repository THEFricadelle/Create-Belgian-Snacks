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

import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

/**
 * v2 waffles (docs/13, section 5.6): batters mixed in a basin, baked in the waffle iron, then topped by a
 * deployer or a spout. Finished waffles are food, so the Supreme Grinder counts them (V-D1).
 */
public final class BSWaffles {
    /** French name of every item of this class, read by the French language provider. */
    public static final Map<ItemEntry<? extends Item>, String> FRENCH = new LinkedHashMap<>();

    public static final ItemEntry<Item>
        LIEGE_WAFFLE_DOUGH = ingredient("liege_waffle_dough", "Liège Waffle Dough", "Pâte à gaufres de Liège"),
        BRUSSELS_WAFFLE_BATTER = ingredient("brussels_waffle_batter", "Brussels Waffle Batter", "Pâte à gaufres de Bruxelles"),
        LACQUEMANT_BATTER = ingredient("lacquemant_batter", "Lacquemant Batter", "Pâte à lacquemant"),
        LACQUEMANT_WAFER = ingredient("lacquemant_wafer", "Lacquemant Wafer", "Gaufrette de lacquemant"),
        LIEGE_WAFFLE = food("liege_waffle", "Liège Waffle", "Gaufre de Liège", BSFoods.LIEGE_WAFFLE),
        BRUSSELS_WAFFLE = food("brussels_waffle", "Brussels Waffle", "Gaufre de Bruxelles", BSFoods.BRUSSELS_WAFFLE),
        BRUSSELS_WAFFLE_SUGARED = food("brussels_waffle_sugared", "Brussels Waffle with Powdered Sugar", "Gaufre de Bruxelles au sucre glace",
            BSFoods.BRUSSELS_WAFFLE_SUGARED),
        BRUSSELS_WAFFLE_CHOCOLATE = food("brussels_waffle_chocolate", "Brussels Waffle with Chocolate", "Gaufre de Bruxelles au chocolat",
            BSFoods.BRUSSELS_WAFFLE_CHOCOLATE),
        LACQUEMANT = food("lacquemant", "Lacquemant", "Lacquemant", BSFoods.LACQUEMANT);

    private BSWaffles() {
    }

    private static ItemEntry<Item> ingredient(String name, String english, String french) {
        ItemEntry<Item> entry = REGISTRATE.item(name, Item::new).lang(english).register();
        FRENCH.put(entry, french);
        return entry;
    }

    private static ItemEntry<Item> food(String name, String english, String french, FoodProperties food) {
        ItemEntry<Item> entry = REGISTRATE.item(name, Item::new).lang(english).properties(p -> p.food(food)).tag(Tags.Items.FOODS).register();
        FRENCH.put(entry, french);
        return entry;
    }

    public static void register() {
        REGISTRATE.addRawLang(BSItems.tooltipKey("liege_waffle"), "Pearl sugar caramelised in the grid. Eaten while walking.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("brussels_waffle"), "Light, crisp, rectangular. A tourist favourite.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("lacquemant"), "A thin wafer full of Liège syrup. From the Liège fair.");
    }
}
