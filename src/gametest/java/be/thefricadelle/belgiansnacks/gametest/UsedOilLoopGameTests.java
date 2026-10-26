/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * v2 used oil (docs/13, section 6.2) in a closed loop: a real fryer fries without a pause while a real
 * mixer filters its used oil back into frying oil. The test does only what pipes, a pump and a funnel
 * would: carry the used oil to the basin, the filtered oil back, keep filters in the basin, top the fat
 * up from a seed-oil line, and take the fried output. The one-hour run in a world is a manual check.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UsedOilLoopGameTests {
    private static final BlockPos FRYER = new BlockPos(1, 2, 1);
    private static final BlockPos BASIN = new BlockPos(3, 1, 6);
    private static final BlockPos MIXER = BASIN.above(2);
    private static final int RUN_TICKS = 2400;

    private UsedOilLoopGameTests() {
    }

    // Its own batch: a long run beside other tests would only slow their light updates.
    @GameTest(template = "empty_large", timeoutTicks = RUN_TICKS + 200, batch = "used_oil_loop")
    public static void fryingAndFilteringRunWithoutAStop(GameTestHelper helper) {
        helper.setBlock(FRYER.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(FRYER.below());
        burner.isCreative = true;
        helper.setBlock(FRYER, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        helper.setBlock(BASIN.below(), Blocks.STONE);
        helper.setBlock(BASIN, AllBlocks.BASIN.getDefaultState());
        helper.setBlock(MIXER, AllBlocks.MECHANICAL_MIXER.getDefaultState());
        helper.setBlock(MIXER.west(), AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Y));
        helper.setBlock(MIXER.west().above(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(MIXER.west().above());
        motor.generatedSpeed.setValue(128);
        var level = helper.getLevel();
        IFluidHandler basinTanks = level.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(BASIN), null);
        IItemHandler basinItems = level.getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(BASIN), null);
        FluidStack fresh = new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getTank().getCapability().fill(fresh.copy(), IFluidHandler.FluidAction.EXECUTE);

        int[] fried = {0};
        int[] recycled = {0};
        int[] filtersUsed = {0};
        int[] blocked = {0};
        int[] longestBlock = {0};
        helper.onEachTick(() -> {
            // The seed line keeps raw fricadelles coming.
            if (fryer.getInput().getStackInSlot(0).isEmpty()) {
                fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(16), false);
            }
            // A funnel takes the fried output.
            for (int slot = 1; slot < fryer.getItemCapability().getSlots(); slot++) {
                fried[0] += fryer.getItemCapability().extractItem(slot, 64, false).getCount();
            }
            // A pump carries the used oil to the basin, as fast as the basin takes it.
            FluidStack used = fryer.getFluidCapability().drain(250, IFluidHandler.FluidAction.SIMULATE);
            if (!used.isEmpty()) {
                int moved = basinTanks.fill(used, IFluidHandler.FluidAction.EXECUTE);
                fryer.getFluidCapability().drain(moved, IFluidHandler.FluidAction.EXECUTE);
            }
            // Filters wait in the basin; the used ones come out.
            boolean hasFilter = false;
            for (int slot = 0; slot < basinItems.getSlots(); slot++) {
                ItemStack stack = basinItems.getStackInSlot(slot);
                hasFilter |= stack.is(BSItems.OIL_FILTER.get());
                if (stack.is(BSItems.USED_OIL_FILTER.get())) {
                    filtersUsed[0] += basinItems.extractItem(slot, 64, false).getCount();
                }
            }
            if (!hasFilter) {
                basinItems.insertItem(0, BSItems.OIL_FILTER.asStack(), false);
            }
            // The filtered oil goes back into the fryer.
            FluidStack clean = basinTanks.drain(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 1000), IFluidHandler.FluidAction.SIMULATE);
            if (!clean.isEmpty()) {
                int back = fryer.getFluidCapability().fill(clean, IFluidHandler.FluidAction.EXECUTE);
                basinTanks.drain(new FluidStack(clean.getFluid(), back), IFluidHandler.FluidAction.EXECUTE);
                recycled[0] += back;
            }
            // The seed-oil line tops the fat up when it runs low.
            if (fryer.getTank().getPrimaryHandler().getFluidAmount() < 300) {
                fryer.getTank().getCapability().fill(fresh.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
            blocked[0] = fryer.status() == FryerBlockEntity.Status.USED_OIL_FULL ? blocked[0] + 1 : 0;
            longestBlock[0] = Math.max(longestBlock[0], blocked[0]);
        });
        helper.runAtTickTime(RUN_TICKS, () -> {
            helper.assertTrue(fried[0] >= 16 * 15, "only " + fried[0] + " fricadelles fried in " + RUN_TICKS + " ticks");
            helper.assertTrue(longestBlock[0] <= 60, "blocked on used oil for " + longestBlock[0] + " ticks in a row");
            helper.assertTrue(filtersUsed[0] >= 1 && recycled[0] >= 800, "filtered back " + recycled[0] + " mB with " + filtersUsed[0] + " filters");
            helper.succeed();
        });
    }
}
