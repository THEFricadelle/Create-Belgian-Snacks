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
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSDataComponents;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The fryer against its checklist (docs/10): a real fryer on a real Blaze Burner. The creative flag
 * keeps a burner lit without fuel. Recipes named test_* come from src/gametest/resources and never ship.
 * Run with {@code ./gradlew runGameTestServer}.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FryerGameTests {
    private static final String TEMPLATE = "empty";
    private static final BlockPos FRYER = new BlockPos(1, 1, 1);
    private static final BlockPos BURNER = FRYER.below();

    private FryerGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void fricadelleRecipeIsTheDocumentedOne(GameTestHelper helper) {
        FryingRecipe recipe = (FryingRecipe) helper.getLevel().getRecipeManager()
            .byKey(BelgianSnacks.asResource("frying/fricadelle")).orElseThrow().value();
        helper.assertTrue(recipe.getIngredients().get(0).test(BSItems.RAW_FRICADELLE.asStack()), "fries raw fricadelle");
        helper.assertTrue(recipe.getRollableResults().get(0).getStack().is(BSItems.FRICADELLE.get()), "into a fricadelle");
        helper.assertValueEqual(recipe.getFat().amount(), 10, "fat per item");
        helper.assertTrue(recipe.getFat().ingredient().test(new FluidStack(BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 1)), "any frying oil");
        helper.assertTrue(recipe.getRequiredHeat() == HeatCondition.HEATED, "heated");
        helper.assertValueEqual(recipe.getProcessingDuration(), 100, "duration");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theFryerIsCraftedOnAMechanicalCrafter(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(BelgianSnacks.asResource("mechanical_crafting/fryer"))
            .orElseThrow(() -> new AssertionError("mechanical_crafting/fryer is not loaded")).value();
        helper.assertTrue(recipe.getType() == com.simibubi.create.AllRecipeTypes.MECHANICAL_CRAFTING.getType(), "crafted on a mechanical crafter");
        helper.assertTrue(recipe.getResultItem(helper.getLevel().registryAccess()).is(BSBlocks.FRYER.asItem()), "makes the fryer");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aBatchOfSixteenFriesTogether(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        ItemStack left = fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(20), false);
        helper.assertValueEqual(left.getCount(), 4, "the input holds one batch of 16");
        helper.onEachTick(() -> helper.assertTrue(fryer.getBasket().getCount() == 0 || fryer.getBasket().getCount() == 16,
            "a batch is all 16 or nothing"));
        helper.succeedWhen(() -> {
            helper.assertValueEqual(count(fryer, BSItems.FRICADELLE.asStack()), 16, "fried fricadelles");
            helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 840, "fat left after 16 x 10 mB");
        });
    }

    // The real tier 3 recipe: superheated, beef tallow only, 600 ticks a piece.
    @GameTest(template = TEMPLATE, timeoutTicks = 1400)
    public static void oneAtATimeItemsFryAlone(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.SEETHING);
        fill(helper, fryer, BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 1000);
        helper.assertTrue(BSItems.RAW_ULTIMATE_FRICADELLE.asStack().is(BSTags.FRYER_ONE_AT_A_TIME), "tier 3 is tagged one_at_a_time");
        fryer.getItemCapability().insertItem(0, BSItems.RAW_ULTIMATE_FRICADELLE.asStack(2), false);
        helper.onEachTick(() -> helper.assertTrue(fryer.getBasket().getCount() <= 1, "more than one THE_FRICADELLE in the basket"));
        helper.succeedWhen(() -> helper.assertValueEqual(count(fryer, BSItems.ULTIMATE_FRICADELLE.asStack()), 2, "fried one by one"));
    }

    // v2 fries (V-D2): raw fries blanch on a lit burner; the second fry needs a seething one.
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void blanchingNeedsHeatAndTheSecondFryNeedsSeething(GameTestHelper helper) {
        FryerBlockEntity blanch = fryerAt(helper, new BlockPos(0, 1, 0), HeatLevel.KINDLED);
        FryerBlockEntity second = fryerAt(helper, new BlockPos(2, 1, 2), HeatLevel.KINDLED);
        fill(helper, blanch, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fill(helper, second, BSFluids.FRYING_OIL.get().getSource(), 1000);
        blanch.getItemCapability().insertItem(0, BSItems.RAW_FRIES.asStack(4), false);
        second.getItemCapability().insertItem(0, BSItems.BLANCHED_FRIES.asStack(4), false);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(count(blanch, BSItems.BLANCHED_FRIES.asStack()), 4, "blanched on a lit burner");
            helper.assertTrue(second.getBasket().isEmpty() && second.status() == FryerBlockEntity.Status.NO_HEAT,
                "the second fry ran on a lit burner, status " + second.status());
            helper.assertValueEqual(blanch.getTank().getPrimaryHandler().getFluidAmount(), 980, "fat left after 4 x 5 mB");
        });
    }

    // V-D4: both second-fry recipes accept beef tallow; the fryer takes the one whose fat is the most
    // specific, so tallow gives tallow fries and any other oil plain fries.
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void theFatPicksTheFries(GameTestHelper helper) {
        FryerBlockEntity oil = fryerAt(helper, new BlockPos(0, 1, 0), HeatLevel.SEETHING);
        FryerBlockEntity tallow = fryerAt(helper, new BlockPos(2, 1, 2), HeatLevel.SEETHING);
        fill(helper, oil, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fill(helper, tallow, BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 1000);
        oil.getItemCapability().insertItem(0, BSItems.BLANCHED_FRIES.asStack(4), false);
        tallow.getItemCapability().insertItem(0, BSItems.BLANCHED_FRIES.asStack(4), false);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(count(oil, BSItems.FRIES.asStack()), 4, "plain fries from oil");
            helper.assertValueEqual(count(tallow, BSItems.FRIES_BEEF_TALLOW.asStack()), 4, "tallow fries from tallow");
            helper.assertValueEqual(count(tallow, BSItems.FRIES.asStack()), 0, "plain fries from tallow");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void noHeatMeansNoStartAndLostHeatPauses(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(4), false);
        helper.startSequence()
            .thenIdle(40)
            .thenExecute(() -> {
                helper.assertTrue(fryer.getBasket().isEmpty(), "started without heat");
                helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 1000, "fat used without heat");
                helper.assertTrue(fryer.status() == FryerBlockEntity.Status.NO_HEAT, "status should say no heat, is " + fryer.status());
                burner(helper, HeatLevel.KINDLED);
            })
            .thenWaitUntil(() -> helper.assertFalse(fryer.getBasket().isEmpty(), "the batch did not start once heated"))
            .thenIdle(30)
            .thenExecute(() -> helper.setBlock(BURNER, Blocks.AIR))
            .thenIdle(120)
            .thenExecute(() -> {
                helper.assertValueEqual(fryer.getBasket().getCount(), 4, "the paused batch stays in the basket");
                helper.assertValueEqual(count(fryer, BSItems.FRICADELLE.asStack()), 0, "fried while paused");
                helper.assertTrue(fryer.getProgress() > 0 && fryer.getProgress() < 1, "progress kept while paused");
                burner(helper, HeatLevel.KINDLED);
            })
            .thenWaitUntil(() -> helper.assertValueEqual(count(fryer, BSItems.FRICADELLE.asStack()), 4, "finished after reheating"))
            .thenExecute(() -> helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 960, "fat paid once"))
            .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void superheatedRecipesNeedASeethingBurner(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getItemCapability().insertItem(0, new ItemStack(Items.POTATO), false);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(fryer.getBasket().isEmpty(), "superheated recipe started on a kindled burner");
            helper.assertTrue(fryer.status() == FryerBlockEntity.Status.NO_HEAT, "status should say no heat");
            burner(helper, HeatLevel.SEETHING);
        });
        helper.runAfterDelay(90, () -> {
            helper.assertValueEqual(count(fryer, new ItemStack(Items.BAKED_POTATO)), 1, "fried on a seething burner");
            helper.succeed();
        });
    }

    // Two recipes for carrots (frying/test_choice_*): superheated, which sorts first (its result is a
    // baked potato), and heated (a golden carrot). On a kindled burner the fryer must pick the one it
    // can run, as Create's basin does, not stall on the first one.
    @GameTest(template = TEMPLATE)
    public static void ofTwoRecipesTheOneTheHeatAllowsRuns(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getItemCapability().insertItem(0, new ItemStack(Items.CARROT), false);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(count(fryer, new ItemStack(Items.GOLDEN_CARROT)), 1, "fried with the heated recipe");
            helper.assertValueEqual(count(fryer, new ItemStack(Items.BAKED_POTATO)), 0, "the superheated recipe ran on a kindled burner");
        });
    }

    // THE_FRICADELLE fries in melted beef tallow only, even on a seething burner.
    @GameTest(template = TEMPLATE)
    public static void ultimateFricadelleRefusesOil(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.SEETHING);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getItemCapability().insertItem(0, BSItems.RAW_ULTIMATE_FRICADELLE.asStack(), false);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(fryer.getBasket().isEmpty(), "THE_FRICADELLE started frying in oil");
            helper.assertTrue(fryer.status() == FryerBlockEntity.Status.NO_FAT, "status: " + fryer.status());
            helper.succeed();
        });
    }

    // Create's rules for its own conditions and levels, whatever a mod does to testBlazeBurner.
    @GameTest(template = TEMPLATE)
    public static void heatRulesAreCreates(GameTestHelper helper) {
        for (HeatLevel heat : List.of(HeatLevel.NONE, HeatLevel.SMOULDERING, HeatLevel.FADING, HeatLevel.KINDLED, HeatLevel.SEETHING)) {
            boolean lit = heat != HeatLevel.NONE && heat != HeatLevel.SMOULDERING;
            helper.assertTrue(FryerBlockEntity.heatAllows(HeatCondition.NONE, heat), "no heat needed, on " + heat);
            helper.assertValueEqual(FryerBlockEntity.heatAllows(HeatCondition.HEATED, heat), lit, "heated, on " + heat);
            helper.assertValueEqual(FryerBlockEntity.heatAllows(HeatCondition.SUPERHEATED, heat), heat == HeatLevel.SEETHING,
                "superheated, on " + heat);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tallowOnlyRecipeRefusesOil(GameTestHelper helper) {
        FryerBlockEntity oil = fryerAt(helper, new BlockPos(0, 1, 0), HeatLevel.KINDLED);
        FryerBlockEntity tallow = fryerAt(helper, new BlockPos(2, 1, 2), HeatLevel.KINDLED);
        fill(helper, oil, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fill(helper, tallow, BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 1000);
        oil.getItemCapability().insertItem(0, new ItemStack(Items.COD), false);
        tallow.getItemCapability().insertItem(0, new ItemStack(Items.COD), false);
        helper.runAfterDelay(70, () -> {
            helper.assertTrue(oil.getBasket().isEmpty() && count(oil, new ItemStack(Items.COOKED_COD)) == 0, "fried in oil");
            helper.assertTrue(oil.status() == FryerBlockEntity.Status.NO_FAT, "status should say no fat, is " + oil.status());
            helper.assertValueEqual(count(tallow, new ItemStack(Items.COOKED_COD)), 1, "fried in melted tallow");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void onlyFryingFatsAndFryableItemsGetIn(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        IFluidHandler tank = fryer.getTank().getCapability();
        helper.assertValueEqual(tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE), 0, "water accepted");
        helper.assertValueEqual(tank.fill(new FluidStack(BSFluids.MAYONNAISE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE), 0,
            "mayonnaise accepted");
        helper.assertValueEqual(tank.fill(new FluidStack(BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE),
            1000, "melted tallow refused");
        ItemStack dirt = new ItemStack(Items.DIRT);
        helper.assertValueEqual(fryer.getItemCapability().insertItem(0, dirt, false).getCount(), 1, "dirt accepted");
        helper.assertValueEqual(fryer.getItemCapability().insertItem(1, BSItems.RAW_FRICADELLE.asStack(), false).getCount(), 1,
            "output slot accepted an insertion");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aHopperBelowTakesOnlyTheOutput(GameTestHelper helper) {
        // No burner: the hopper sits where it would be, and nothing cooks meanwhile.
        helper.setBlock(BURNER, Blocks.HOPPER);
        FryerBlockEntity fryer = fryer(helper, null);
        fryer.getInput().setStackInSlot(0, BSItems.RAW_FRICADELLE.asStack(3));
        fryer.getOutput().setStackInSlot(0, BSItems.FRICADELLE.asStack(2));
        helper.runAfterDelay(60, () -> {
            HopperBlockEntity hopper = helper.getBlockEntity(BURNER);
            int pulled = 0;
            for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
                ItemStack stack = hopper.getItem(slot);
                helper.assertFalse(stack.is(BSItems.RAW_FRICADELLE.get()), "the hopper pulled unfried input");
                if (stack.is(BSItems.FRICADELLE.get())) {
                    pulled += stack.getCount();
                }
            }
            helper.assertValueEqual(pulled, 2, "fried output pulled by the hopper");
            helper.assertValueEqual(fryer.getInput().getStackInSlot(0).getCount(), 3, "input left in place");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void comparatorReadsTheOutput(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(FRYER);
        helper.assertValueEqual(level.getBlockState(absolute).getAnalogOutputSignal(level, absolute), 0, "empty output");
        fryer.getOutput().setStackInSlot(0, BSItems.FRICADELLE.asStack(64));
        helper.assertTrue(level.getBlockState(absolute).getAnalogOutputSignal(level, absolute) > 0, "full output gives a signal");
        helper.succeed();
    }

    // A first bucket already fills the vat deep; a full tank reaches just under the rim.
    @GameTest(template = TEMPLATE)
    public static void theFatSurfaceStartsDeepAndRisesToTheRim(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        helper.assertValueEqual(fryer.fatSurface(), 4 / 16f, "empty: the vat floor");
        int capacity = fryer.getTank().getPrimaryHandler().getCapacity();
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        helper.assertTrue(Math.abs(fryer.fatSurface() - (9 / 16f + 6 / 16f * 1000 / capacity)) < 1.0E-6f, "one bucket: " + fryer.fatSurface());
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), capacity - 1000);
        helper.assertValueEqual(fryer.fatSurface(), 15 / 16f, "full");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingKeepsTheFatAndDropsTheItems(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 500);
        fryer.getInput().setStackInSlot(0, BSItems.RAW_FRICADELLE.asStack(3));
        helper.getLevel().destroyBlock(helper.absolutePos(FRYER), true);

        List<ItemEntity> drops = helper.findEntities(EntityType.ITEM, 1, 1, 1, 3);
        ItemStack fryerItem = drops.stream().map(ItemEntity::getItem).filter(s -> s.is(BSBlocks.FRYER.asItem())).findFirst()
            .orElseThrow(() -> new AssertionError("no fryer item dropped"));
        int raw = drops.stream().map(ItemEntity::getItem).filter(s -> s.is(BSItems.RAW_FRICADELLE.get())).mapToInt(ItemStack::getCount).sum();
        helper.assertValueEqual(raw, 3, "input items dropped");
        var fat = fryerItem.get(BSDataComponents.FRYER_FLUID.get());
        helper.assertTrue(fat != null && fat.copy().getAmount() == 500, "the dropped fryer carries its 500 mB");

        // Placing it again, as BlockItem does, restores the fat.
        BlockPos again = new BlockPos(2, 1, 2);
        helper.setBlock(again, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity placed = helper.getBlockEntity(again);
        placed.applyComponentsFromItemStack(fryerItem);
        helper.assertValueEqual(placed.getTank().getPrimaryHandler().getFluidAmount(), 500, "fat restored on placement");
        helper.succeed();
    }

    // Every constant the running game has, including any a mod such as Create Heat JS adds.
    @GameTest(template = TEMPLATE)
    public static void everyHeatLevelHasAStatusLine(GameTestHelper helper) {
        for (HeatLevel heat : HeatLevel.values()) {
            helper.assertTrue(FryerBlockEntity.heatKey(heat).startsWith(BelgianSnacks.MOD_ID + ".fryer.heat."), "no line for " + heat);
        }
        helper.assertValueEqual(FryerBlockEntity.heatKey(HeatLevel.NONE), BelgianSnacks.MOD_ID + ".fryer.heat.none", "none");
        helper.assertValueEqual(FryerBlockEntity.heatKey(HeatLevel.SMOULDERING), BelgianSnacks.MOD_ID + ".fryer.heat.none", "smouldering");
        helper.assertValueEqual(FryerBlockEntity.heatKey(HeatLevel.FADING), BelgianSnacks.MOD_ID + ".fryer.heat.heated", "fading");
        helper.assertValueEqual(FryerBlockEntity.heatKey(HeatLevel.KINDLED), BelgianSnacks.MOD_ID + ".fryer.heat.heated", "kindled");
        helper.assertValueEqual(FryerBlockEntity.heatKey(HeatLevel.SEETHING), BelgianSnacks.MOD_ID + ".fryer.heat.superheated", "seething");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBeltDropsItsItemsIn(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        DirectBeltInputBehaviour belt = BlockEntityBehaviour.get(fryer, DirectBeltInputBehaviour.TYPE);
        helper.assertTrue(belt != null, "fryer has no belt input");
        ItemStack left = belt.handleInsertion(new TransportedItemStack(BSItems.RAW_FRICADELLE.asStack(5)), Direction.NORTH, false);
        helper.assertTrue(left.isEmpty(), "belt insertion refused");
        helper.assertValueEqual(fryer.getInput().getStackInSlot(0).getCount(), 5, "items from the belt");
        ItemStack refused = belt.handleInsertion(new TransportedItemStack(new ItemStack(Items.DIRT)), Direction.NORTH, false);
        helper.assertValueEqual(refused.getCount(), 1, "belt pushed an unfryable item in");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPlayerFillsLoadsAndEmptiesItByHand(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(FRYER);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        ItemStack bucket = new ItemStack(BSFluids.FRYING_OIL.getBucket().orElseThrow());
        player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
        level.getBlockState(absolute).useItemOn(bucket, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 1000, "oil poured from the bucket");
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "the player keeps an empty bucket");

        ItemStack raw = BSItems.RAW_FRICADELLE.asStack(5);
        player.setItemInHand(InteractionHand.MAIN_HAND, raw);
        level.getBlockState(absolute).useItemOn(raw, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertValueEqual(fryer.getInput().getStackInSlot(0).getCount(), 5, "items put in by hand");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the hand is emptied");

        fryer.getOutput().setStackInSlot(0, BSItems.FRICADELLE.asStack(2));
        level.getBlockState(absolute).useWithoutItem(level, player, hit);
        helper.assertTrue(player.getInventory().countItem(BSItems.FRICADELLE.get()) == 2, "an empty hand takes the fried output");
        helper.assertValueEqual(fryer.getInput().getStackInSlot(0).getCount(), 5, "the input stays while there is output");
        helper.succeed();
    }

    // ------------------------------------------------------------------ v2 used oil (section 6.2)

    // A batch of 16 at 10 mB each: 160 mB of fat paid, half of it back as used oil once the batch ends.
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aBatchGivesBackHalfItsFatAsUsedOil(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(16), false);
        helper.onEachTick(() -> {
            if (!fryer.getBasket().isEmpty()) {
                helper.assertValueEqual(fryer.getUsedOil().getPrimaryHandler().getFluidAmount(), 0, "used oil before the batch ends");
            }
        });
        helper.succeedWhen(() -> {
            helper.assertValueEqual(count(fryer, BSItems.FRICADELLE.asStack()), 16, "fried");
            FluidStack used = fryer.getUsedOil().getPrimaryHandler().getFluid();
            helper.assertTrue(used.getFluid().isSame(BSFluids.USED_FRYING_OIL.get()), "used oil is used frying oil, is " + used);
            helper.assertValueEqual(used.getAmount(), 80, "used oil after 16 x 10 mB");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aFullUsedOilTankStopsTheFryer(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        var used = fryer.getUsedOil().getPrimaryHandler();
        used.setFluid(new FluidStack(BSFluids.USED_FRYING_OIL.get().getSource(), used.getCapacity() - 40));
        fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(16), false);
        helper.startSequence()
            .thenIdle(30)
            .thenExecute(() -> {
                helper.assertTrue(fryer.getBasket().isEmpty(), "started with no room for its used oil");
                helper.assertTrue(fryer.status() == FryerBlockEntity.Status.USED_OIL_FULL, "status " + fryer.status());
                FluidStack drained = fryer.getFluidCapability().drain(500, IFluidHandler.FluidAction.EXECUTE);
                helper.assertTrue(drained.getFluid().isSame(BSFluids.USED_FRYING_OIL.get()) && drained.getAmount() == 500, "a pipe drained " + drained);
            })
            .thenWaitUntil(() -> helper.assertValueEqual(count(fryer, BSItems.FRICADELLE.asStack()), 16, "fried once drained"))
            .thenSucceed();
    }

    // Pipes and buckets: fat goes in, used oil comes out, nothing else either way.
    @GameTest(template = TEMPLATE)
    public static void pipesPourFatAndTakeUsedOil(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        IFluidHandler pipe = fryer.getFluidCapability();
        helper.assertValueEqual(pipe.getTanks(), 2, "fat and used oil tanks");
        helper.assertValueEqual(pipe.fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE), 1000, "oil in");
        helper.assertValueEqual(pipe.fill(new FluidStack(BSFluids.USED_FRYING_OIL.get().getSource(), 100), IFluidHandler.FluidAction.EXECUTE), 0,
            "used oil poured back in");
        helper.assertValueEqual(pipe.fill(new FluidStack(Fluids.WATER, 100), IFluidHandler.FluidAction.EXECUTE), 0, "water poured in");
        helper.assertTrue(pipe.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "the fat came out with no used oil to take");
        helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 1000, "fat kept");
        fryer.getUsedOil().getPrimaryHandler().setFluid(new FluidStack(BSFluids.USED_FRYING_OIL.get().getSource(), 300));
        FluidStack out = pipe.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(out.getFluid().isSame(BSFluids.USED_FRYING_OIL.get()) && out.getAmount() == 300, "drained " + out);
        helper.assertValueEqual(fryer.getTank().getPrimaryHandler().getFluidAmount(), 1000, "fat still kept");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingKeepsTheUsedOilToo(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, null);
        fryer.getUsedOil().getPrimaryHandler().setFluid(new FluidStack(BSFluids.USED_FRYING_OIL.get().getSource(), 400));
        helper.getLevel().destroyBlock(helper.absolutePos(FRYER), true);
        ItemStack item = helper.findEntities(EntityType.ITEM, 1, 1, 1, 3).stream().map(ItemEntity::getItem)
            .filter(s -> s.is(BSBlocks.FRYER.asItem())).findFirst().orElseThrow(() -> new AssertionError("no fryer item dropped"));
        BlockPos again = new BlockPos(2, 1, 2);
        helper.setBlock(again, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity placed = helper.getBlockEntity(again);
        placed.applyComponentsFromItemStack(item);
        helper.assertValueEqual(placed.getUsedOil().getPrimaryHandler().getFluidAmount(), 400, "used oil restored on placement");
        helper.succeed();
    }

    // A fryer saved by v1 has no used oil tank in its data: it loads with an empty one and fries.
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void aV1FryerLoadsWithAnEmptyUsedOilTank(GameTestHelper helper) {
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        var registries = helper.getLevel().registryAccess();
        net.minecraft.nbt.CompoundTag saved = fryer.saveWithFullMetadata(registries);
        saved.remove("OutputTanks");
        saved.remove("FatPaid");
        // Loaded into a fresh block entity, as when the chunk loads.
        FryerBlockEntity loaded = fryerAt(helper, new BlockPos(2, 1, 2), HeatLevel.KINDLED);
        loaded.loadWithComponents(saved, registries);
        helper.assertValueEqual(loaded.getUsedOil().getPrimaryHandler().getFluidAmount(), 0, "used oil of a v1 fryer");
        helper.assertValueEqual(loaded.getTank().getPrimaryHandler().getFluidAmount(), 1000, "fat of a v1 fryer");
        loaded.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(2), false);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(count(loaded, BSItems.FRICADELLE.asStack()), 2, "fried");
            helper.assertValueEqual(loaded.getUsedOil().getPrimaryHandler().getFluidAmount(), 10, "then makes used oil");
        });
    }

    // fryer.usedOilRatio = 0 is the v1 fryer: no used oil, and pipes drain the fat. Its own batch: the
    // config is shared with every test running at the same time.
    @GameTest(template = TEMPLATE, timeoutTicks = 400, batch = "fryer_config_ratio")
    public static void ratioZeroIsTheV1Fryer(GameTestHelper helper) {
        net.neoforged.neoforge.common.ModConfigSpec.DoubleValue ratio = usedOilRatio();
        double before = ratio.get();
        // Saved too: a test that saved the config file before this one makes NeoForge reload it from
        // disk a moment later, which would put the file's ratio back in the middle of this test.
        ratio.set(0.0);
        ratio.save();
        FryerBlockEntity fryer = fryer(helper, HeatLevel.KINDLED);
        fill(helper, fryer, BSFluids.FRYING_OIL.get().getSource(), 1000);
        fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(16), false);
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(count(fryer, BSItems.FRICADELLE.asStack()), 16, "fried"))
            .thenExecute(() -> {
                helper.assertValueEqual(fryer.getUsedOil().getPrimaryHandler().getFluidAmount(), 0, "used oil at ratio 0");
                FluidStack out = fryer.getFluidCapability().drain(100, IFluidHandler.FluidAction.EXECUTE);
                helper.assertTrue(out.getFluid().isSame(BSFluids.FRYING_OIL.get()) && out.getAmount() == 100, "at ratio 0 a pipe drains the fat, got " + out);
                ratio.set(before);
                ratio.save();
            })
            .thenSucceed();
    }

    private static net.neoforged.neoforge.common.ModConfigSpec.DoubleValue usedOilRatio() {
        try {
            var field = be.thefricadelle.belgiansnacks.config.BSConfig.class.getDeclaredField("FRYER_USED_OIL_RATIO");
            field.setAccessible(true);
            return (net.neoforged.neoforge.common.ModConfigSpec.DoubleValue) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("no FRYER_USED_OIL_RATIO", e);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static FryerBlockEntity fryer(GameTestHelper helper, HeatLevel heat) {
        return fryerAt(helper, FRYER, heat);
    }

    private static FryerBlockEntity fryerAt(GameTestHelper helper, BlockPos pos, HeatLevel heat) {
        if (heat != null) {
            burnerAt(helper, pos.below(), heat);
        }
        helper.setBlock(pos, BSBlocks.FRYER.getDefaultState());
        return helper.getBlockEntity(pos);
    }

    private static void burner(GameTestHelper helper, HeatLevel heat) {
        burnerAt(helper, BURNER, heat);
    }

    // A creative burner never burns out, so the heat level holds for the whole test.
    private static void burnerAt(GameTestHelper helper, BlockPos pos, HeatLevel heat) {
        helper.setBlock(pos, AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
        BlazeBurnerBlockEntity burner = helper.getBlockEntity(pos);
        burner.isCreative = true;
    }

    private static void fill(GameTestHelper helper, FryerBlockEntity fryer, Fluid fluid, int amount) {
        int filled = fryer.getTank().getCapability().fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        helper.assertValueEqual(filled, amount, "fat accepted");
    }

    private static int count(FryerBlockEntity fryer, ItemStack like) {
        int total = 0;
        for (int slot = 0; slot < fryer.getOutput().getSlots(); slot++) {
            ItemStack stack = fryer.getOutput().getStackInSlot(slot);
            if (ItemStack.isSameItem(stack, like)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
