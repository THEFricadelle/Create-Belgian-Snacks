/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import java.util.function.Consumer;

import com.tterrag.registrate.providers.ProviderType;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * The mod's advancement tab (decided on 27/09/2026): from the first mince to THE_FRICADELLE.
 * Titles and descriptions are translation keys, filled in both languages by BSLang.
 */
public final class BSAdvancements {
    public static final String KEY = "advancement." + BelgianSnacks.MOD_ID + ".";
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/block/yellow_terracotta.png");

    private BSAdvancements() {
    }

    public static void register() {
        BelgianSnacks.REGISTRATE.addDataGenerator(ProviderType.ADVANCEMENT, BSAdvancements::generate);
    }

    private static void generate(Consumer<AdvancementHolder> out) {
        AdvancementHolder root = Advancement.Builder.advancement()
            .display(BSItems.FRICADELLE.get(), title("root"), description("root"), BACKGROUND, AdvancementType.TASK, false, false, false)
            .addCriterion("minced", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BSTags.MINCED_MEATS)))
            .build(id("root"));
        out.accept(root);

        AdvancementHolder fryer = node(out, root, "fryer", BSBlocks.FRYER.get(), AdvancementType.TASK,
            ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(BSBlocks.FRYER.get()));
        node(out, fryer, "beef_tallow", BSFluids.MELTED_BEEF_TALLOW.getBucket().orElseThrow(), AdvancementType.TASK,
            InventoryChangeTrigger.TriggerInstance.hasItems(BSFluids.MELTED_BEEF_TALLOW.getBucket().orElseThrow()));
        AdvancementHolder fricadelle = node(out, fryer, "fricadelle", BSItems.FRICADELLE.get(), AdvancementType.TASK,
            ConsumeItemTrigger.TriggerInstance.usedItem(BSItems.FRICADELLE.get()));

        AdvancementHolder grinder = node(out, fricadelle, "supreme_grinder", BSBlocks.SUPREME_GRINDER.get(), AdvancementType.TASK,
            ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(BSBlocks.SUPREME_GRINDER.get()));
        AdvancementHolder exceptional = node(out, grinder, "exceptional_paste", BSItems.EXCEPTIONAL_PASTE.get(), AdvancementType.TASK,
            InventoryChangeTrigger.TriggerInstance.hasItems(BSItems.EXCEPTIONAL_PASTE.get()));
        node(out, exceptional, "the_fricadelle", BSItems.THE_FRICADELLE.get(), AdvancementType.GOAL,
            ConsumeItemTrigger.TriggerInstance.usedItem(BSItems.THE_FRICADELLE.get()));

        // v2 (M21): the Belgian menu, children of the root.
        node(out, root, "chicon_cellar", BSCrops.CHICON.get(), AdvancementType.TASK,
            InventoryChangeTrigger.TriggerInstance.hasItems(BSCrops.CHICON.get()));
        node(out, root, "brewer", BSFluids.KRIEK.getBucket().orElseThrow(), AdvancementType.TASK,
            InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
                BSFluids.BLONDE_BEER.getBucket().orElseThrow(), BSFluids.BROWN_BEER.getBucket().orElseThrow(),
                BSFluids.GUEUZE.getBucket().orElseThrow(), BSFluids.KRIEK.getBucket().orElseThrow())));
        node(out, root, "ballotin", BSSweets.BALLOTIN.get(), AdvancementType.TASK,
            InventoryChangeTrigger.TriggerInstance.hasItems(BSSweets.BALLOTIN.get()));
        node(out, root, "waffle_iron", BSBlocks.WAFFLE_IRON.get(), AdvancementType.TASK,
            ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(BSBlocks.WAFFLE_IRON.get()));
        // One food of each family: five criteria, all needed.
        AdvancementHolder tour = Advancement.Builder.advancement()
            .parent(root)
            .display(BSSnacks.MITRAILLETTE.get(), title("tour_de_belgique"), description("tour_de_belgique"), null, AdvancementType.GOAL,
                true, true, false)
            .addCriterion("fries", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(BSTags.MENU_FRIES)))
            .addCriterion("friterie", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(BSTags.MENU_FRITERIE)))
            .addCriterion("dishes", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(BSTags.MENU_DISHES)))
            .addCriterion("waffles", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(BSTags.MENU_WAFFLES)))
            .addCriterion("sweets", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(BSTags.MENU_SWEETS)))
            .build(id("tour_de_belgique"));
        out.accept(tour);

        AdvancementHolder halfway = node(out, grinder, "grinder_half", BSBlocks.SUPREME_GRINDER.get(), AdvancementType.GOAL,
            BSTriggers.GRINDER_HALF.get().criterion());
        AdvancementHolder absolute = node(out, halfway, "absolute_paste", BSItems.ABSOLUTE_PASTE.get(), AdvancementType.CHALLENGE,
            InventoryChangeTrigger.TriggerInstance.hasItems(BSItems.ABSOLUTE_PASTE.get()));
        node(out, absolute, "ultimate_fricadelle", BSItems.ULTIMATE_FRICADELLE.get(), AdvancementType.CHALLENGE,
            ConsumeItemTrigger.TriggerInstance.usedItem(BSItems.ULTIMATE_FRICADELLE.get()));
    }

    private static AdvancementHolder node(Consumer<AdvancementHolder> out, AdvancementHolder parent, String name, ItemLike icon,
                                          AdvancementType type, Criterion<?> criterion) {
        // Toast and chat announcement for all; challenges hidden until their parent is done.
        AdvancementHolder holder = Advancement.Builder.advancement()
            .parent(parent)
            .display(icon, title(name), description(name), null, type, true, true, type == AdvancementType.CHALLENGE)
            .addCriterion(name, criterion)
            .build(id(name));
        out.accept(holder);
        return holder;
    }

    public static ResourceLocation id(String name) {
        return BelgianSnacks.asResource(name);
    }

    private static Component title(String name) {
        return Component.translatable(KEY + name);
    }

    private static Component description(String name) {
        return Component.translatable(KEY + name + ".desc");
    }
}
