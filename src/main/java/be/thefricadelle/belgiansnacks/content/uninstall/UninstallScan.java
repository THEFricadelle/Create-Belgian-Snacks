/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.uninstall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * What of ours a removal would leave behind (spec v2, rule R4): items and fluids in online players
 * and in the block entities of loaded chunks, our own blocks there, and entries in other mods' player
 * data. Read only, once per command: nothing here runs in a tick.
 */
public final class UninstallScan {
    private UninstallScan() {
    }

    public record Report(int items, int fluidMillibuckets, int blocks, int references, List<String> lines) {
    }

    public static Report run(MinecraftServer server) {
        Counter counter = new Counter();
        List<ServerPlayer> players = new ArrayList<>();
        server.getAllLevels().forEach(level -> players.addAll(level.players()));
        for (ServerPlayer player : players) {
            String who = "player " + player.getGameProfile().getName();
            counter.items(who + " inventory", player.getInventory());
            counter.items(who + " ender chest", player.getEnderChestInventory());
            for (ReferencePurger purger : Uninstall.purgers()) {
                int found = purger.count(player);
                if (found > 0) {
                    counter.references += found;
                    counter.lines.add(who + " " + purger.name() + " data: " + found + " entries");
                }
            }
        }
        int viewDistance = server.getPlayerList().getViewDistance();
        for (ServerLevel level : server.getAllLevels()) {
            for (LevelChunk chunk : loadedChunks(level, viewDistance)) {
                for (BlockEntity blockEntity : List.copyOf(chunk.getBlockEntities().values())) {
                    counter.blockEntity(level, blockEntity);
                }
            }
        }
        return new Report(counter.items, counter.fluids, counter.blocks, counter.references,
            Collections.unmodifiableList(counter.lines));
    }

    // Chunks around the online players and forced chunks: the ones a server keeps loaded.
    private static List<LevelChunk> loadedChunks(ServerLevel level, int viewDistance) {
        LongSet positions = new LongOpenHashSet(level.getForcedChunks());
        for (ServerPlayer player : level.players()) {
            ChunkPos centre = player.chunkPosition();
            for (int dx = -viewDistance; dx <= viewDistance; dx++) {
                for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                    positions.add(ChunkPos.asLong(centre.x + dx, centre.z + dz));
                }
            }
        }
        List<LevelChunk> chunks = new ArrayList<>();
        for (long position : positions) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(position), ChunkPos.getZ(position));
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    private static boolean ours(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(BelgianSnacks.MOD_ID);
    }

    private static final class Counter {
        int items;
        int fluids;
        int blocks;
        int references;
        final List<String> lines = new ArrayList<>();
        // Multiblocks (Create tanks, vaults) share one handler between their blocks: counted once.
        final Set<Object> seenHandlers = Collections.newSetFromMap(new IdentityHashMap<>());

        void items(String where, Container container) {
            int found = 0;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                found += countStack(container.getItem(slot));
            }
            add(where, found);
        }

        void blockEntity(ServerLevel level, BlockEntity blockEntity) {
            BlockPos pos = blockEntity.getBlockPos();
            String where = level.dimension().location() + " " + pos.toShortString();
            var type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
            if (type != null && type.getNamespace().equals(BelgianSnacks.MOD_ID)) {
                blocks++;
                lines.add(where + ": " + type);
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null && seenHandlers.add(handler)) {
                int found = 0;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    found += countStack(handler.getStackInSlot(slot));
                }
                add(where, found);
            }
            IFluidHandler tanks = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
            if (tanks != null && seenHandlers.add(tanks)) {
                int amount = 0;
                for (int tank = 0; tank < tanks.getTanks(); tank++) {
                    FluidStack fluid = tanks.getFluidInTank(tank);
                    if (!fluid.isEmpty()
                        && BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getNamespace().equals(BelgianSnacks.MOD_ID)) {
                        amount += fluid.getAmount();
                    }
                }
                if (amount > 0) {
                    fluids += amount;
                    lines.add(where + ": " + amount + " mB of our fluids");
                }
            }
        }

        // The stack itself, and one level of what it holds (shulker boxes, bundles).
        int countStack(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            int found = ours(stack) ? stack.getCount() : 0;
            ItemContainerContents container = stack.get(DataComponents.CONTAINER);
            if (container != null) {
                for (ItemStack inner : container.nonEmptyItems()) {
                    found += ours(inner) ? inner.getCount() : 0;
                }
            }
            BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundle != null) {
                for (ItemStack inner : bundle.items()) {
                    found += ours(inner) ? inner.getCount() : 0;
                }
            }
            return found;
        }

        void add(String where, int found) {
            if (found > 0) {
                items += found;
                lines.add(where + ": " + found + " of our items");
            }
        }
    }
}
