/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import be.thefricadelle.belgiansnacks.registry.BSDataComponents;
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import be.thefricadelle.belgiansnacks.registry.BSTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Collects every unique food once. The collection is shared by both modes; reaching the current
 * mode's goal turns the whole collection into one paste (D9). Only the server counts; clients get
 * the figures and a few missing examples, never the collection itself.
 */
public class SupremeGrinderBlockEntity extends KineticBlockEntity {
    private static final int MISSING_SAMPLES = 5;
    private static final int HALFWAY_RANGE = 16;

    // Set from addBehaviours, which the SmartBlockEntity constructor calls before field initialisers:
    // no initialiser here, or it would be reset to null.
    private ScrollOptionBehaviour<GrinderMode> mode;

    // Ids stay even when their item is gone: a mod removed then added back gets its progress back.
    private final Set<ResourceLocation> consumed = new LinkedHashSet<>();
    private final ItemStackHandler output = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                sendData();
            }
        }
    };
    private final IItemHandler itemCapability = new GrinderItemHandler();

    // Server: the index the count was taken against, recounted when it changes.
    @Nullable
    private FoodIndex.Snapshot countedFor;
    private boolean recount = true;

    // What clients see, kept on both sides so a change is only sent when it matters.
    private int count;
    private int goal;
    private int total;
    private List<ResourceLocation> samples = List.of();

    public SupremeGrinderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BSBlockEntities.SUPREME_GRINDER.get(), (be, side) -> be.itemCapability);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        mode = new ScrollOptionBehaviour<>(GrinderMode.class, Component.translatable(BelgianSnacks.MOD_ID + ".grinder.mode"), this,
            new CenteredSideValueBoxTransform((state, direction) -> direction.getAxis().isHorizontal()));
        mode.withCallback(value -> recount = true);
        behaviours.add(mode);
        // A belt ending on the grinder feeds it through the same item handler as a funnel.
        behaviours.add(new DirectBeltInputBehaviour(this));
    }

    // ------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        FoodIndex.Snapshot index = FoodIndex.server();
        boolean indexChanged = index != countedFor;
        if (indexChanged || recount) {
            countedFor = index;
            recount = false;
            int newCount = GrinderProgress.count(consumed, index.lookup());
            int newGoal = GrinderProgress.goal(getMode().ratio(), index.size());
            if (newCount != count || newGoal != goal || index.size() != total || indexChanged) {
                boolean grew = newCount > count;
                count = newCount;
                goal = newGoal;
                total = index.size();
                samples = pickSamples(index);
                updateGauge();
                sendData();
                if (grew) {
                    rewardHalfway();
                }
            }
        } else {
            // The ratio is config: a reload changes the goal without touching the index.
            int newGoal = GrinderProgress.goal(getMode().ratio(), total);
            if (newGoal != goal) {
                goal = newGoal;
                updateGauge();
                sendData();
            }
        }
        if (GrinderProgress.reached(count, goal) && output.getStackInSlot(0).isEmpty()) {
            produce();
        }
    }

    // Advancement "Halfway There": half of every food of the index, whatever the mode is aiming at.
    private void rewardHalfway() {
        if (count < GrinderProgress.goal(0.5, total) || !(level instanceof ServerLevel server)) {
            return;
        }
        for (ServerPlayer player : server.players()) {
            if (player.blockPosition().closerThan(worldPosition, HALFWAY_RANGE)) {
                BSTriggers.GRINDER_HALF.get().trigger(player);
            }
        }
    }

    private void produce() {
        GrinderMode current = getMode();
        int excess = count - goal;
        output.setStackInSlot(0, current.paste());
        consumed.clear();
        recount = true;
        setChanged();
        level.playSound(null, worldPosition, BSSoundEvents.GRINDER_COMPLETE.get(), SoundSource.BLOCKS, 1f, 1f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1,
                worldPosition.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0);
            // D9: a collection beyond the THE_ goal is lost with the rest, a THE_FRICADELLE attempt that fell short.
            if (current == GrinderMode.THE_ && excess > 0) {
                Component message = Component.translatable(BelgianSnacks.MOD_ID + ".grinder.excess", excess).withStyle(ChatFormatting.GOLD);
                for (ServerPlayer player : server.players()) {
                    if (player.blockPosition().closerThan(worldPosition, 16)) {
                        player.displayClientMessage(message, true);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ input

    /**
     * Takes at most one item of {@code stack} and returns the rest. Refused (whole stack back) when
     * the grinder turns too slowly, the paste is waiting, or the item is not a new food.
     */
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (level == null || level.isClientSide || stack.isEmpty() || !isFastEnough() || !output.getStackInSlot(0).isEmpty()) {
            return stack;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        GrinderProgress.Decision decision = GrinderProgress.decide(id, FoodIndex.server().contains(id), consumed,
            BSConfig.grinderRejectDuplicates());
        if (!decision.takesItem()) {
            return stack;
        }
        if (!simulate) {
            if (decision == GrinderProgress.Decision.NEW) {
                consumed.add(id);
                recount = true;
                setChanged();
            }
            grindEffects(stack);
        }
        return stack.copyWithCount(stack.getCount() - 1);
    }

    public boolean isFastEnough() {
        return Math.abs(getSpeed()) >= BSConfig.grinderMinSpeed();
    }

    private void grindEffects(ItemStack stack) {
        level.playSound(null, worldPosition, BSSoundEvents.GRINDER_GRIND.get(), SoundSource.BLOCKS, 0.5f, 0.9f + level.random.nextFloat() * 0.2f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.copyWithCount(1)), worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.05);
        }
    }

    // ------------------------------------------------------------------ progress

    private List<ResourceLocation> pickSamples(FoodIndex.Snapshot index) {
        List<ResourceLocation> missing = missing(index);
        if (missing.size() <= MISSING_SAMPLES) {
            return missing;
        }
        Collections.shuffle(missing, new java.util.Random(level.random.nextLong()));
        return List.copyOf(missing.subList(0, MISSING_SAMPLES));
    }

    /** Foods of the index not collected yet, in index order. Server only. */
    public List<ResourceLocation> missing(FoodIndex.Snapshot index) {
        List<ResourceLocation> missing = new ArrayList<>(Math.max(0, index.size() - consumed.size()));
        for (ResourceLocation id : index.ids()) {
            if (!consumed.contains(id)) {
                missing.add(id);
            }
        }
        return missing;
    }

    /** Replaces the collection (the fill command, tests). The next tick counts it and may produce. */
    public void setConsumed(Iterable<ResourceLocation> ids) {
        consumed.clear();
        ids.forEach(consumed::add);
        recount = true;
        setChanged();
    }

    public Set<ResourceLocation> getConsumed() {
        return Collections.unmodifiableSet(consumed);
    }

    public GrinderMode getMode() {
        return mode.get();
    }

    public void setMode(GrinderMode value) {
        mode.setValue(value.ordinal());
        recount = true;
    }

    public int getCount() {
        return count;
    }

    public int getGoal() {
        return goal;
    }

    public int getTotal() {
        return total;
    }

    public List<ResourceLocation> getSamples() {
        return samples;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    public IItemHandler getItemCapability() {
        return itemCapability;
    }

    public int comparatorSignal() {
        return goal == 0 ? 0 : Math.round(15f * Math.min(count, goal) / goal);
    }

    private void updateGauge() {
        int fill = goal == 0 ? 0 : Math.min(4, count * 4 / goal);
        BlockState state = getBlockState();
        if (state.hasProperty(SupremeGrinderBlock.FILL) && state.getValue(SupremeGrinderBlock.FILL) != fill) {
            level.setBlock(worldPosition, state.setValue(SupremeGrinderBlock.FILL, fill), 3);
        }
    }

    // ------------------------------------------------------------------ lifecycle and sync

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, output);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Output", output.serializeNBT(registries));
        if (clientPacket) {
            tag.putInt("Count", count);
            tag.putInt("Goal", goal);
            tag.putInt("Total", total);
            tag.put("Samples", idList(samples));
        } else {
            tag.put("Consumed", idList(consumed));
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        output.deserializeNBT(registries, tag.getCompound("Output"));
        if (clientPacket) {
            count = tag.getInt("Count");
            goal = tag.getInt("Goal");
            total = tag.getInt("Total");
            samples = readIds(tag.getList("Samples", Tag.TAG_STRING));
        } else {
            consumed.clear();
            consumed.addAll(readIds(tag.getList("Consumed", Tag.TAG_STRING)));
            recount = true;
        }
    }

    private static ListTag idList(Iterable<ResourceLocation> ids) {
        ListTag list = new ListTag();
        ids.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        return list;
    }

    private static List<ResourceLocation> readIds(ListTag list) {
        List<ResourceLocation> ids = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    // The collection travels with the item when the grinder is broken (D10, loot table copies this component).
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!consumed.isEmpty() || getMode() != GrinderMode.THE_) {
            components.set(BSDataComponents.GRINDER_CONTENTS.get(), new GrinderContents(List.copyOf(consumed), getMode().id()));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        GrinderContents contents = components.get(BSDataComponents.GRINDER_CONTENTS.get());
        if (contents != null) {
            setConsumed(contents.consumed());
            setMode(GrinderMode.byId(contents.mode()));
        }
    }

    // ------------------------------------------------------------------ goggles

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        String key = BelgianSnacks.MOD_ID + ".grinder.";
        tooltip.add(Component.literal("    ").append(Component.translatable("block." + BelgianSnacks.MOD_ID + ".supreme_grinder")));
        tooltip.add(line(Component.translatable(key + "goggles.mode", Component.translatable(getMode().getTranslationKey())), ChatFormatting.GRAY));
        tooltip.add(line(Component.translatable(key + "goggles.progress", count, goal, percent()), ChatFormatting.GRAY));
        if (!isFastEnough()) {
            tooltip.add(line(Component.translatable(key + "goggles.too_slow", BSConfig.grinderMinSpeed()), ChatFormatting.GOLD));
        }
        if (!output.getStackInSlot(0).isEmpty()) {
            tooltip.add(line(Component.translatable(key + "goggles.output_full"), ChatFormatting.GOLD));
        }
        if (!samples.isEmpty()) {
            tooltip.add(line(Component.translatable(key + "goggles.missing"), ChatFormatting.GRAY));
            for (ResourceLocation id : samples) {
                tooltip.add(Component.literal("      ").append(BuiltInRegistries.ITEM.get(id).getDescription().copy().withStyle(ChatFormatting.DARK_GRAY)));
            }
            tooltip.add(line(Component.translatable(key + "goggles.full_list"), ChatFormatting.DARK_GRAY));
        }
        return true;
    }

    public int percent() {
        return goal == 0 ? 0 : Math.min(100, count * 100 / goal);
    }

    private static Component line(Component text, ChatFormatting style) {
        return Component.literal("     ").append(text.copy().withStyle(style));
    }

    // ------------------------------------------------------------------ item handler

    /** Slot 0 takes foods one at a time (always reads empty), slot 1 is the paste (extract only). */
    private final class GrinderItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 1 ? output.getStackInSlot(0) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0 ? insert(stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? output.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : output.getSlotLimit(0);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0;
        }
    }
}
