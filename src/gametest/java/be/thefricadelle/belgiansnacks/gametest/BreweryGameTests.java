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

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * v2 beers on a real mixer (docs/13, section 5.9): Create's mixer honours the processing time,
 * scaled by its speed, so the gueuze takes its long wait (about 1081 ticks at 64 rpm).
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BreweryGameTests {
    private static final BlockPos BASIN = new BlockPos(2, 1, 2);
    private static final BlockPos MIXER = BASIN.above(2);

    private BreweryGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 1600)
    public static void gueuzeTakesItsLongWaitOnARealMixer(GameTestHelper helper) {
        helper.setBlock(BASIN.below(), Blocks.STONE);
        helper.setBlock(BASIN, AllBlocks.BASIN.getDefaultState());
        // The mixer is a cog with no shaft: it turns through a meshing cogwheel beside it.
        helper.setBlock(MIXER, AllBlocks.MECHANICAL_MIXER.getDefaultState());
        helper.setBlock(MIXER.west(), AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Axis.Y));
        helper.setBlock(MIXER.west().above(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(MIXER.west().above());
        motor.generatedSpeed.setValue(64);
        var level = helper.getLevel();
        IFluidHandler tanks = level.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(BASIN), null);
        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(BASIN), null);
        helper.assertValueEqual(tanks.fill(new FluidStack(BSFluids.WORT.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE), 1000, "wort in");
        items.insertItem(0, new ItemStack(Items.WHEAT), false);
        int[] ticks = {0};
        helper.onEachTick(() -> ticks[0]++);
        helper.succeedWhen(() -> {
            boolean gueuze = false;
            for (int tank = 0; tank < tanks.getTanks(); tank++) {
                gueuze |= tanks.getFluidInTank(tank).getFluid() == BSFluids.GUEUZE.get().getSource();
            }
            helper.assertTrue(gueuze, "no gueuze yet after " + ticks[0] + " ticks");
            // Without the processing time it would be ready in about 46 ticks.
            helper.assertTrue(ticks[0] > 900, "gueuze ready after only " + ticks[0] + " ticks");
        });
    }
}
