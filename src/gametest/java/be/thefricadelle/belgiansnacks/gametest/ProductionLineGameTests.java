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
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Tier 1 end to end on real Create machines, nothing done by hand once meat starts falling:
 * crushing wheels -> hopper -> basin + mixer -> belt under a press -> fryer on a burner.
 * Each machine has its own creative motor; the fryer's oil is pre-filled (not what this proves).
 * Run with {@code ./gradlew runGameTestServer}.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProductionLineGameTests {
    private static final int SPEED = 64;
    // Four of each: 8 of each mince, enough crumbs, so 8 mixes of 2 pastes each.
    private static final int EACH = 4;
    private static final int EXPECTED_FRIED = 16;

    // Layout, relative to the empty_large structure (5 x 11 x 11). Flow runs towards +z.
    private static final BlockPos WHEEL_WEST = new BlockPos(1, 4, 1);
    private static final BlockPos WHEEL_EAST = new BlockPos(3, 4, 1);
    // Raw materials: a chest filled once, a hopper feeding the crushing wheels one stack at a time.
    private static final BlockPos CHEST = new BlockPos(2, 6, 1);
    private static final BlockPos FEEDER = new BlockPos(2, 5, 1);
    private static final BlockPos HOPPER = new BlockPos(2, 3, 1);
    private static final BlockPos BASIN = new BlockPos(2, 3, 2);
    private static final BlockPos MIXER = BASIN.above(2);
    private static final BlockPos BELT_START = new BlockPos(2, 2, 3);
    private static final BlockPos BELT_END = new BlockPos(2, 2, 7);
    private static final BlockPos PRESS = new BlockPos(2, 4, 6);
    private static final BlockPos FRYER = new BlockPos(2, 2, 8);

    private ProductionLineGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 2400)
    public static void meatBecomesFriedFricadellesWithoutHelp(GameTestHelper helper) {
        build(helper);
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        Set<String> reached = new LinkedHashSet<>();

        // The only input of the whole run: the chest is filled once, then the line is left alone.
        IItemHandler chest = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(CHEST), null);
        for (Item raw : new Item[] {Items.PORKCHOP, Items.BEEF, Items.CHICKEN, Items.BREAD}) {
            net.neoforged.neoforge.items.ItemHandlerHelper.insertItem(chest, new ItemStack(raw, EACH), false);
        }

        // Record every stage reached, so a failure names the one the line is stuck before.
        helper.onEachTick(() -> {
            BasinBlockEntity basin = helper.getBlockEntity(BASIN);
            IItemHandler basinItems = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(BASIN), null);
            for (int slot = 0; basinItems != null && slot < basinItems.getSlots(); slot++) {
                if (basinItems.getStackInSlot(slot).is(BSTags.MINCED_MEATS)) {
                    reached.add("minced meat in the basin");
                }
            }
            for (BlockPos pos : BlockPos.betweenClosed(BELT_START, BELT_END)) {
                IItemHandler belt = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), null);
                for (int slot = 0; belt != null && slot < belt.getSlots(); slot++) {
                    ItemStack stack = belt.getStackInSlot(slot);
                    if (stack.is(BSItems.FRICADELLE_PASTE.get())) {
                        reached.add("paste on the belt");
                    }
                    if (stack.is(BSItems.RAW_FRICADELLE.get())) {
                        reached.add("raw fricadelle pressed on the belt");
                    }
                }
            }
            if (fryer.getBasket().is(BSItems.RAW_FRICADELLE.get()) || fryer.getInput().getStackInSlot(0).is(BSItems.RAW_FRICADELLE.get())) {
                reached.add("raw fricadelle in the fryer");
            }
            if (basin == null) {
                reached.add("basin missing");
            }
        });

        helper.succeedWhen(() -> {
            int fried = 0;
            for (int slot = 0; slot < fryer.getOutput().getSlots(); slot++) {
                if (fryer.getOutput().getStackInSlot(slot).is(BSItems.FRICADELLE.get())) {
                    fried += fryer.getOutput().getStackInSlot(slot).getCount();
                }
            }
            helper.assertTrue(fried >= EXPECTED_FRIED, fried + " fricadelles fried so far; stages reached: " + reached);
        });
    }

    private static void build(GameTestHelper helper) {
        // Crushing wheels, counter-rotating, each driven along its axis by its own motor.
        helper.setBlock(WHEEL_WEST, AllBlocks.CRUSHING_WHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
        helper.setBlock(WHEEL_EAST, AllBlocks.CRUSHING_WHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Z));
        motor(helper, WHEEL_WEST.north(), Direction.SOUTH, -SPEED);
        motor(helper, WHEEL_EAST.north(), Direction.SOUTH, SPEED);

        helper.setBlock(CHEST, Blocks.CHEST);
        helper.setBlock(FEEDER, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));

        // Under the crushing gap, a hopper feeding the basin sideways.
        helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.SOUTH));
        helper.setBlock(BASIN.below(), Blocks.STONE);
        helper.setBlock(BASIN, AllBlocks.BASIN.getDefaultState().setValue(BasinBlock.FACING, Direction.SOUTH));
        // The mixer is a cog with no shaft: it turns only through a meshing cogwheel beside it.
        helper.setBlock(MIXER, AllBlocks.MECHANICAL_MIXER.getDefaultState());
        helper.setBlock(MIXER.west(), AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Y));
        motor(helper, MIXER.west().above(), Direction.DOWN, SPEED);

        // The basin spouts onto the belt start, one block down and one ahead.
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(BELT_START), helper.absolutePos(BELT_END));
        motor(helper, BELT_START.west(), Direction.EAST, SPEED);

        helper.setBlock(PRESS, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        motor(helper, PRESS.west(), Direction.EAST, SPEED);

        helper.setBlock(FRYER.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(FRYER.below());
        burner.isCreative = true;
        helper.setBlock(FRYER, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 4000), IFluidHandler.FluidAction.EXECUTE);
    }

    private static void motor(GameTestHelper helper, BlockPos pos, Direction facing, int speed) {
        helper.setBlock(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, facing));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(pos);
        motor.generatedSpeed.setValue(speed);
    }
}
