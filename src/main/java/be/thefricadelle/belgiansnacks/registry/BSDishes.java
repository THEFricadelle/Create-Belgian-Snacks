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
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

/**
 * v2 Belgian dishes (docs/13, section 5.11): bowls cooked in a heated mixer, which give the bowl back
 * when eaten, and oven dishes baked by a fan on a campfire. Finished dishes are food, so the Supreme
 * Grinder counts them (V-D1).
 */
public final class BSDishes {
    /** French name of every item of this class, read by the French language provider. */
    public static final Map<ItemEntry<? extends Item>, String> FRENCH = new LinkedHashMap<>();

    // Bowls from the mixer.
    public static final ItemEntry<Item>
        MOULES_MARINIERES = bowl("moules_marinieres", "Moules Marinières", "Moules marinières", BSFoods.DISH),
        MOULES_A_LA_BIERE = bowl("moules_a_la_biere", "Mussels in Beer", "Moules à la bière", BSFoods.DISH),
        MOULES_FRITES = bowl("moules_frites", "Moules-Frites", "Moules-frites", BSFoods.DISH_WITH_FRIES),
        CARBONNADE = bowl("carbonnade", "Carbonnade", "Carbonnade", BSFoods.DISH),
        CARBONNADE_FRITES = bowl("carbonnade_frites", "Carbonnade with Fries", "Carbonnade-frites", BSFoods.DISH_WITH_FRIES),
        STOEMP = bowl("stoemp", "Stoemp", "Stoemp", BSFoods.SIDE_DISH_LARGE),
        STOEMP_SAUCISSE = bowl("stoemp_saucisse", "Stoemp with Sausage", "Stoemp-saucisse", BSFoods.DISH_WITH_FRIES),
        WATERZOOI = bowl("waterzooi", "Waterzooi", "Waterzooi", BSFoods.DISH),
        LAPIN_A_LA_GUEUZE = bowl("lapin_a_la_gueuze", "Rabbit in Gueuze", "Lapin à la gueuze", BSFoods.DISH),
        BRUSSELS_SPROUTS_BACON = bowl("brussels_sprouts_bacon", "Brussels Sprouts with Bacon", "Choux de Bruxelles au lard", BSFoods.SIDE_DISH),
        ASPARAGUS_FLEMISH = bowl("asparagus_flemish", "Flemish Asparagus", "Asperges à la flamande", BSFoods.SIDE_DISH),
        HOP_SHOOTS_CREAM = bowl("hop_shoots_cream", "Hop Shoots in Cream", "Jets de houblon à la crème", BSFoods.SIDE_DISH),
        SALADE_LIEGEOISE = bowl("salade_liegeoise", "Liège Salad", "Salade liégeoise", BSFoods.SIDE_DISH),
        HOCHEPOT = bowl("hochepot", "Hochepot", "Hochepot", BSFoods.DISH),
        ESCAVECHE = bowl("escaveche", "Escavèche", "Escavèche", BSFoods.SIDE_DISH);

    // Steak-frites: a plate, no bowl.
    public static final ItemEntry<Item>
        STEAK_FRITES = dish("steak_frites", "Steak-Frites", "Steak-frites", BSFoods.DISH_WITH_FRIES);

    // Chicons au gratin: chicon, ham, béchamel and cheese, browned in the oven.
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_CHICONS_AU_GRATIN = transitional("incomplete_chicons_au_gratin", "Incomplete Chicons au Gratin", "Chicons au gratin en cours");
    public static final ItemEntry<Item>
        RAW_CHICONS_AU_GRATIN = ingredient("raw_chicons_au_gratin", "Unbaked Chicons au Gratin", "Chicons au gratin à gratiner");
    public static final ItemEntry<Item>
        CHICONS_AU_GRATIN = baked("chicons_au_gratin", "Chicons au Gratin", "Chicons au gratin", BSFoods.DISH, () -> RAW_CHICONS_AU_GRATIN);

    // Vol-au-vent: a puff pastry shell filled with chicken, mushrooms and béchamel.
    public static final ItemEntry<Item>
        RAW_VOL_AU_VENT_SHELL = ingredient("raw_vol_au_vent_shell", "Raw Vol-au-Vent Shell", "Bouchée à la reine crue");
    public static final ItemEntry<Item>
        VOL_AU_VENT_SHELL = REGISTRATE.item("vol_au_vent_shell", Item::new).lang("Vol-au-Vent Shell")
            .recipe((context, provider) -> BSSnacks.cook(provider, context.get(), Ingredient.of(BSDishes.RAW_VOL_AU_VENT_SHELL.get()), "vol_au_vent_shell"))
            .register();

