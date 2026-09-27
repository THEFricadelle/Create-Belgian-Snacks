/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderContents;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlock;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSDataComponents;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Supreme Grinder (docs/05, checklist in docs/10). The grinder sits at (1,1,1) under a creative
 * motor. Tests that change the server config run in their own batch, after the others, so they
 * never race a test that reads the same setting.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrinderGameTests {
    private static final String TEMPLATE = "empty";
    private static final String CONFIG_BATCH = "grinder_config";
    private static final BlockPos GRINDER = new BlockPos(1, 1, 1);
    private static final BlockPos MOTOR = GRINDER.above();

    private GrinderGameTests() {
    }

    // ------------------------------------------------------------------ kinetics

    @GameTest(template = TEMPLATE)
    public static void stressImpactIsTheConfiguredOne(GameTestHelper helper) {
        helper.assertValueEqual(BlockStressValues.getImpact(BSBlocks.SUPREME_GRINDER.get()), 8.0, "stress impact per RPM (D11)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void refusesEverythingWithoutRotation(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = grinder(helper, 0);
        helper.runAfterDelay(5, () -> {
            helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE), false).getCount(), 1, "a stopped grinder took an apple");
            helper.assertTrue(grinder.getConsumed().isEmpty(), "nothing recorded");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void refusesBelowTheMinimumSpeed(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = grinder(helper, 32);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(Math.abs(grinder.getSpeed()), 32f, "speed"))
            .thenExecute(() -> helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE), false).getCount(), 1,
                "took an apple at 32 RPM"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------ acceptance

    @GameTest(template = TEMPLATE)
    public static void takesEachNewFoodOnceAndNothingElse(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough(), "not up to speed"))
            .thenExecute(() -> {
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE, 3), false).getCount(), 2, "one apple of three taken");
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE, 3), false).getCount(), 3, "duplicate apple refused");
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.STONE), false).getCount(), 1, "stone refused");
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.CAKE), false).getCount(), 0, "cake taken (extra tag)");
                helper.assertValueEqual(grinder.insert(BSItems.FRICADELLE.asStack(), false).getCount(), 1, "fricadelle refused (blacklist)");
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), false).getCount(), 0,
                    "enchanted golden apple taken (D8)");
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.BREAD), true).getCount(), 0, "simulated bread accepted");
                helper.assertFalse(grinder.getConsumed().contains(id("minecraft:bread")), "a simulation recorded the bread");
                helper.assertValueEqual(grinder.getConsumed(),
                    Set.of(id("minecraft:apple"), id("minecraft:cake"), id("minecraft:enchanted_golden_apple")), "collection");
            })
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), 3, "counted foods"))
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPlayerFeedsOneFoodPerClick(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(GRINDER);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.NORTH, absolute, false);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough(), "not up to speed"))
            .thenExecute(() -> {
                ItemStack apples = new ItemStack(Items.APPLE, 5);
                player.setItemInHand(InteractionHand.MAIN_HAND, apples);
                level.getBlockState(absolute).useItemOn(apples, level, player, InteractionHand.MAIN_HAND, hit);
                helper.assertValueEqual(player.getMainHandItem().getCount(), 4, "one apple left the hand");
                level.getBlockState(absolute).useItemOn(apples, level, player, InteractionHand.MAIN_HAND, hit);
                helper.assertValueEqual(player.getMainHandItem().getCount(), 4, "the duplicate stays in the hand");

                grinder.getOutput().setStackInSlot(0, BSItems.EXCEPTIONAL_PASTE.asStack());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                level.getBlockState(absolute).useWithoutItem(level, player, hit);
                helper.assertValueEqual(player.getInventory().countItem(BSItems.EXCEPTIONAL_PASTE.get()), 1, "empty hand takes the paste");
            })
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBeltFeedsOneOfEachAndWaitsOnTheRest(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        DirectBeltInputBehaviour belt = BlockEntityBehaviour.get(grinder, DirectBeltInputBehaviour.TYPE);
        helper.assertTrue(belt != null, "grinder has no belt input");
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough(), "not up to speed"))
            .thenExecute(() -> {
                ItemStack left = belt.handleInsertion(new TransportedItemStack(new ItemStack(Items.APPLE, 64)), Direction.NORTH, false);
                helper.assertValueEqual(left.getCount(), 63, "one apple from the belt");
                left = belt.handleInsertion(new TransportedItemStack(new ItemStack(Items.APPLE, 63)), Direction.NORTH, false);
                helper.assertValueEqual(left.getCount(), 63, "the rest of the apples wait on the belt");
                left = belt.handleInsertion(new TransportedItemStack(new ItemStack(Items.BREAD, 64)), Direction.NORTH, false);
                helper.assertValueEqual(left.getCount(), 63, "one bread from the belt");
            })
            .thenSucceed();
    }

    // ------------------------------------------------------------------ goals

    @GameTest(template = TEMPLATE)
    public static void theModeMakesAnExceptionalPasteAtItsGoal(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        List<ResourceLocation> index = FoodIndex.server().ids();
        int goal = GrinderProgress.goal(BSConfig.grinderTheFricadelleRatio(), index.size());
        // Everything but the apple, one short of the goal.
        List<ResourceLocation> start = new ArrayList<>(index);
        start.remove(id("minecraft:apple"));
        grinder.setConsumed(start.subList(0, goal - 1));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough() && grinder.getCount() == goal - 1, "not ready"))
            .thenExecute(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).isEmpty(), "paste one food early"))
            .thenExecute(() -> grinder.insert(new ItemStack(Items.APPLE), false))
            .thenWaitUntil(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).is(BSItems.EXCEPTIONAL_PASTE.get()), "no paste"))
            .thenExecute(() -> {
                helper.assertTrue(grinder.getConsumed().isEmpty(), "the collection was not emptied (D9)");
                helper.assertValueEqual(grinder.insert(new ItemStack(Items.BREAD), false).getCount(), 1, "took food with the paste waiting");
            })
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void ultimateModeNeedsEveryFood(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setMode(GrinderMode.ULTIMATE);
        List<ResourceLocation> index = FoodIndex.server().ids();
        grinder.setConsumed(index.subList(0, index.size() - 1));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getGoal(), index.size(), "ultimate goal is the whole index"))
            .thenIdle(3)
            .thenExecute(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).isEmpty(), "paste one food short of 100 %"))
            .thenExecute(() -> command(helper, "belgiansnacks grinder fill " + coords(helper) + " 100"))
            .thenWaitUntil(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).is(BSItems.ABSOLUTE_PASTE.get()), "no absolute paste"))
            .thenExecute(() -> helper.assertTrue(grinder.getConsumed().isEmpty(), "collection kept after the paste"))
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void switchingToTheModeAboveItsGoalUsesUpEverything(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setMode(GrinderMode.ULTIMATE);
        List<ResourceLocation> index = FoodIndex.server().ids();
        grinder.setConsumed(index.subList(0, index.size() / 2));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), index.size() / 2, "half collected"))
            .thenExecute(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).isEmpty(), "paste at half in ultimate mode"))
            .thenExecute(() -> grinder.setMode(GrinderMode.THE_))
            .thenWaitUntil(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).is(BSItems.EXCEPTIONAL_PASTE.get()), "no paste"))
            .thenExecute(() -> helper.assertTrue(grinder.getConsumed().isEmpty(), "the excess survived (D9: a failed attempt)"))
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFullOutputStopsInputAndAHopperEmptiesIt(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.getOutput().setStackInSlot(0, BSItems.EXCEPTIONAL_PASTE.asStack());
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough(), "not up to speed"))
            .thenExecute(() -> helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE), false).getCount(), 1,
                "took food with the paste waiting"))
            .thenExecute(() -> helper.setBlock(GRINDER.below(), Blocks.HOPPER))
            .thenWaitUntil(() -> {
                HopperBlockEntity hopper = helper.getBlockEntity(GRINDER.below());
                helper.assertTrue(hopper.getItem(0).is(BSItems.EXCEPTIONAL_PASTE.get()), "the hopper did not pull the paste");
            })
            .thenExecute(() -> helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE), false).getCount(), 0,
                "food refused once the paste is out"))
            .thenSucceed();
    }

    // A goal reached while a paste waits (a fill, a mode switch) must not overwrite it: the next paste
    // comes once the first is taken.
    @GameTest(template = TEMPLATE)
    public static void aWaitingPasteIsNeverOverwritten(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.getOutput().setStackInSlot(0, BSItems.ABSOLUTE_PASTE.asStack());
        List<ResourceLocation> index = FoodIndex.server().ids();
        grinder.setConsumed(index);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), index.size(), "counted"))
            .thenIdle(5)
            .thenExecute(() -> {
                helper.assertTrue(grinder.getOutput().getStackInSlot(0).is(BSItems.ABSOLUTE_PASTE.get()), "the waiting paste was replaced");
                helper.assertValueEqual(grinder.getConsumed().size(), index.size(), "collection spent without room for the paste");
                grinder.getOutput().extractItem(0, 1, false);
            })
            .thenWaitUntil(() -> helper.assertTrue(grinder.getOutput().getStackInSlot(0).is(BSItems.EXCEPTIONAL_PASTE.get()),
                "no paste once the output was free"))
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void gaugeAndComparatorFollowTheGoal(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setMode(GrinderMode.ULTIMATE);
        List<ResourceLocation> index = FoodIndex.server().ids();
        // Half rounded up: exactly two quarters of the gauge for any index size (the one with FD is odd).
        int half = (index.size() + 1) / 2;
        grinder.setConsumed(index.subList(0, half));
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(GRINDER);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(level.getBlockState(absolute).getValue(SupremeGrinderBlock.FILL), 2, "gauge"))
            .thenExecute(() -> helper.assertValueEqual(level.getBlockState(absolute).getAnalogOutputSignal(level, absolute),
                Math.round(15f * half / index.size()), "comparator"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------ persistence

    @GameTest(template = TEMPLATE)
    public static void breakingKeepsTheCollectionAndTheMode(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        List<ResourceLocation> kept = List.of(id("minecraft:apple"), id("minecraft:bread"), id("ghostmod:food"));
        grinder.setMode(GrinderMode.ULTIMATE);
        grinder.setConsumed(kept);
        helper.getLevel().destroyBlock(helper.absolutePos(GRINDER), true);

        List<ItemEntity> drops = helper.findEntities(EntityType.ITEM, 1, 1, 1, 3);
        ItemStack grinderItem = drops.stream().map(ItemEntity::getItem).filter(s -> s.is(BSBlocks.SUPREME_GRINDER.asItem())).findFirst()
            .orElseThrow(() -> new AssertionError("no grinder item dropped"));
        GrinderContents contents = grinderItem.get(BSDataComponents.GRINDER_CONTENTS.get());
        helper.assertTrue(contents != null && contents.consumed().equals(kept), "the dropped grinder lost its collection: " + contents);

        BlockPos again = new BlockPos(3, 1, 1);
        helper.setBlock(again, BSBlocks.SUPREME_GRINDER.getDefaultState());
        SupremeGrinderBlockEntity placed = helper.getBlockEntity(again);
        placed.applyComponentsFromItemStack(grinderItem);
        helper.assertValueEqual(placed.getConsumed(), Set.copyOf(kept), "collection restored on placement");
        helper.assertTrue(placed.getMode() == GrinderMode.ULTIMATE, "mode restored on placement");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void unknownIdsSurviveASaveButDoNotCount(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setConsumed(List.of(id("minecraft:apple"), id("ghostmod:food")));
        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = grinder.saveWithoutMetadata(registries);
        grinder.setConsumed(List.of());
        grinder.loadWithComponents(saved, registries);
        helper.assertValueEqual(grinder.getConsumed(), Set.of(id("minecraft:apple"), id("ghostmod:food")), "loaded collection");
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), 1, "only the apple counts"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------ commands and goggles

    @GameTest(template = TEMPLATE)
    public static void fillAndClearCommands(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setMode(GrinderMode.ULTIMATE);
        int size = FoodIndex.server().size();
        command(helper, "belgiansnacks grinder fill " + coords(helper) + " 50");
        helper.assertValueEqual(grinder.getConsumed().size(), (int) Math.round(size * 0.5), "filled to 50 %");
        helper.assertTrue(grinder.getConsumed().contains(FoodIndex.server().ids().get(0)), "fill starts at the first food");
        command(helper, "belgiansnacks grinder clear " + coords(helper));
        helper.assertTrue(grinder.getConsumed().isEmpty(), "cleared");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    // The goggle lines themselves are client code (Create measures them with the font): ClientSmokeTest checks them.
    public static void clientFiguresFollowTheCollection(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setConsumed(List.of(id("minecraft:apple")));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), 1, "counted"))
            .thenExecute(() -> {
                int goal = GrinderProgress.goal(BSConfig.grinderTheFricadelleRatio(), FoodIndex.server().size());
                helper.assertValueEqual(grinder.getGoal(), goal, "goal");
                helper.assertValueEqual(grinder.getTotal(), FoodIndex.server().size(), "total");
                helper.assertValueEqual(grinder.percent(), 100 / goal, "percent");
                helper.assertValueEqual(grinder.getSamples().size(), 5, "missing examples");
                helper.assertFalse(grinder.getSamples().contains(id("minecraft:apple")), "a collected food shown as missing");
            })
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void farmersDelightFoodsCountWhenLoaded(GameTestHelper helper) {
        if (!ModList.get().isLoaded("farmersdelight")) {
            helper.succeed();
            return;
        }
        SupremeGrinderBlockEntity grinder = running(helper);
        ResourceLocation fd = FoodIndex.server().ids().stream().filter(i -> i.getNamespace().equals("farmersdelight")).findFirst()
            .orElseThrow(() -> new AssertionError("no Farmer's Delight food in the index"));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough(), "not up to speed"))
            .thenExecute(() -> helper.assertValueEqual(grinder.insert(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(fd)), false)
                .getCount(), 0, fd + " refused"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------ config (own batch)

    @GameTest(template = TEMPLATE, batch = CONFIG_BATCH)
    public static void duplicatesAreDestroyedWhenNotRejected(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(grinder.isFastEnough(), "not up to speed"))
            .thenExecute(() -> {
                ModConfigSpec.BooleanValue reject = config("GRINDER_REJECT_DUPLICATES");
                reject.set(false);
                try {
                    grinder.insert(new ItemStack(Items.APPLE), false);
                    helper.assertValueEqual(grinder.insert(new ItemStack(Items.APPLE, 2), false).getCount(), 1, "duplicate destroyed");
                    helper.assertValueEqual(grinder.getConsumed().size(), 1, "a destroyed duplicate counted");
                } finally {
                    reject.set(true);
                }
            })
            .thenSucceed();
    }

    // What a pack losing a food mod between two sessions looks like to a grinder: its foods leave the
    // index, the collection keeps them, the count drops, and comes back with the mod.
    @GameTest(template = TEMPLATE, batch = CONFIG_BATCH)
    public static void aFoodModLeavingAndComingBack(GameTestHelper helper) {
        SupremeGrinderBlockEntity grinder = running(helper);
        grinder.setConsumed(List.of(id("minecraft:apple"), id("minecraft:bread"), id("minecraft:cake")));
        var server = helper.getLevel().getServer();
        ModConfigSpec.ConfigValue<List<? extends String>> mods = config("GRINDER_BLACKLISTED_MODS");
        List<? extends String> before = mods.get();
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), 3, "counted"))
            .thenExecute(() -> {
                mods.set(List.of("minecraft"));
                FoodIndex.recompute(server);
            })
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), 0, "count without the mod"))
            .thenExecute(() -> {
                helper.assertValueEqual(grinder.getConsumed().size(), 3, "collection kept while the mod is gone");
                helper.assertTrue(grinder.getOutput().getStackInSlot(0).isEmpty(), "an empty index produced a paste");
                mods.set(before);
                FoodIndex.recompute(server);
            })
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), 3, "count with the mod back"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------ helpers

    private static SupremeGrinderBlockEntity running(GameTestHelper helper) {
        return grinder(helper, 64);
    }

    private static SupremeGrinderBlockEntity grinder(GameTestHelper helper, int speed) {
        helper.setBlock(GRINDER, BSBlocks.SUPREME_GRINDER.getDefaultState());
        if (speed != 0) {
            helper.setBlock(MOTOR, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN));
            CreativeMotorBlockEntity motor = helper.getBlockEntity(MOTOR);
            motor.generatedSpeed.setValue(speed);
        }
        return helper.getBlockEntity(GRINDER);
    }

    private static void command(GameTestHelper helper, String command) {
        var server = helper.getLevel().getServer();
        CommandSourceStack source = server.createCommandSourceStack().withLevel(helper.getLevel()).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(source, command);
    }

    private static String coords(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(GRINDER);
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    @SuppressWarnings("unchecked")
    private static <T extends ModConfigSpec.ConfigValue<?>> T config(String field) {
        try {
            var declared = BSConfig.class.getDeclaredField(field);
            declared.setAccessible(true);
            return (T) declared.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("no config field " + field, e);
        }
    }

    private static ResourceLocation id(String id) {
        return ResourceLocation.parse(id);
    }
}
