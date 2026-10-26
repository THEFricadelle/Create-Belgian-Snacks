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
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.waffle.WaffleBakingRecipe;
import be.thefricadelle.belgiansnacks.content.waffle.WaffleIronBlock;
import be.thefricadelle.belgiansnacks.content.waffle.WaffleIronBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The waffle iron (docs/13, section 6.1), on a creative Blaze Burner, with the real waffle recipes. */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WaffleIronGameTests {
    private static final String TEMPLATE = "empty";
    private static final BlockPos IRON = new BlockPos(1, 1, 1);
    private static final BlockPos BURNER = IRON.below();

    private WaffleIronGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void wafflesBakeOnALitBurner(GameTestHelper helper) {
        for (String id : List.of("liege_waffle", "brussels_waffle", "lacquemant_wafer")) {
            var recipe = helper.getLevel().getRecipeManager().byKey(BelgianSnacks.asResource("waffle_baking/" + id))
                .orElseThrow(() -> new AssertionError("waffle_baking/" + id + " is not loaded")).value();
            helper.assertTrue(recipe instanceof WaffleBakingRecipe baking && baking.getRequiredHeat() == HeatCondition.HEATED, id + " heated");
        }
        var craft = helper.getLevel().getRecipeManager().byKey(BelgianSnacks.asResource("mechanical_crafting/waffle_iron"))
            .orElseThrow(() -> new AssertionError("mechanical_crafting/waffle_iron is not loaded")).value();
        helper.assertTrue(craft.getResultItem(helper.getLevel().registryAccess()).is(BSBlocks.WAFFLE_IRON.asItem()), "makes the waffle iron");
        helper.succeed();
    }

    // waffleIron.maxBatch is 4: four batters bake together, the fifth waits for the next batch.
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void fourBattersBakeTogether(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, HeatLevel.KINDLED);
        ItemStack left = iron.getItemCapability().insertItem(0, BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(6), false);
        helper.assertValueEqual(left.getCount(), 2, "the input holds one batch of 4");
        helper.onEachTick(() -> helper.assertTrue(iron.getPlates().getCount() == 0 || iron.getPlates().getCount() == 4,
            "a batch is all 4 or nothing"));
        helper.succeedWhen(() -> helper.assertValueEqual(count(iron, BSWaffles.BRUSSELS_WAFFLE.asStack()), 4, "baked waffles"));
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 800)
    public static void noHeatMeansNoStartAndLostHeatPauses(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, null);
        iron.getItemCapability().insertItem(0, BSWaffles.LIEGE_WAFFLE_DOUGH.asStack(2), false);
        helper.startSequence()
            .thenIdle(40)
            .thenExecute(() -> {
                helper.assertTrue(iron.getPlates().isEmpty(), "started without heat");
                helper.assertTrue(iron.status() == WaffleIronBlockEntity.Status.NO_HEAT, "status should say no heat, is " + iron.status());
                burner(helper, HeatLevel.KINDLED);
            })
            .thenWaitUntil(() -> helper.assertFalse(iron.getPlates().isEmpty(), "the batch did not start once heated"))
            .thenIdle(30)
            .thenExecute(() -> helper.setBlock(BURNER, Blocks.AIR))
            .thenIdle(240)
            .thenExecute(() -> {
                helper.assertValueEqual(iron.getPlates().getCount(), 2, "the paused batch stays on the plates");
                helper.assertValueEqual(count(iron, BSWaffles.LIEGE_WAFFLE.asStack()), 0, "baked while paused");
                helper.assertTrue(iron.getProgress() > 0 && iron.getProgress() < 1, "progress kept while paused");
                burner(helper, HeatLevel.KINDLED);
            })
            .thenWaitUntil(() -> helper.assertValueEqual(count(iron, BSWaffles.LIEGE_WAFFLE.asStack()), 2, "finished after reheating"))
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void onlyBattersGetIn(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, null);
        helper.assertValueEqual(iron.getItemCapability().insertItem(0, new ItemStack(Items.DIRT), false).getCount(), 1, "dirt accepted");
        helper.assertValueEqual(iron.getItemCapability().insertItem(0, BSWaffles.BRUSSELS_WAFFLE.asStack(), false).getCount(), 1,
            "a baked waffle accepted again");
        helper.assertValueEqual(iron.getItemCapability().insertItem(1, BSWaffles.LACQUEMANT_BATTER.asStack(), false).getCount(), 1,
            "output slot accepted an insertion");
        helper.assertTrue(iron.getItemCapability().insertItem(0, BSWaffles.LACQUEMANT_BATTER.asStack(), false).isEmpty(), "batter refused");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aHopperBelowTakesOnlyTheOutput(GameTestHelper helper) {
        helper.setBlock(BURNER, Blocks.HOPPER);
        WaffleIronBlockEntity iron = iron(helper, null);
        iron.getInput().setStackInSlot(0, BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(3));
        iron.getOutput().setStackInSlot(0, BSWaffles.BRUSSELS_WAFFLE.asStack(2));
        helper.runAfterDelay(60, () -> {
            HopperBlockEntity hopper = helper.getBlockEntity(BURNER);
            int pulled = 0;
            for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
                ItemStack stack = hopper.getItem(slot);
                helper.assertFalse(stack.is(BSWaffles.BRUSSELS_WAFFLE_BATTER.get()), "the hopper pulled unbaked batter");
                if (stack.is(BSWaffles.BRUSSELS_WAFFLE.get())) {
                    pulled += stack.getCount();
                }
            }
            helper.assertValueEqual(pulled, 2, "waffles pulled by the hopper");
            helper.assertValueEqual(iron.getInput().getStackInSlot(0).getCount(), 3, "input left in place");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void comparatorReadsTheOutput(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, null);
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(IRON);
        helper.assertValueEqual(level.getBlockState(absolute).getAnalogOutputSignal(level, absolute), 0, "empty output");
        iron.getOutput().setStackInSlot(0, BSWaffles.LIEGE_WAFFLE.asStack(64));
        helper.assertTrue(level.getBlockState(absolute).getAnalogOutputSignal(level, absolute) > 0, "full output gives a signal");
        helper.succeed();
    }

    // Broken mid-batch: the batters on the plates, the waiting ones and the waffles all drop.
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void breakingDropsEverything(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, HeatLevel.KINDLED);
        iron.getItemCapability().insertItem(0, BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(4), false);
        iron.getOutput().setStackInSlot(0, BSWaffles.BRUSSELS_WAFFLE.asStack(2));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(iron.getPlates().getCount(), 4, "the batch is on the plates"))
            .thenExecute(() -> {
                iron.getInput().setStackInSlot(0, BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(1));
                helper.getLevel().destroyBlock(helper.absolutePos(IRON), true);
                List<ItemEntity> drops = helper.findEntities(EntityType.ITEM, 1, 1, 1, 3);
                int batter = drops.stream().map(ItemEntity::getItem).filter(s -> s.is(BSWaffles.BRUSSELS_WAFFLE_BATTER.get()))
                    .mapToInt(ItemStack::getCount).sum();
                int waffles = drops.stream().map(ItemEntity::getItem).filter(s -> s.is(BSWaffles.BRUSSELS_WAFFLE.get()))
                    .mapToInt(ItemStack::getCount).sum();
                helper.assertValueEqual(batter, 5, "batters on the plates and waiting");
                helper.assertValueEqual(waffles, 2, "waffles in the output");
                helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(BSBlocks.WAFFLE_IRON.asItem())), "the waffle iron itself drops");
            })
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBeltDropsItsBattersIn(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, null);
        DirectBeltInputBehaviour belt = BlockEntityBehaviour.get(iron, DirectBeltInputBehaviour.TYPE);
        helper.assertTrue(belt != null, "waffle iron has no belt input");
        ItemStack left = belt.handleInsertion(new TransportedItemStack(BSWaffles.LIEGE_WAFFLE_DOUGH.asStack(3)), Direction.NORTH, false);
        helper.assertTrue(left.isEmpty(), "belt insertion refused");
        helper.assertValueEqual(iron.getInput().getStackInSlot(0).getCount(), 3, "batters from the belt");
        ItemStack refused = belt.handleInsertion(new TransportedItemStack(new ItemStack(Items.DIRT)), Direction.NORTH, false);
        helper.assertValueEqual(refused.getCount(), 1, "belt pushed something unbakeable in");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPlayerLoadsAndEmptiesItByHand(GameTestHelper helper) {
        WaffleIronBlockEntity iron = iron(helper, null);
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(IRON);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack batter = BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(3);
        player.setItemInHand(InteractionHand.MAIN_HAND, batter);
        level.getBlockState(absolute).useItemOn(batter, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertValueEqual(iron.getInput().getStackInSlot(0).getCount(), 3, "batter put in by hand");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the hand is emptied");
        iron.getOutput().setStackInSlot(0, BSWaffles.BRUSSELS_WAFFLE.asStack(2));
        level.getBlockState(absolute).useWithoutItem(level, player, hit);
        helper.assertTrue(player.getInventory().countItem(BSWaffles.BRUSSELS_WAFFLE.get()) == 2, "an empty hand takes the waffles");
        helper.assertValueEqual(iron.getInput().getStackInSlot(0).getCount(), 3, "the input stays while there are waffles");
        // The waffles went into the hand; empty it, as the block only answers an empty hand.
        player.getInventory().clearContent();
        level.getBlockState(absolute).useWithoutItem(level, player, hit);
        helper.assertTrue(iron.getInput().getStackInSlot(0).isEmpty(), "then the unbaked batter comes back");
        helper.assertTrue(player.getInventory().countItem(BSWaffles.BRUSSELS_WAFFLE_BATTER.get()) == 3, "the batter is in the inventory");
        helper.succeed();
    }

    // The handle faces the player who places it; the hinge is behind.
    @GameTest(template = TEMPLATE)
    public static void theHandleFacesThePlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(IRON);
        for (Direction looking : Direction.Plane.HORIZONTAL) {
            player.setYRot(looking.toYRot());
            ItemStack stack = new ItemStack(BSBlocks.WAFFLE_IRON.get());
            BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
            var state = BSBlocks.WAFFLE_IRON.get().getStateForPlacement(context);
            helper.assertValueEqual(state.getValue(WaffleIronBlock.FACING), looking.getOpposite(), "placed looking " + looking);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    private static WaffleIronBlockEntity iron(GameTestHelper helper, HeatLevel heat) {
        if (heat != null) {
            burner(helper, heat);
        }
        helper.setBlock(IRON, BSBlocks.WAFFLE_IRON.getDefaultState());
        return helper.getBlockEntity(IRON);
    }

    // A creative burner never burns out, so the heat level holds for the whole test.
    private static void burner(GameTestHelper helper, HeatLevel heat) {
        helper.setBlock(BURNER, AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(BURNER);
        burner.isCreative = true;
    }

    private static int count(WaffleIronBlockEntity iron, ItemStack like) {
        int total = 0;
        for (int slot = 0; slot < iron.getOutput().getSlots(); slot++) {
            ItemStack stack = iron.getOutput().getStackInSlot(slot);
            if (ItemStack.isSameItem(stack, like)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
