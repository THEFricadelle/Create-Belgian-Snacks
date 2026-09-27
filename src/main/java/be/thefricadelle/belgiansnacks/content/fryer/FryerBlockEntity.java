/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.fryer;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import be.thefricadelle.belgiansnacks.registry.BSDataComponents;
import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Fries whole batches: the input stack (up to maxBatch, one item for FRYER_ONE_AT_A_TIME) and the
 * fat for all of it move into the basket when a batch starts. Without enough heat the batch pauses
 * and keeps its progress; nothing is ever consumed without being fried.
 */
public class FryerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private static final int IDLE_RETRY_TICKS = 10;
    private static final int DEFAULT_DURATION = 100;
    // Not an EnumSet: Create Heat JS adds HeatLevel constants at runtime.
    private static final Set<HeatLevel> CREATE_HEAT_LEVELS =
        Set.of(HeatLevel.NONE, HeatLevel.SMOULDERING, HeatLevel.FADING, HeatLevel.KINDLED, HeatLevel.SEETHING);

    public enum Status {
        IDLE, FRYING, NO_RECIPE, NO_HEAT, NO_FAT, OUTPUT_FULL;

        public String key() {
            return BelgianSnacks.MOD_ID + ".fryer.status." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    // Set from addBehaviours, which the SmartBlockEntity constructor calls before field initialisers:
    // no initialiser here, or it would be reset to null.
    private SmartFluidTankBehaviour tank;

    private final ItemStackHandler input = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return BSConfig.fryerMaxBatch();
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
    // The basket: what is being fried right now, already paid for in fat.
    private final ItemStackHandler basket = new ItemStackHandler(1);
    private final IItemHandler itemCapability = new FryerItemHandler();

    @Nullable
    private ResourceLocation recipeId;
    private int progress;
    private int duration;
    private boolean paused;
    private int retry;

    public FryerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BSBlockEntities.FRYER.get(), (be, side) -> be.itemCapability);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BSBlockEntities.FRYER.get(), (be, side) -> be.tank.getCapability());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, BSConfig.fryerTankCapacity())
            .whenFluidUpdates(() -> retry = 0);
        tank.getPrimaryHandler().setValidator(stack -> stack.is(BSTags.FRYING_OILS));
        behaviours.add(tank);
        // A belt ending on the fryer drops its items in, through the same item handler as a funnel.
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
        if (!basket.getStackInSlot(0).isEmpty()) {
            fry();
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
        RecipeHolder<FryingRecipe> holder = findRecipe(in);
        FryingRecipe recipe = holder.value();
        int count = batchSize(in);
        SizedFluidIngredient fat = recipe.getFat();
        if (fat != null) {
            tank.getPrimaryHandler().drain(fat.amount() * count, IFluidHandler.FluidAction.EXECUTE);
        }
        basket.setStackInSlot(0, input.extractItem(0, count, false));
        recipeId = holder.id();
        int base = recipe.getProcessingDuration() > 0 ? recipe.getProcessingDuration() : DEFAULT_DURATION;
        duration = Math.max(1, (int) Math.round(base / BSConfig.fryerSpeedMultiplier()));
        progress = 0;
        paused = false;
        notifyUpdate();
    }

    private void fry() {
        FryingRecipe recipe = currentRecipe();
        if (recipe == null) {
            // The recipe vanished in a /reload: hand the basket back untouched rather than lose it.
            finishBatch(List.of(basket.getStackInSlot(0).copy()));
            return;
        }
        boolean hot = heatAllows(recipe);
        if (hot == paused) {
            paused = !hot;
            notifyUpdate();
        }
        if (!hot) {
            return;
        }
        if (++progress < duration) {
            return;
        }
        int count = basket.getStackInSlot(0).getCount();
        List<ItemStack> results = new java.util.ArrayList<>();
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
        basket.setStackInSlot(0, ItemStack.EMPTY);
        recipeId = null;
        progress = 0;
        duration = 0;
        paused = false;
        retry = 0;
        notifyUpdate();
    }

    private void clientTick() {
        if (basket.getStackInSlot(0).isEmpty() || paused) {
            return;
        }
        // Local estimate between server updates, for the goggles percentage and the effects.
        if (progress < duration) {
            progress++;
        }
        var random = level.random;
        double surface = worldPosition.getY() + fatSurface();
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, worldPosition.getX() + 0.2 + random.nextDouble() * 0.6, surface,
                worldPosition.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.02, 0);
        }
        if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, surface + 0.1, worldPosition.getZ() + 0.5, 0, 0.03, 0);
        }
    }

    // ------------------------------------------------------------------ rules

    /** Why the input cannot start a batch right now, or null if it can. */
    @Nullable
    private Status blocker(ItemStack in) {
        RecipeHolder<FryingRecipe> holder = findRecipe(in);
        if (holder == null) {
            return Status.NO_RECIPE;
        }
        FryingRecipe recipe = holder.value();
        if (!heatAllows(recipe)) {
            return Status.NO_HEAT;
        }
        int count = batchSize(in);
        SizedFluidIngredient fat = recipe.getFat();
        if (fat != null) {
            FluidStack inTank = tank.getPrimaryHandler().getFluid();
            if (!fat.ingredient().test(inTank) || inTank.getAmount() < fat.amount() * count) {
                return Status.NO_FAT;
            }
        }
        if (!outputFits(recipe, count)) {
            return Status.OUTPUT_FULL;
        }
        return null;
    }

    public Status status() {
        if (!basket.getStackInSlot(0).isEmpty()) {
            return paused ? Status.NO_HEAT : Status.FRYING;
        }
        ItemStack in = input.getStackInSlot(0);
        if (in.isEmpty()) {
            return Status.IDLE;
        }
        Status blocked = blocker(in);
        return blocked == null ? Status.IDLE : blocked;
    }

    private int batchSize(ItemStack in) {
        return in.is(BSTags.FRYER_ONE_AT_A_TIME) ? 1 : Math.min(in.getCount(), BSConfig.fryerMaxBatch());
    }

    private boolean heatAllows(FryingRecipe recipe) {
        return heatAllows(recipe.getRequiredHeat(), heatBelow());
    }

    public HeatLevel heatBelow() {
        // Create's own lookup: also honours heat sources other mods register through it.
        return BasinBlockEntity.getHeatLevelOf(level.getBlockState(worldPosition.below()));
    }

    private boolean outputFits(FryingRecipe recipe, int count) {
        ItemStackHandler simulated = new ItemStackHandler(output.getSlots());
        for (int slot = 0; slot < output.getSlots(); slot++) {
            simulated.setStackInSlot(slot, output.getStackInSlot(slot).copy());
        }
        for (ProcessingOutput result : recipe.getRollableResults()) {
            ItemStack stack = result.getStack();
            int total = stack.getCount() * count;
            while (total > 0) {
                int part = Math.min(total, stack.getMaxStackSize());
                if (!ItemHandlerHelper.insertItemStacked(simulated, stack.copyWithCount(part), false).isEmpty()) {
                    return false;
                }
                total -= part;
            }
        }
        return true;
    }

    /**
     * Like Create's basin: of the frying recipes for this item, the first the heat below allows, else
     * the first one (its heat is what the status reports). Not cached: a pack has a handful of frying
     * recipes, and one a /reload or a KubeJS script adds must apply at once.
     */
    @Nullable
    RecipeHolder<FryingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        List<RecipeHolder<FryingRecipe>> matching = level.getRecipeManager()
            .getRecipesFor(BSRecipeTypes.FRYING.<SingleRecipeInput, FryingRecipe>getType(), new SingleRecipeInput(stack), level);
        if (matching.isEmpty()) {
            return null;
        }
        HeatLevel heat = heatBelow();
        for (RecipeHolder<FryingRecipe> holder : matching) {
            if (heatAllows(holder.value().getRequiredHeat(), heat)) {
                return holder;
            }
        }
        return matching.get(0);
    }

    /**
     * Create's rules for Create's own conditions and levels: no heat needed always fries, heated needs
     * a lit burner, superheated a seething one. Create Heat JS (in Arcadia) rewrites testBlazeBurner so
     * that "no heat" fails on any lit burner and "heated" on a fading one; the conditions and levels it
     * adds still go through it.
     */
    public static boolean heatAllows(HeatCondition need, HeatLevel heat) {
        if (CREATE_HEAT_LEVELS.contains(heat)) {
            if (need == HeatCondition.NONE) {
                return true;
            }
            if (need == HeatCondition.HEATED) {
                return heat.isAtLeast(HeatLevel.FADING);
            }
            if (need == HeatCondition.SUPERHEATED) {
                return heat == HeatLevel.SEETHING;
            }
        }
        return need.testBlazeBurner(heat);
    }

    @Nullable
    private FryingRecipe currentRecipe() {
        if (recipeId == null) {
            return null;
        }
        return level.getRecipeManager().byKey(recipeId)
            .map(RecipeHolder::value)
            .filter(FryingRecipe.class::isInstance)
            .map(FryingRecipe.class::cast)
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

    public SmartFluidTankBehaviour getTank() {
        return tank;
    }

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    public ItemStack getBasket() {
        return basket.getStackInSlot(0);
    }

    public IItemHandler getItemCapability() {
        return itemCapability;
    }

    public float getProgress() {
        return duration == 0 ? 0 : Math.min(1f, progress / (float) duration);
    }

    /** Height of the fat surface inside the block, 0 to 1. */
    public float fatSurface() {
        var handler = tank.getPrimaryHandler();
        float fill = handler.getCapacity() == 0 ? 0 : handler.getFluidAmount() / (float) handler.getCapacity();
        // Any fat at all fills the vat to y = 9 (a deep fryer is never a puddle), full reaches y = 15.
        // Empty, the surface is the vat floor (y = 4, the fryer stands on feet).
        return fill <= 0 ? 4 / 16f : 9 / 16f + 6 / 16f * Math.min(1f, fill);
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
        ItemHelper.dropContents(level, worldPosition, basket);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Input", input.serializeNBT(registries));
        tag.put("Output", output.serializeNBT(registries));
        tag.put("Basket", basket.serializeNBT(registries));
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
        basket.deserializeNBT(registries, tag.getCompound("Basket"));
        recipeId = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
        progress = tag.getInt("Progress");
        duration = tag.getInt("Duration");
        paused = tag.getBoolean("Paused");
    }

    // The fat travels with the item when the fryer is broken (loot table copies this component).
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        FluidStack fat = tank.getPrimaryHandler().getFluid();
        if (!fat.isEmpty()) {
            components.set(BSDataComponents.FRYER_FLUID.get(), SimpleFluidContent.copyOf(fat));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        SimpleFluidContent fat = components.get(BSDataComponents.FRYER_FLUID.get());
        if (fat != null && !fat.isEmpty()) {
            tank.getPrimaryHandler().setFluid(fat.copy());
        }
    }

    // ------------------------------------------------------------------ goggles

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("block." + BelgianSnacks.MOD_ID + ".fryer")));
        Status status = status();
        Component statusLine = status == Status.FRYING
            ? Component.translatable(status.key(), Math.round(getProgress() * 100))
            : Component.translatable(status.key());
        tooltip.add(Component.literal("     ").append(statusLine.copy().withStyle(status == Status.FRYING || status == Status.IDLE
            ? ChatFormatting.GRAY : ChatFormatting.GOLD)));
        tooltip.add(Component.literal("     ").append(Component.translatable(heatKey(heatBelow())).withStyle(ChatFormatting.GRAY)));
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
        return true;
    }

    public static String heatKey(HeatLevel heat) {
        // No switch: Create Heat JS (in Arcadia) adds HeatLevel constants, which an exhaustive switch
        // would reject with a MatchException. Ordered comparisons, as Create itself does, place them.
        String level = heat.isAtLeast(HeatLevel.SEETHING) ? "superheated" : heat.isAtLeast(HeatLevel.FADING) ? "heated" : "none";
        return BelgianSnacks.MOD_ID + ".fryer.heat." + level;
    }

    // ------------------------------------------------------------------ item handler

    /** Slot 0 is the input (insert only), slots 1-2 the output (extract only). */
    private final class FryerItemHandler implements IItemHandler {
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