    static {
        FRENCH.put(VOL_AU_VENT_SHELL, "Bouchée à la reine");
    }

    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_VOL_AU_VENT = transitional("incomplete_vol_au_vent", "Incomplete Vol-au-Vent", "Vol-au-vent en cours");
    public static final ItemEntry<Item>
        VOL_AU_VENT = dish("vol_au_vent", "Vol-au-Vent", "Vol-au-vent", BSFoods.DISH);

    // Boudin, poached, with apple compote.
    public static final ItemEntry<Item>
        RAW_BOUDIN = ingredient("raw_boudin", "Raw Boudin", "Boudin cru");
    public static final ItemEntry<Item>
        BOUDIN = baked("boudin", "Boudin", "Boudin", BSFoods.SIDE_DISH, () -> RAW_BOUDIN),
        BOUDIN_COMPOTE = bowl("boudin_compote", "Boudin with Apple Compote", "Boudin-compote", BSFoods.SIDE_DISH_LARGE);

    // Tarts share one transitional item (the sugar tart and the mattentaart come in M19).
    public static final ItemEntry<Item>
        TART_SHELL = ingredient("tart_shell", "Tart Shell", "Fond de tarte");
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_TART = transitional("incomplete_tart", "Incomplete Tart", "Tarte en cours");
    public static final ItemEntry<Item>
        RAW_FLAMICHE = ingredient("raw_flamiche", "Unbaked Flamiche", "Flamiche crue");
    public static final ItemEntry<Item>
        FLAMICHE = baked("flamiche", "Flamiche", "Flamiche", BSFoods.SIDE_DISH_LARGE, () -> RAW_FLAMICHE);

    // Croque-monsieur: bread, ham, cheese, pressed, toasted.
    public static final ItemEntry<SequencedAssemblyItem>
        INCOMPLETE_CROQUE_MONSIEUR = transitional("incomplete_croque_monsieur", "Incomplete Croque-Monsieur", "Croque-monsieur en cours");
    public static final ItemEntry<Item>
        RAW_CROQUE_MONSIEUR = ingredient("raw_croque_monsieur", "Untoasted Croque-Monsieur", "Croque-monsieur à toaster");
    public static final ItemEntry<Item>
        CROQUE_MONSIEUR = baked("croque_monsieur", "Croque-Monsieur", "Croque-monsieur", BSFoods.SIDE_DISH_LARGE, () -> RAW_CROQUE_MONSIEUR);

    private BSDishes() {
    }

    private static ItemEntry<Item> ingredient(String name, String english, String french) {
        return french(REGISTRATE.item(name, Item::new).lang(english).register(), french);
    }

    private static ItemEntry<SequencedAssemblyItem> transitional(String name, String english, String french) {
        return french(REGISTRATE.item(name, SequencedAssemblyItem::new).lang(english).tag(BSTags.GRINDER_BLACKLIST).register(), french);
    }

    private static ItemBuilder<Item, ?> food(String name, String english, FoodProperties food) {
        return REGISTRATE.item(name, Item::new).lang(english).properties(p -> p.food(food)).tag(Tags.Items.FOODS);
    }

    private static ItemEntry<Item> dish(String name, String english, String french, FoodProperties food) {
        return french(food(name, english, food).register(), french);
    }

    // Eaten from a bowl, which comes back; stacks like the apple compote.
    private static ItemEntry<Item> bowl(String name, String english, String french, FoodProperties food) {
        return french(food(name, english, BSFoods.inBowl(food)).properties(p -> p.stacksTo(16)).register(), french);
    }

    // Baked by a fan on a campfire, or in a furnace.
    private static ItemEntry<Item> baked(String name, String english, String french, FoodProperties food, Supplier<ItemEntry<Item>> raw) {
        return french(food(name, english, food)
            .recipe((context, provider) -> BSSnacks.cook(provider, context.get(), Ingredient.of(raw.get().get()), name))
            .register(), french);
    }

    private static <T extends Item> ItemEntry<T> french(ItemEntry<T> entry, String french) {
        FRENCH.put(entry, french);
        return entry;
    }

    public static void register() {
        REGISTRATE.addRawLang(BSItems.tooltipKey("moules_frites"), "The national dish. Mussels, fries, a bowl of shells.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("carbonnade"), "Beef stewed in brown beer, with a slice of bread to thicken it.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("stoemp"), "Mashed potatoes with whatever the garden has.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("waterzooi"), "From Ghent. Soup or stew, the Ghentians argue.");
        REGISTRATE.addRawLang(BSItems.tooltipKey("chicons_au_gratin"), "Bitter, creamy, cheesy. Children run away.");
    }
}
