/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.waffle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.processing.OutputSpace;
import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Bakes batters between two hot plates, a few at a time (waffleIron.maxBatch). The batch moves onto
 * the plates when it starts and the lid closes over it; without enough heat it pauses and keeps its
 * progress. Heat follows the fryer's rules, Create Heat JS included (FryerBlockEntity.heatAllows).
 */
public class WaffleIronBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private static final int SIZZLE_INTERVAL = 50;
    private static final int IDLE_RETRY_TICKS = 10;
    private static final int DEFAULT_DURATION = 160;
    // Ticks for the lid to swing fully open or shut.
    private static final float LID_TICKS = 6;

    public enum Status {
        IDLE, BAKING, NO_RECIPE, NO_HEAT, OUTPUT_FULL;

        public String key() {
            return BelgianSnacks.MOD_ID + ".waffle_iron.status." + name().toLowerCase(Locale.ROOT);
        }
    }

    private final ItemStackHandler input = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return BSConfig.waffleIronMaxBatch();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return findRecipe(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            onInventoryChanged();
        }
    };
    private final ItemStackHandler output = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            onInventoryChanged();
        }
    };
    // What lies between the plates right now.
    private final ItemStackHandler plates = new ItemStackHandler(1);
    private final IItemHandler itemCapability = new WaffleIronItemHandler();

    @Nullable
    private ResourceLocation recipeId;
    private int progress;
    private int duration;
    private boolean paused;
    private int retry;
    // Client only: how open the lid is, 0 shut to 1 open, this tick and the last.
    private float lidOpen = 1;
    private float lidOpenPrevious = 1;

    public WaffleIronBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BSBlockEntities.WAFFLE_IRON.get(), (be, side) -> be.itemCapability);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // A belt ending on the waffle iron drops its batters in, through the same handler as a funnel.
        behaviours.add(new DirectBeltInputBehaviour(this));
    }

    // ------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            clientTick();
            return;
        }
        if (!plates.getStackInSlot(0).isEmpty()) {
            bake();
            return;
        }
        if (retry-- > 0) {
            return;
        }
        retry = IDLE_RETRY_TICKS;
        tryStartBatch();
    }

    private void tryStartBatch() {
        ItemStack in = input.getStackInSlot(0);
        if (in.isEmpty() || blocker(in) != null) {
            return;
        }
        RecipeHolder<WaffleBakingRecipe> holder = findRecipe(in);
        WaffleBakingRecipe recipe = holder.value();
        plates.setStackInSlot(0, input.extractItem(0, batchSize(in), false));
        recipeId = holder.id();
        int base = recipe.getProcessingDuration() > 0 ? recipe.getProcessingDuration() : DEFAULT_DURATION;
        duration = Math.max(1, (int) Math.round(base / BSConfig.waffleIronSpeedMultiplier()));
        progress = 0;
        paused = false;
        notifyUpdate();
    }

    private void bake() {
        WaffleBakingRecipe recipe = currentRecipe();
        if (recipe == null) {
            // The recipe vanished in a /reload: hand the batters back untouched.
            finishBatch(List.of(plates.getStackInSlot(0).copy()));
            return;
        }
        boolean hot = FryerBlockEntity.heatAllows(recipe.getRequiredHeat(), heatBelow());
        if (hot == paused) {
            paused = !hot;
            notifyUpdate();
        }
        if (!hot) {
            return;
        }
        if (progress % SIZZLE_INTERVAL == 0) {
            level.playSound(null, worldPosition, BSSoundEvents.WAFFLE_IRON_SIZZLE.get(), SoundSource.BLOCKS, 0.5f,
                1.0f + level.random.nextFloat() * 0.2f);
        }
        if (++progress < duration) {
            return;
        }
        int count = plates.getStackInSlot(0).getCount();
        List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.addAll(recipe.rollResults(level.random));
        }
        finishBatch(results);
    }

    private void finishBatch(List<ItemStack> results) {
        for (ItemStack result : results) {
            ItemStack left = ItemHandlerHelper.insertItemStacked(output, result, false);
            // Space was checked when the batch started; this only guards against external edits.
            if (!left.isEmpty()) {
                Block.popResource(level, worldPosition.above(), left);
            }
        }
        plates.setStackInSlot(0, ItemStack.EMPTY);
        recipeId = null;
        progress = 0;
        duration = 0;
        paused = false;
        retry = 0;
        notifyUpdate();
    }

    private void clientTick() {
        boolean shut = !plates.getStackInSlot(0).isEmpty();
        lidOpenPrevious = lidOpen;
        lidOpen = Math.max(0, Math.min(1, lidOpen + (shut ? -1 : 1) / LID_TICKS));
        if (!shut || paused) {
            return;
        }
        if (progress < duration) {
            progress++;
        }
        // Steam escaping between the plates.
        var random = level.random;
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.WHITE_SMOKE, worldPosition.getX() + 0.15 + random.nextDouble() * 0.7, worldPosition.getY() + 0.6,
                worldPosition.getZ() + 0.15 + random.nextDouble() * 0.7, 0, 0.02, 0);
        }
    }

    // ------------------------------------------------------------------ rules

    @Nullable
    private Status blocker(ItemStack in) {
        RecipeHolder<WaffleBakingRecipe> holder = findRecipe(in);
        if (holder == null) {
            return Status.NO_RECIPE;
        }
        if (!FryerBlockEntity.heatAllows(holder.value().getRequiredHeat(), heatBelow())) {
            return Status.NO_HEAT;
        }
        if (!OutputSpace.fits(output, holder.value().getRollableResults(), batchSize(in))) {
            return Status.OUTPUT_FULL;
        }
        return null;
    }

    public Status status() {
        if (!plates.getStackInSlot(0).isEmpty()) {
            return paused ? Status.NO_HEAT : Status.BAKING;
        }
        ItemStack in = input.getStackInSlot(0);
        if (in.isEmpty()) {
            return Status.IDLE;
        }
        Status blocked = blocker(in);
        return blocked == null ? Status.IDLE : blocked;
    }

    private int batchSize(ItemStack in) {
        return Math.min(in.getCount(), BSConfig.waffleIronMaxBatch());
    }

    public HeatLevel heatBelow() {
        return BasinBlockEntity.getHeatLevelOf(level.getBlockState(worldPosition.below()));
    }

    /**
     * The first baking recipe for this item the heat below allows, else the first one (the status
     * then reports the heat). Not cached: a /reload or a KubeJS script applies at once.
     */
    @Nullable
    RecipeHolder<WaffleBakingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        List<RecipeHolder<WaffleBakingRecipe>> matching = level.getRecipeManager()
            .getRecipesFor(BSRecipeTypes.WAFFLE_BAKING.<SingleRecipeInput, WaffleBakingRecipe>getType(), new SingleRecipeInput(stack), level);
        if (matching.isEmpty()) {
            return null;
        }
        HeatLevel heat = heatBelow();
        for (RecipeHolder<WaffleBakingRecipe> holder : matching) {
            if (FryerBlockEntity.heatAllows(holder.value().getRequiredHeat(), heat)) {
                return holder;
            }
        }
        return matching.get(0);
    }

    @Nullable
    private WaffleBakingRecipe currentRecipe() {
        if (recipeId == null) {
            return null;
        }
        return level.getRecipeManager().byKey(recipeId)
            .map(RecipeHolder::value)
            .filter(WaffleBakingRecipe.class::isInstance)
            .map(WaffleBakingRecipe.class::cast)
            .orElse(null);
    }

    private void onInventoryChanged() {
        retry = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            sendData();
        }
    }

    // ------------------------------------------------------------------ accessors

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    public ItemStack getPlates() {
        return plates.getStackInSlot(0);
    }

    public IItemHandler getItemCapability() {
        return itemCapability;
    }

    public float getProgress() {
        return duration == 0 ? 0 : Math.min(1f, progress / (float) duration);
    }

    /** How open the lid is between two client ticks, 0 shut to 1 open. */
    public float lidOpen(float partialTicks) {
        return lidOpenPrevious + (lidOpen - lidOpenPrevious) * partialTicks;
    }

    public int comparatorSignal() {
        return ItemHelper.calcRedstoneFromInventory(output);
    }

    // ------------------------------------------------------------------ lifecycle and sync

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, input);
        ItemHelper.dropContents(level, worldPosition, output);
        ItemHelper.dropContents(level, worldPosition, plates);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Input", input.serializeNBT(registries));
        tag.put("Output", output.serializeNBT(registries));
        tag.put("Plates", plates.serializeNBT(registries));
        if (recipeId != null) {
            tag.putString("Recipe", recipeId.toString());
        }
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putBoolean("Paused", paused);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        input.deserializeNBT(registries, tag.getCompound("Input"));
        output.deserializeNBT(registries, tag.getCompound("Output"));
        plates.deserializeNBT(registries, tag.getCompound("Plates"));
        recipeId = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
        progress = tag.getInt("Progress");
        duration = tag.getInt("Duration");
        paused = tag.getBoolean("Paused");
    }

    // ------------------------------------------------------------------ goggles

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("block." + BelgianSnacks.MOD_ID + ".waffle_iron")));
        Status status = status();
        Component statusLine = status == Status.BAKING
            ? Component.translatable(status.key(), Math.round(getProgress() * 100))
            : Component.translatable(status.key());
        tooltip.add(Component.literal("     ").append(statusLine.copy().withStyle(status == Status.BAKING || status == Status.IDLE
            ? ChatFormatting.GRAY : ChatFormatting.GOLD)));
        tooltip.add(Component.literal("     ").append(Component.translatable(FryerBlockEntity.heatKey(heatBelow())).withStyle(ChatFormatting.GRAY)));
        return true;
    }

    // ------------------------------------------------------------------ item handler

    /** Slot 0 is the input (insert only), slots 1-2 the output (extract only). */
    private final class WaffleIronItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1 + output.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? input.getStackInSlot(0) : output.getStackInSlot(slot - 1);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0 ? input.insertItem(0, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 ? ItemStack.EMPTY : output.extractItem(slot - 1, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? input.getSlotLimit(0) : output.getSlotLimit(slot - 1);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && input.isItemValid(0, stack);
        }
    }
}
