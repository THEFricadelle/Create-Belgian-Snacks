/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

/**
 * Where the v2 seeds and mussels come from (docs/13, section 5.8): wild grass and fishing. NeoForge's
 * own add_table modifier adds our small tables to vanilla ones; nothing of ours is registered for it.
 */
public final class BSLootProviders {
    public static final ResourceKey<LootTable> WILD_SEEDS = key("gameplay/wild_seeds");
    public static final ResourceKey<LootTable> FISHING_MUSSELS = key("gameplay/fishing_mussels");

    private BSLootProviders() {
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, BelgianSnacks.asResource(path));
    }

    /** The two tables: one roll of grass in about seven gives one of the six seeds; one catch in twenty a mussel. */
    public static LootTableProvider tables(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(Tables::new, LootContextParamSets.ALL_PARAMS)),
            registries);
    }

    private record Tables(HolderLookup.Provider registries) implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            LootPool.Builder seeds = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.15f));
            for (var seed : List.of(BSCrops.CHICORY_ROOT, BSCrops.LEEK, BSCrops.HOP_SEEDS, BSCrops.BRUSSELS_SPROUT,
                BSCrops.ASPARAGUS_CROWN, BSCrops.BUCKWHEAT, BSCrops.MUSTARD_SEEDS)) {
                seeds.add(LootItem.lootTableItem(seed.get()));
            }
            output.accept(WILD_SEEDS, LootTable.lootTable().withPool(seeds));
            output.accept(FISHING_MUSSELS, LootTable.lootTable().withPool(LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance(0.05f))
                .add(LootItem.lootTableItem(BSCrops.MUSSEL.get()))));
        }
    }

    public static GlobalLootModifierProvider modifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new GlobalLootModifierProvider(output, registries, BelgianSnacks.MOD_ID) {
            @Override
            protected void start() {
                add("wild_seeds_from_short_grass", addTo("blocks/short_grass", WILD_SEEDS));
                add("wild_seeds_from_tall_grass", addTo("blocks/tall_grass", WILD_SEEDS));
                add("mussels_from_fishing", addTo("gameplay/fishing/fish", FISHING_MUSSELS));
            }
        };
    }

    private static AddTableLootModifier addTo(String vanillaTable, ResourceKey<LootTable> table) {
        return new AddTableLootModifier(new LootItemCondition[] {
            LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace(vanillaTable)).build()}, table);
    }
}
