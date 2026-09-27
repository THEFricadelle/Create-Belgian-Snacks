/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
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
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Tier 2 on real Create machines: an Exceptional Paste rides a belt under a deployer (spices), two
 * spouts (mayonnaise, curry ketchup), a deployer (onion, or beetroot without an onion mod) and a
 * press. The test only carries the unfinished item from the belt end back to its start, the loop a
 * player builds; after three loops the raw THE_Fricadelle goes into a fryer.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TheFricadelleLineGameTests {
    private static final int SPEED = 64;
    private static final int LOOPS = 3;
    private static final int SAUCE = 1000;

    // empty_large is 5 x 11 x 11; the belt runs towards +z. Create looks for processing machines two
    // blocks above a belt (BeltInventory), spouts included.
    private static final BlockPos BELT_START = new BlockPos(2, 2, 1);
    private static final BlockPos BELT_END = new BlockPos(2, 2, 8);
    private static final BlockPos SPICES = new BlockPos(2, 4, 2);
    private static final BlockPos MAYONNAISE = new BlockPos(2, 4, 3);
    private static final BlockPos CURRY = new BlockPos(2, 4, 4);
    private static final BlockPos ONION = new BlockPos(2, 4, 5);
    private static final BlockPos PRESS = new BlockPos(2, 4, 6);
    private static final BlockPos FRYER = new BlockPos(4, 2, 9);

    private TheFricadelleLineGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 3600)
    public static void exceptionalPasteBecomesTheFricadelleOnRealMachines(GameTestHelper helper) {
        Item onion = onion();
        build(helper, onion);
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        Set<String> reached = new LinkedHashSet<>();
        int[] loopsSeen = {0};

        // A belt takes items once its segments have ticked.
        helper.runAfterDelay(10, () -> insert(helper, BELT_START, BSItems.EXCEPTIONAL_PASTE.asStack()));

        helper.onEachTick(() -> {
            IItemHandler end = items(helper, BELT_END);
            ItemStack last = end == null ? ItemStack.EMPTY : end.getStackInSlot(0);
            if (last.is(BSItems.INCOMPLETE_THE_FRICADELLE.get()) && loopsSeen[0] >= LOOPS) {
                helper.fail("still unfinished after " + loopsSeen[0] + " passes: " + last.getComponentsPatch() + "; mayonnaise "
                    + fluid(helper, MAYONNAISE) + ", curry " + fluid(helper, CURRY) + ", spices " + held(helper, SPICES)
                    + ", onions " + held(helper, ONION));
            } else if (last.is(BSItems.INCOMPLETE_THE_FRICADELLE.get())) {
                // Back to the start: what a looping belt does in a player's factory.
                ItemStack moved = end.extractItem(0, 1, false);
                loopsSeen[0]++;
                reached.add("loop " + loopsSeen[0] + " done");
                insert(helper, BELT_START, moved);
            } else if (last.is(BSItems.RAW_THE_FRICADELLE.get())) {
                reached.add("raw THE_Fricadelle after " + (loopsSeen[0] + 1) + " passes");
                fryer.getItemCapability().insertItem(0, end.extractItem(0, 1, false), false);
            }
        });

        helper.succeedWhen(() -> {
            helper.assertTrue(fryer.getOutput().getStackInSlot(0).is(BSItems.THE_FRICADELLE.get()), "no THE_Fricadelle yet; stages: " + reached);
            helper.assertValueEqual(loopsSeen[0] + 1, LOOPS, "passes along the line");
            helper.assertValueEqual(fluid(helper, MAYONNAISE), SAUCE - 300, "mayonnaise used (3 x 100 mB)");
            helper.assertValueEqual(fluid(helper, CURRY), SAUCE - 300, "curry ketchup used (3 x 100 mB)");
            helper.assertValueEqual(held(helper, SPICES), 64 - LOOPS, "spices used");
            helper.assertValueEqual(held(helper, ONION), 64 - LOOPS, BuiltInRegistries.ITEM.getKey(onion) + " used");
            helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 1000 - 25, "fat for one THE_Fricadelle");
        });
    }

    private static Item onion() {
        var onions = BuiltInRegistries.ITEM.getTag(BSTags.C_ONIONS);
        return onions.isPresent() && onions.get().size() > 0 ? onions.get().get(0).value() : Items.BEETROOT;
    }

    private static void build(GameTestHelper helper, Item onion) {
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(BELT_START), helper.absolutePos(BELT_END));
        motor(helper, BELT_START.west(), Direction.EAST);

        deployer(helper, SPICES, BSItems.BELGIAN_SPICES.asStack(64));
        spout(helper, MAYONNAISE, BSFluids.MAYONNAISE.get().getSource());
        spout(helper, CURRY, BSFluids.CURRY_KETCHUP.get().getSource());
        deployer(helper, ONION, new ItemStack(onion, 64));
        helper.setBlock(PRESS, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        motor(helper, PRESS.west(), Direction.EAST);

        helper.setBlock(FRYER.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(FRYER.below());
        burner.isCreative = true;
        helper.setBlock(FRYER, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE);
    }

    // Facing down with its shaft along x, turned by a motor on its west side, holding a full stack.
    private static void deployer(GameTestHelper helper, BlockPos pos, ItemStack held) {
        helper.setBlock(pos, AllBlocks.DEPLOYER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN)
            .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
        motor(helper, pos.west(), Direction.EAST);
        IItemHandler hand = items(helper, pos);
        helper.assertTrue(hand != null && hand.insertItem(0, held, false).isEmpty(), "deployer at " + pos + " refused " + held);
    }

    private static void spout(GameTestHelper helper, BlockPos pos, Fluid sauce) {
        helper.setBlock(pos, AllBlocks.SPOUT.getDefaultState());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), null);
        helper.assertValueEqual(tank.fill(new FluidStack(sauce, SAUCE), IFluidHandler.FluidAction.EXECUTE), SAUCE, "sauce in the spout at " + pos);
    }

    private static void motor(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, facing));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(pos);
        motor.generatedSpeed.setValue(SPEED);
    }

    private static IItemHandler items(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), null);
    }

    private static void insert(GameTestHelper helper, BlockPos belt, ItemStack stack) {
        IItemHandler handler = items(helper, belt);
        helper.assertTrue(handler != null && handler.insertItem(0, stack, false).isEmpty(), "the belt at " + belt + " refused " + stack);
    }

    private static int fluid(GameTestHelper helper, BlockPos spout) {
        return helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(spout), null).getFluidInTank(0).getAmount();
    }

    private static int held(GameTestHelper helper, BlockPos deployer) {
        return items(helper, deployer).getStackInSlot(0).getCount();
    }
}
