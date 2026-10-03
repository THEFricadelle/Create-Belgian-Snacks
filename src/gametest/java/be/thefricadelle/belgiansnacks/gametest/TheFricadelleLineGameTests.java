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
import java.util.List;
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
 * Tiers 2 and 3 on real Create machines: the paste rides a belt under deployers, spouts and a press
 * (Create looks for processing machines two blocks above a belt, spouts included). The test only
 * carries the unfinished item from the belt end back to its start, the loop a player builds; after
 * the last pass the raw fricadelle goes into a fryer on the right burner with the right fat.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TheFricadelleLineGameTests {
    private static final int SPEED = 64;
    private static final int STOCK = 64;
    // A spout holds 1000 mB; a factory pipes sauce in, the test tops it up every tick and counts.
    private static final int TANK = 1000;
    // empty_large is 5 x 11 x 11; the belt runs towards +z from z = 1, stations from z = 2.
    private static final BlockPos BELT_START = new BlockPos(2, 2, 1);
    private static final int STATION_Y = 4;
    private static final BlockPos FRYER = new BlockPos(4, 2, 10);

    private enum Kind { SPICES, ONION, SAUCE, PRESS }

    private record Station(Kind kind, Fluid sauce) {
        static Station of(Kind kind) {
            return new Station(kind, null);
        }

        static Station sauce(Fluid fluid) {
            return new Station(Kind.SAUCE, fluid);
        }
    }

    private record Line(Item paste, Item unfinished, Item raw, Item fried, int passes, int perStep, List<Station> stations,
                        HeatLevel heat, Fluid fat, int fatUsed) {
    }

    private TheFricadelleLineGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 3600)
    public static void exceptionalPasteBecomesTheFricadelleOnRealMachines(GameTestHelper helper) {
        run(helper, new Line(BSItems.EXCEPTIONAL_PASTE.get(), BSItems.INCOMPLETE_THE_FRICADELLE.get(), BSItems.RAW_THE_FRICADELLE.get(),
            BSItems.THE_FRICADELLE.get(), 3, 100,
            List.of(Station.of(Kind.SPICES), Station.sauce(BSFluids.MAYONNAISE.get().getSource()),
                Station.sauce(BSFluids.CURRY_KETCHUP.get().getSource()), Station.of(Kind.ONION), Station.of(Kind.PRESS)),
            HeatLevel.KINDLED, BSFluids.FRYING_OIL.get().getSource(), 25));
    }

    // Tier 3 (27/09/2026): beef tallow first, 250 mB servings, five passes, then a seething fryer in tallow.
    @GameTest(template = "empty_large", timeoutTicks = 6000)
    public static void absolutePasteBecomesTheUltimateFricadelleOnRealMachines(GameTestHelper helper) {
        run(helper, new Line(BSItems.ABSOLUTE_PASTE.get(), BSItems.INCOMPLETE_ULTIMATE_FRICADELLE.get(), BSItems.RAW_ULTIMATE_FRICADELLE.get(),
            BSItems.ULTIMATE_FRICADELLE.get(), 5, 250,
            List.of(Station.sauce(BSFluids.MELTED_BEEF_TALLOW.get().getSource()), Station.of(Kind.SPICES),
                Station.sauce(BSFluids.MAYONNAISE.get().getSource()), Station.sauce(BSFluids.CURRY_KETCHUP.get().getSource()),
                Station.of(Kind.ONION), Station.of(Kind.PRESS)),
            HeatLevel.SEETHING, BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 250));
    }

    private static void run(GameTestHelper helper, Line line) {
        Item onion = onion();
        BlockPos beltEnd = BELT_START.south(line.stations().size() + 2);
        build(helper, line, onion, beltEnd);
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        Set<String> reached = new LinkedHashSet<>();
        int[] passes = {0};
        int[] topUps = new int[line.stations().size()];

        // A belt takes items once its segments have ticked.
        helper.runAfterDelay(10, () -> insert(helper, BELT_START, new ItemStack(line.paste())));

        helper.onEachTick(() -> {
            for (int i = 0; i < line.stations().size(); i++) {
                Station station = line.stations().get(i);
                if (station.kind() == Kind.SAUCE) {
                    IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(stationPos(i)), null);
                    int missing = TANK - tank.getFluidInTank(0).getAmount();
                    if (missing > 0) {
                        topUps[i] += tank.fill(new FluidStack(station.sauce(), missing), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
            IItemHandler end = items(helper, beltEnd);
            ItemStack last = end == null ? ItemStack.EMPTY : end.getStackInSlot(0);
            if (last.is(line.unfinished()) && passes[0] >= line.passes()) {
                helper.fail("still unfinished after " + passes[0] + " passes: " + last.getComponentsPatch() + "; stations " + stock(helper, line));
            } else if (last.is(line.unfinished())) {
                // Back to the start: what a looping belt does in a player's factory.
                ItemStack moved = end.extractItem(0, 1, false);
                passes[0]++;
                reached.add("pass " + passes[0] + " done");
                insert(helper, BELT_START, moved);
            } else if (last.is(line.raw())) {
                reached.add("raw after " + (passes[0] + 1) + " passes");
                fryer.getItemCapability().insertItem(0, end.extractItem(0, 1, false), false);
            }
        });

        helper.succeedWhen(() -> {
            helper.assertTrue(fryer.getOutput().getStackInSlot(0).is(line.fried()), "not fried yet; stages: " + reached + "; fryer " + fryer.status());
            helper.assertValueEqual(passes[0] + 1, line.passes(), "passes along the line");
            for (int i = 0; i < line.stations().size(); i++) {
                Station station = line.stations().get(i);
                BlockPos pos = stationPos(i);
                switch (station.kind()) {
                    case SAUCE -> helper.assertValueEqual(topUps[i] + TANK - fluid(helper, pos), line.passes() * line.perStep(),
                        BuiltInRegistries.FLUID.getKey(station.sauce()) + " used");
                    case SPICES, ONION -> helper.assertValueEqual(held(helper, pos), STOCK - line.passes(), station.kind() + " used");
                    default -> {
                    }
                }
            }
            helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 1000 - line.fatUsed(), "fat for one fricadelle");
        });
    }

    private static BlockPos stationPos(int index) {
        return new BlockPos(2, STATION_Y, 2 + index);
    }

    private static Item onion() {
        var onions = BuiltInRegistries.ITEM.getTag(BSTags.C_ONIONS);
        return onions.isPresent() && onions.get().size() > 0 ? onions.get().get(0).value() : Items.BEETROOT;
    }

    private static void build(GameTestHelper helper, Line line, Item onion, BlockPos beltEnd) {
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(BELT_START), helper.absolutePos(beltEnd));
        motor(helper, BELT_START.west(), Direction.EAST);
        for (int i = 0; i < line.stations().size(); i++) {
            Station station = line.stations().get(i);
            BlockPos pos = stationPos(i);
            switch (station.kind()) {
                case SPICES -> deployer(helper, pos, BSItems.BELGIAN_SPICES.asStack(STOCK));
                case ONION -> deployer(helper, pos, new ItemStack(onion, STOCK));
                case SAUCE -> spout(helper, pos, station.sauce());
                case PRESS -> {
                    helper.setBlock(pos, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
                    motor(helper, pos.west(), Direction.EAST);
                }
            }
        }
        helper.setBlock(FRYER.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, line.heat()));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(FRYER.below());
        burner.isCreative = true;
        helper.setBlock(FRYER, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = helper.getBlockEntity(FRYER);
        fryer.getTank().getCapability().fill(new FluidStack(line.fat(), 1000), IFluidHandler.FluidAction.EXECUTE);
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
        helper.assertValueEqual(tank.fill(new FluidStack(sauce, TANK), IFluidHandler.FluidAction.EXECUTE), TANK, "sauce in the spout at " + pos);
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

    private static String stock(GameTestHelper helper, Line line) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < line.stations().size(); i++) {
            BlockPos pos = stationPos(i);
            Kind kind = line.stations().get(i).kind();
            text.append(kind).append('=').append(kind == Kind.SAUCE ? fluid(helper, pos) + " mB" : kind == Kind.PRESS ? "-" : held(helper, pos)).append(' ');
        }
        return text.toString().trim();
    }
}
