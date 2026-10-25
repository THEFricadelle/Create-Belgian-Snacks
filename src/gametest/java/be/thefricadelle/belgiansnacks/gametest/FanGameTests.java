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
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * v2 base ingredients processed by Create's fans (docs/13, section 5.7): blowing through water washes
 * the young Herve into Herve cheese and makes grain sprout; blowing through a lit campfire smokes the
 * sprouted grain into malt and bakes the pistolet. Each row: motor, fan, catalyst, depot.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FanGameTests {
    private static final int SPEED = 64;

    private FanGameTests() {
    }

    @GameTest(template = "empty_large", timeoutTicks = 600)
    public static void fansWashAndSmokeTheBaseIngredients(GameTestHelper helper) {
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState campfire = Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true);
        BlockPos cheese = row(helper, 1, water, BSItems.YOUNG_HERVE_CHEESE.asStack());
        BlockPos grain = row(helper, 3, water, new ItemStack(Items.WHEAT));
        BlockPos malt = row(helper, 5, campfire, BSItems.SPROUTED_GRAIN.asStack());
        BlockPos roll = row(helper, 7, campfire, BSItems.PISTOLET_DOUGH.asStack());
        helper.succeedWhen(() -> {
            expect(helper, cheese, BSItems.HERVE_CHEESE.get());
            expect(helper, grain, BSItems.SPROUTED_GRAIN.get());
            expect(helper, malt, BSItems.MALT.get());
            expect(helper, roll, BSItems.PISTOLET.get());
        });
    }

    // Along +x: a motor, the fan blowing east, the catalyst, then the depot holding the item.
    private static BlockPos row(GameTestHelper helper, int z, BlockState catalyst, ItemStack item) {
        BlockPos fan = new BlockPos(1, 2, z);
        helper.setBlock(fan.west(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
        CreativeMotorBlockEntity motor = helper.getBlockEntity(fan.west());
        motor.generatedSpeed.setValue(SPEED);
        helper.setBlock(fan, AllBlocks.ENCASED_FAN.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
        helper.setBlock(fan.east(), catalyst);
        BlockPos depot = fan.east(2);
        helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
        IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), null);
        helper.assertTrue(handler != null && handler.insertItem(0, item, false).isEmpty(), "the depot refused " + item);
        return depot;
    }

    private static void expect(GameTestHelper helper, BlockPos depot, Item result) {
        ItemStack held = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), null).getStackInSlot(0);
        helper.assertTrue(held.is(result), "depot at " + depot.toShortString() + " holds " + held + ", expected " + result);
    }
}
