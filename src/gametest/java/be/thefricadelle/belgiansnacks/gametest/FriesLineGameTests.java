/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.LinkedHashSet;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * v2 fries on real Create machines (docs/13, section 5.1): potatoes on a belt through a mechanical
 * saw into a fryer on a lit burner, a second fryer on a seething burner, then a belt under a deployer
 * holding paper cones. Between the fryers and onto the cone belt the test moves the items, as the tier
 * 2 and 3 line test does: a fryer sits on its burner, so in a factory a funnel or a hopper beside it
 * takes the output through the same capability (FryerGameTests.aHopperBelowTakesOnlyTheOutput).
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FriesLineGameTests {
    private static final int SPEED = 64;
    private static final int POTATOES = 2;
    private static final int PORTIONS = POTATOES * 4;

    // Layout, relative to the empty_large structure (5 x 11 x 11). Both belts run towards +z.
    private static final BlockPos CUT_BELT_START = new BlockPos(2, 2, 1);
    private static final BlockPos CUT_BELT_END = new BlockPos(2, 2, 3);
    private static final BlockPos SAW = new BlockPos(2, 2, 4);
    private static final BlockPos FEED_BELT_START = new BlockPos(2, 2, 5);
    private static final BlockPos FEED_BELT_END = new BlockPos(2, 2, 7);
    private static final BlockPos BLANCHER = new BlockPos(2, 2, 8);
    private static final BlockPos FINISHER = new BlockPos(4, 2, 9);
    private static final BlockPos CONE_BELT_START = new BlockPos(4, 2, 1);
    private static final BlockPos CONE_BELT_END = new BlockPos(4, 2, 6);
    private static final BlockPos DEPLOYER = new BlockPos(4, 4, 3);
    // A belt drops what reaches its end: a depot at its end catches the cones.
    private static final BlockPos DEPOT = CONE_BELT_END.south();

    private FriesLineGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 1600)
    public static void potatoesBecomeConesOfFries(GameTestHelper helper) {
        build(helper);
        FryerBlockEntity blancher = helper.getBlockEntity(BLANCHER);
        FryerBlockEntity finisher = helper.getBlockEntity(FINISHER);
        Set<String> reached = new LinkedHashSet<>();

        // The only input: potatoes on the first belt, once its segments have ticked.
        helper.runAfterDelay(10, () -> insert(helper, CUT_BELT_START, new ItemStack(Items.POTATO, POTATOES)));

        int[] collected = {0};
        helper.onEachTick(() -> {
            // A depot holds one stack: taken off as it arrives, as a funnel would.
            IItemHandler depot = items(helper, DEPOT);
            if (depot != null && depot.getStackInSlot(0).is(BSItems.CONE_OF_FRIES.get())) {
                collected[0] += depot.extractItem(0, 64, false).getCount();
            }
            IItemHandler saw = items(helper, SAW);
            for (int slot = 0; saw != null && slot < saw.getSlots(); slot++) {
                if (!saw.getStackInSlot(slot).isEmpty()) {
                    reached.add("in the saw: " + saw.getStackInSlot(slot));
                }
            }
            if (count(helper, FEED_BELT_START, FEED_BELT_END, BSItems.RAW_FRIES.get()) > 0) {
                reached.add("raw fries out of the saw");
            }
            if (blancher.getBasket().is(BSItems.RAW_FRIES.get())) {
                reached.add("blanching");
            }
            move(blancher.getOutput(), finisher.getItemCapability(), BSItems.BLANCHED_FRIES.get(), reached, "blanched fries to the second fryer");
            IItemHandler coneBelt = items(helper, CONE_BELT_START);
            for (int slot = 0; slot < finisher.getOutput().getSlots(); slot++) {
                ItemStack done = finisher.getOutput().getStackInSlot(slot);
                if (done.is(BSItems.FRIES.get()) && coneBelt != null
                    && coneBelt.insertItem(0, done.copyWithCount(1), true).isEmpty()) {
                    coneBelt.insertItem(0, finisher.getOutput().extractItem(slot, 1, false), false);
                    reached.add("fries onto the cone belt");
                }
            }
        });

        helper.succeedWhen(() -> {
            helper.assertValueEqual(collected[0], PORTIONS, "cones of fries; stages: " + reached
                + "; blancher " + blancher.status() + ", finisher " + finisher.status() + "; cone belt " + contents(helper)
                + "; deployer holds " + items(helper, DEPLOYER).getStackInSlot(0));
            helper.assertValueEqual(items(helper, DEPLOYER).getStackInSlot(0).getCount(), 16 - PORTIONS, "paper cones left");
        });
    }

    private static void build(GameTestHelper helper) {
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(CUT_BELT_START), helper.absolutePos(CUT_BELT_END));
        motor(helper, CUT_BELT_START.west(), Direction.EAST);
        // A saw facing up, in line with the belt: items ride into it and come out on the next belt.
        helper.setBlock(SAW, AllBlocks.MECHANICAL_SAW.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP)
            .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
        // Its speed sets which way the items leave: negative here sends them on towards +z.
        motor(helper, SAW.west(), Direction.EAST, -SPEED);
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(FEED_BELT_START), helper.absolutePos(FEED_BELT_END));
        motor(helper, FEED_BELT_START.west(), Direction.EAST);
        fryer(helper, BLANCHER, HeatLevel.KINDLED);
        fryer(helper, FINISHER, HeatLevel.SEETHING);

        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(CONE_BELT_START), helper.absolutePos(CONE_BELT_END));
        motor(helper, CONE_BELT_START.west(), Direction.EAST);
        helper.setBlock(DEPOT, AllBlocks.DEPOT.getDefaultState());
        helper.setBlock(DEPLOYER, AllBlocks.DEPLOYER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN)
            .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
        motor(helper, DEPLOYER.west(), Direction.EAST);
        IItemHandler hand = items(helper, DEPLOYER);
        helper.assertTrue(hand != null && hand.insertItem(0, BSItems.PAPER_CONE.asStack(16), false).isEmpty(), "the deployer refused the cones");
    }

    private static void fryer(GameTestHelper helper, BlockPos pos, HeatLevel heat) {
        helper.setBlock(pos.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(pos.below());
        burner.isCreative = true;
        helper.setBlock(pos, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = helper.getBlockEntity(pos);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE);
    }

    private static void move(IItemHandler from, IItemHandler to, Item item, Set<String> reached, String stage) {
        for (int slot = 0; slot < from.getSlots(); slot++) {
            ItemStack stack = from.getStackInSlot(slot);
            if (stack.is(item)) {
                ItemStack left = to.insertItem(0, from.extractItem(slot, stack.getCount(), false), false);
                if (!left.isEmpty()) {
                    from.insertItem(slot, left, false);
                }
                reached.add(stage);
            }
        }
    }

    private static int count(GameTestHelper helper, BlockPos start, BlockPos end, Item item) {
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
            IItemHandler belt = items(helper, pos);
            for (int slot = 0; belt != null && slot < belt.getSlots(); slot++) {
                ItemStack stack = belt.getStackInSlot(slot);
                if (stack.is(item)) {
                    found += stack.getCount();
                }
            }
        }
        return found;
    }

    private static String contents(GameTestHelper helper) {
        StringBuilder text = new StringBuilder();
        for (BlockPos pos : BlockPos.betweenClosed(CONE_BELT_START, DEPOT)) {
            IItemHandler belt = items(helper, pos);
            for (int slot = 0; belt != null && slot < belt.getSlots(); slot++) {
                if (!belt.getStackInSlot(slot).isEmpty()) {
                    text.append(pos.getZ()).append(':').append(belt.getStackInSlot(slot)).append(' ');
                }
            }
        }
        return text.toString();
    }

    private static void motor(GameTestHelper helper, BlockPos pos, Direction facing) {
        motor(helper, pos, facing, SPEED);
    }

    private static void motor(GameTestHelper helper, BlockPos pos, Direction facing, int speed) {
        helper.setBlock(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, facing));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(pos);
        motor.generatedSpeed.setValue(speed);
    }

    private static IItemHandler items(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), null);
    }

    private static void insert(GameTestHelper helper, BlockPos belt, ItemStack stack) {
        IItemHandler handler = items(helper, belt);
        helper.assertTrue(handler != null && handler.insertItem(0, stack, false).isEmpty(), "the belt at " + belt + " refused " + stack);
    }
}
