/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
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
 * v2 friterie snacks on real Create machines (docs/13, section 5.10): a belt runs under deployers, a
 * spout and a press, one pass each. The breaded line takes two different fillings in a row: they
 * share one transitional item, and each still comes out as its own raw snack.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FriterieLineGameTests {
    private static final int SPEED = 64;
    private static final int STOCK = 16;
    private static final int TANK = 1000;
    private static final BlockPos BELT_START = new BlockPos(2, 2, 1);
    private static final int STATION_Y = 4;

    private record Station(ItemStack held, Fluid sauce) {
        static Station deployer(ItemStack held) {
            return new Station(held, null);
        }

        static Station spout(Fluid sauce) {
            return new Station(ItemStack.EMPTY, sauce);
        }

        static Station press() {
            return new Station(ItemStack.EMPTY, null);
        }
    }

    private FriterieLineGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void mitrailletteIsAssembledOnRealMachines(GameTestHelper helper) {
        run(helper, List.of(BSSnacks.HALF_BAGUETTE.asStack()), List.of(BSSnacks.MITRAILLETTE.get()),
            List.of(Station.deployer(BSItems.FRICADELLE.asStack(STOCK)), Station.deployer(BSItems.FRIES.asStack(STOCK)),
                Station.spout(BSFluids.MAYONNAISE.get().getSource())), 100);
    }

    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void twoCroquettesShareOneBreadingLine(GameTestHelper helper) {
        run(helper, List.of(BSSnacks.CHEESE_CROQUETTE_MIX.asStack(), BSSnacks.CHICKEN_CROQUETTE_MIX.asStack()),
            List.of(BSSnacks.RAW_CHEESE_CROQUETTE.get(), BSSnacks.RAW_CHICKEN_CROQUETTE.get()),
            List.of(Station.deployer(new ItemStack(Items.EGG, STOCK)), Station.deployer(BSItems.BREAD_CRUMBS.asStack(STOCK)), Station.press()), 0);
    }

    // The deployer keeps its shears (keep_held_item): two chickens, four wings, the same pair of shears.
    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public static void shearsCutWingsAndStayInTheDeployer(GameTestHelper helper) {
        run(helper, List.of(new ItemStack(Items.CHICKEN), new ItemStack(Items.CHICKEN)),
            List.of(BSSnacks.RAW_CHICKEN_WINGS.get(), BSSnacks.RAW_CHICKEN_WINGS.get(), BSSnacks.RAW_CHICKEN_WINGS.get(), BSSnacks.RAW_CHICKEN_WINGS.get()),
            List.of(Station.deployer(new ItemStack(Items.SHEARS))), 0);
    }

    private static void run(GameTestHelper helper, List<ItemStack> inputs, List<Item> expected, List<Station> stations, int saucePerItem) {
        BlockPos beltEnd = BELT_START.south(stations.size() + 2);
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(BELT_START), helper.absolutePos(beltEnd));
        motor(helper, BELT_START.west(), Direction.EAST);
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            BlockPos pos = stationPos(i);
            if (station.sauce() != null) {
                helper.setBlock(pos, AllBlocks.SPOUT.getDefaultState());
                helper.assertValueEqual(tank(helper, pos).fill(new FluidStack(station.sauce(), TANK), IFluidHandler.FluidAction.EXECUTE), TANK, "sauce in");
            } else if (!station.held().isEmpty()) {
                helper.setBlock(pos, AllBlocks.DEPLOYER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN)
                    .setValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
                motor(helper, pos.west(), Direction.EAST);
                helper.assertTrue(items(helper, pos).insertItem(0, station.held().copy(), false).isEmpty(), "deployer at " + pos);
            } else {
                helper.setBlock(pos, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
                motor(helper, pos.west(), Direction.EAST);
            }
        }
        // One input every 40 ticks, so they never ride the belt side by side.
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack input = inputs.get(i);
            helper.runAfterDelay(10 + 40L * i, () -> helper.assertTrue(items(helper, BELT_START).insertItem(0, input.copy(), false).isEmpty(), "belt refused " + input));
        }
        List<Item> collected = new ArrayList<>();
        helper.onEachTick(() -> {
            IItemHandler end = items(helper, beltEnd);
            ItemStack last = end == null ? ItemStack.EMPTY : end.getStackInSlot(0);
            if (!last.isEmpty() && expected.contains(last.getItem())) {
                ItemStack out = end.extractItem(0, 64, false);
                for (int n = 0; n < out.getCount(); n++) {
                    collected.add(out.getItem());
                }
            } else if (!last.isEmpty() && !last.is(inputs.get(0).getItem()) && inputs.stream().noneMatch(in -> last.is(in.getItem()))
                && BuiltInRegistries.ITEM.getKey(last.getItem()).getPath().startsWith("incomplete")) {
                helper.fail("left the line unfinished: " + last + " " + last.getComponentsPatch());
            }
        });
        helper.succeedWhen(() -> {
            helper.assertValueEqual(collected, expected, "snacks out of the line");
            for (int i = 0; i < stations.size(); i++) {
                Station station = stations.get(i);
                BlockPos pos = stationPos(i);
                if (station.sauce() != null) {
                    helper.assertValueEqual(TANK - tank(helper, pos).getFluidInTank(0).getAmount(), saucePerItem * inputs.size(), "sauce used");
                } else if (station.held().is(Items.SHEARS)) {
                    // Without keep_held_item Create would wear the shears down by one use per chicken.
                    ItemStack shears = items(helper, pos).getStackInSlot(0);
                    helper.assertTrue(shears.is(Items.SHEARS), "the deployer kept its shears");
                    helper.assertValueEqual(shears.getDamageValue(), 0, "wear on the shears");
                } else if (!station.held().isEmpty()) {
                    helper.assertValueEqual(items(helper, pos).getStackInSlot(0).getCount(), STOCK - inputs.size(), station.held().getItem() + " used");
                }
            }
        });
    }

    private static BlockPos stationPos(int index) {
        return new BlockPos(2, STATION_Y, 2 + index);
    }

    private static void motor(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, facing));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(pos);
        motor.generatedSpeed.setValue(SPEED);
    }

    private static IItemHandler items(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), null);
    }

    private static IFluidHandler tank(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), null);
    }
}
