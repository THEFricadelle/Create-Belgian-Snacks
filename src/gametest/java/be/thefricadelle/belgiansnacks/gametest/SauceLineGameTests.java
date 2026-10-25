/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * v2 sauced cones on real Create machines (docs/13, section 5.2): three belts, each carrying one cone
 * under a Spout of a different sauce to a depot. Each Spout uses 50 mB and gives the sauced cone.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SauceLineGameTests {
    private static final int SPEED = 64;
    private static final int TANK = 1000;
    private static final int SAUCE = 50;

    private record Row(int z, ItemStack cone, Fluid sauce, Item sauced) {
    }

    private SauceLineGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 600)
    public static void spoutsPutEachSauceOnItsCone(GameTestHelper helper) {
        List<Row> rows = List.of(
            new Row(1, BSItems.CONE_OF_FRIES.asStack(), BSFluids.ANDALOUSE.get().getSource(), BSItems.SAUCED_CONES.get("andalouse").get()),
            new Row(4, BSItems.CONE_OF_FRIES_BEEF_TALLOW.asStack(), BSFluids.SAMOURAI.get().getSource(),
                BSItems.SAUCED_TALLOW_CONES.get("samourai").get()),
            new Row(7, BSItems.CONE_OF_FRIES.asStack(), BSFluids.AMERICAINE.get().getSource(), BSItems.SAUCED_CONES.get("americaine").get()));
        for (Row row : rows) {
            build(helper, row);
        }
        // Belts take items once their segments have ticked.
        helper.runAfterDelay(10, () -> rows.forEach(row -> {
            IItemHandler start = items(helper, new BlockPos(0, 2, row.z()));
            helper.assertTrue(start != null && start.insertItem(0, row.cone().copy(), false).isEmpty(), "belt refused the cone");
        }));
        helper.succeedWhen(() -> {
            for (Row row : rows) {
                ItemStack done = items(helper, depot(row)).getStackInSlot(0);
                helper.assertTrue(done.is(row.sauced()), "depot of row " + row.z() + " holds " + done + "; belt " + belt(helper, row));
                IFluidHandler spout = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(spout(row)), null);
                helper.assertValueEqual(spout.getFluidInTank(0).getAmount(), TANK - SAUCE, "sauce used by the spout of row " + row.z());
            }
        });
    }

    // A belt along +x from x=0 to x=3, a spout two blocks above x=2, a depot at x=4 catching the cone.
    private static void build(GameTestHelper helper, Row row) {
        BlockPos start = new BlockPos(0, 2, row.z());
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(start), helper.absolutePos(new BlockPos(3, 2, row.z())));
        helper.setBlock(start.north(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.SOUTH));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(start.north());
        // Seen from the north, a positive speed runs this belt towards -x: negative sends the cone to the spout.
        motor.generatedSpeed.setValue(-SPEED);
        helper.setBlock(spout(row), AllBlocks.SPOUT.getDefaultState());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(spout(row)), null);
        helper.assertValueEqual(tank.fill(new FluidStack(row.sauce(), TANK), IFluidHandler.FluidAction.EXECUTE), TANK, "sauce in the spout");
        helper.setBlock(depot(row), AllBlocks.DEPOT.getDefaultState());
    }

    private static String belt(GameTestHelper helper, Row row) {
        StringBuilder text = new StringBuilder();
        for (int x = 0; x <= 3; x++) {
            IItemHandler segment = items(helper, new BlockPos(x, 2, row.z()));
            if (segment != null && !segment.getStackInSlot(0).isEmpty()) {
                text.append(x).append(':').append(segment.getStackInSlot(0)).append(' ');
            }
        }
        return text.toString();
    }

    private static BlockPos spout(Row row) {
        return new BlockPos(2, 4, row.z());
    }

    private static BlockPos depot(Row row) {
        return new BlockPos(4, 2, row.z());
    }

    private static IItemHandler items(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), null);
    }
}
