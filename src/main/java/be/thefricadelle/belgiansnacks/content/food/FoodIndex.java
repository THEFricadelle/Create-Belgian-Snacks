/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.network.FoodIndexSyncPayload;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Every unique food of the running modpack, the list the Supreme Grinder collects.
 * <p>
 * Computed on the server only (start, tag reload, config reload) and sent to clients, which never
 * compute it themselves: their tags or config could differ. One snapshot per side, since an
 * integrated server shares the JVM with its client. Never computed during a tick.
 */
public final class FoodIndex {
    public record Snapshot(List<ResourceLocation> ids, Set<ResourceLocation> lookup, Map<ResourceLocation, FoodIndexRules.Source> sources,
                           int excluded, long computeNanos) {
        static Snapshot of(List<ResourceLocation> ids, Map<ResourceLocation, FoodIndexRules.Source> sources, int excluded, long nanos) {
            return new Snapshot(List.copyOf(ids), Set.copyOf(ids), Map.copyOf(sources), excluded, nanos);
        }

        public boolean contains(ResourceLocation id) {
            return lookup.contains(id);
        }

        public int size() {
            return ids.size();
        }

        /** Order-sensitive fingerprint, to compare what two sides hold without sending the list. */
        public int fingerprint() {
            return ids.hashCode();
        }
    }

    public static final Snapshot EMPTY = Snapshot.of(List.of(), Map.of(), 0, 0);

    private static volatile Snapshot server = EMPTY;
    private static volatile Snapshot client = EMPTY;

    private FoodIndex() {
    }

    public static Snapshot get(Level level) {
        return level.isClientSide ? client : server;
    }

    public static Snapshot server() {
        return server;
    }

    public static Snapshot client() {
        return client;
    }

    /** Recomputes the server list from the config and broadcasts it when a server is running. */
    public static void recompute(@Nullable MinecraftServer running) {
        server = compute(BSConfig.grinderBlacklistedMods(), BSConfig.grinderBlacklistedItems());
        BelgianSnacks.LOGGER.info("Food index: {} foods ({} excluded) in {} ms", server.size(), server.excluded(),
            String.format(java.util.Locale.ROOT, "%.1f", server.computeNanos() / 1_000_000.0));
        if (running != null) {
            PacketDistributor.sendToAllPlayers(new FoodIndexSyncPayload(server.ids()));
        }
    }

    /** The list for the given config blacklists, from the current registries and tags. */
    public static Snapshot compute(Set<String> blacklistedMods, Set<String> blacklistedItems) {
        long start = System.nanoTime();
        List<FoodIndexRules.Candidate> candidates = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                candidates.add(new FoodIndexRules.Candidate(BuiltInRegistries.ITEM.getKey(item).toString(), item.components().has(DataComponents.FOOD)));
            }
        }
        FoodIndexRules.Result result = FoodIndexRules.compute(candidates, members(BSTags.GRINDER_EXTRA_FOODS),
            members(BSTags.GRINDER_BLACKLIST), blacklistedMods, blacklistedItems);
        List<ResourceLocation> ids = new ArrayList<>(result.foods().size());
        Map<ResourceLocation, FoodIndexRules.Source> sources = new LinkedHashMap<>();
        for (FoodIndexRules.Entry entry : result.foods()) {
            ResourceLocation id = ResourceLocation.parse(entry.id());
            ids.add(id);
            sources.put(id, entry.source());
        }
        return Snapshot.of(ids, sources, result.excluded(), System.nanoTime() - start);
    }

    /** Called on the client with the server's list. */
    public static void acceptFromServer(List<ResourceLocation> ids) {
        client = Snapshot.of(ids, Map.of(), 0, 0);
    }

    public static void clearClient() {
        client = EMPTY;
    }

    private static Set<String> members(TagKey<Item> tag) {
        Set<String> ids = new HashSet<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> ids.add(holder.unwrapKey().orElseThrow().location().toString()));
        return ids;
    }
}
