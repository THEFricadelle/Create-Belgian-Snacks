/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.List;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Recipe contracts, checked against Create's own machines: recipes are looked up in the live
 * RecipeManager and basin recipes are matched against a real basin. The dev runtime has neither
 * Farmer's Delight nor the seed-oil mods, which fixes which conditional twins must be loaded.
 * Run with {@code ./gradlew runGameTestServer}.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RecipeGameTests {
    private static final String TEMPLATE = "empty";

    // Loaded in the dev runtime. mixing/curry_ketchup is deliberately absent: no tomato without Farmer's Delight.
    private static final Set<String> LOADED_IN_DEV = Set.of(
        "crushing/porkchop", "crushing/beef", "crushing/chicken", "crushing/bread",
        "milling/dried_kelp",
        "mixing/fricadelle_paste", "mixing/melted_beef_tallow", "mixing/mayonnaise", "mixing/curry_ketchup_from_beetroot",
        "compacting/frying_oil_from_seeds",
        "pressing/fricadelle_paste");

    private RecipeGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void conditionalRecipesFollowTheLoadedMods(GameTestHelper helper) {
        for (String id : LOADED_IN_DEV) {
            helper.assertTrue(find(helper, id) != null, id + " should be loaded");
        }
        helper.assertTrue(find(helper, "mixing/curry_ketchup") == null, "tomato recipe must stay off while c:crops/tomato is empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void crushingTurnsMeatIntoMinceAndBeefIntoTallow(GameTestHelper helper) {
        assertSingleInput(helper, "crushing/porkchop", Items.PORKCHOP);
        assertOutputs(helper, "crushing/porkchop", List.of(out(BSItems.MINCED_PORK.get(), 2, 1f)));
        assertSingleInput(helper, "crushing/beef", Items.BEEF);
        assertOutputs(helper, "crushing/beef", List.of(out(BSItems.MINCED_BEEF.get(), 2, 1f), out(BSItems.BEEF_TALLOW.get(), 1, 0.5f)));
        assertSingleInput(helper, "crushing/chicken", Items.CHICKEN);
        assertOutputs(helper, "crushing/chicken", List.of(out(BSItems.MINCED_CHICKEN.get(), 2, 1f)));
        assertSingleInput(helper, "crushing/bread", Items.BREAD);
        assertOutputs(helper, "crushing/bread", List.of(out(BSItems.BREAD_CRUMBS.get(), 3, 1f)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void millingAndPressing(GameTestHelper helper) {
        assertSingleInput(helper, "milling/dried_kelp", Items.DRIED_KELP);
        assertOutputs(helper, "milling/dried_kelp", List.of(out(BSItems.BELGIAN_SPICES.get(), 1, 1f), out(BSItems.BELGIAN_SPICES.get(), 1, 0.25f)));
        assertSingleInput(helper, "pressing/fricadelle_paste", BSItems.FRICADELLE_PASTE.get());
        assertOutputs(helper, "pressing/fricadelle_paste", List.of(out(BSItems.RAW_FRICADELLE.get(), 1, 1f)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pasteMixesOnlyWithAllThreeMeatsAndCrumbs(GameTestHelper helper) {
        BasinBlockEntity full = basin(helper, new BlockPos(0, 1, 0), false,
            List.of(BSItems.MINCED_PORK.asStack(), BSItems.MINCED_BEEF.asStack(), BSItems.MINCED_CHICKEN.asStack(), BSItems.BREAD_CRUMBS.asStack()), null);
        BasinBlockEntity noChicken = basin(helper, new BlockPos(2, 1, 0), false,
            List.of(BSItems.MINCED_PORK.asStack(), BSItems.MINCED_BEEF.asStack(), BSItems.BREAD_CRUMBS.asStack()), null);
        helper.assertTrue(BasinRecipe.match(full, recipe(helper, "mixing/fricadelle_paste")), "paste should mix with all ingredients");
        helper.assertFalse(BasinRecipe.match(noChicken, recipe(helper, "mixing/fricadelle_paste")), "paste must not mix without chicken");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mayonnaiseAcceptsAnyFryingOilButNotWater(GameTestHelper helper) {
        Recipe<?> mayo = recipe(helper, "mixing/mayonnaise");
        List<ItemStack> egg = List.of(new ItemStack(Items.EGG));
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, egg, new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 100)), mayo),
            "mayonnaise from frying oil");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, egg, new FluidStack(BSFluids.MELTED_BEEF_TALLOW.get().getSource(), 100)), mayo),
            "mayonnaise from melted beef tallow");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, egg, new FluidStack(Fluids.WATER, 100)), mayo),
            "water is not an oil");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), false, egg, new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 99)), mayo),
            "99 mB is not enough");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void heatedRecipesNeedALitBlazeBurner(GameTestHelper helper) {
        List<ItemStack> tallow = List.of(BSItems.BEEF_TALLOW.asStack(2));
        List<ItemStack> ketchup = List.of(new ItemStack(Items.BEETROOT), new ItemStack(Items.SUGAR), BSItems.BELGIAN_SPICES.asStack());
        Recipe<?> melting = recipe(helper, "mixing/melted_beef_tallow");
        Recipe<?> curry = recipe(helper, "mixing/curry_ketchup_from_beetroot");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, tallow, null), melting), "tallow must not melt cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), true, tallow, null), melting), "tallow melts over a lit burner");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 2), false, ketchup, null), curry), "curry ketchup must not cook cold");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 2), true, ketchup, null), curry), "curry ketchup cooks over a lit burner");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void seedsCompactIntoFryingOilWithoutOtherOilMods(GameTestHelper helper) {
        Recipe<?> seeds = recipe(helper, "compacting/frying_oil_from_seeds");
        helper.assertTrue(BasinRecipe.match(basin(helper, new BlockPos(0, 1, 0), false, List.of(new ItemStack(Items.WHEAT_SEEDS, 8)), null), seeds),
            "8 seeds compact into frying oil");
        helper.assertFalse(BasinRecipe.match(basin(helper, new BlockPos(2, 1, 0), false, List.of(new ItemStack(Items.WHEAT_SEEDS, 7)), null), seeds),
            "7 seeds are not enough");
        helper.succeed();
    }

    // End to end: a real mechanical press driven by a creative motor presses paste on a depot.
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aPoweredPressTurnsPasteIntoRawFricadelle(GameTestHelper helper) {
        BlockPos depot = new BlockPos(1, 0, 1);
        BlockPos press = new BlockPos(1, 2, 1);
        BlockPos motor = new BlockPos(2, 2, 1);
        helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
        helper.setBlock(press, AllBlocks.MECHANICAL_PRESS.getDefaultState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.WEST));
        IItemHandler depotItems = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depot), null);
        helper.assertTrue(depotItems != null, "depot exposes an item handler");
        depotItems.insertItem(0, BSItems.FRICADELLE_PASTE.asStack(), false);

        helper.succeedWhen(() -> {
            boolean pressed = false;
            for (int slot = 0; slot < depotItems.getSlots(); slot++) {
                pressed |= depotItems.getStackInSlot(slot).is(BSItems.RAW_FRICADELLE.get());
            }
            helper.assertTrue(pressed, "no raw fricadelle on the depot yet");
        });
    }

    private static BasinBlockEntity basin(GameTestHelper helper, BlockPos pos, boolean heated, List<ItemStack> items, FluidStack fluid) {
        // The burner goes first: a basin caches the heat below it on first read.
        if (heated) {
            helper.setBlock(pos.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED));
        }
        helper.setBlock(pos, AllBlocks.BASIN.getDefaultState());
        BlockPos absolute = helper.absolutePos(pos);
        IItemHandler itemHandler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absolute, null);
        for (ItemStack stack : items) {
            ItemStack left = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), false);
            helper.assertTrue(left.isEmpty(), "basin refused " + stack);
        }
        if (fluid != null) {
            IFluidHandler fluidHandler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolute, null);
            int filled = fluidHandler.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
            helper.assertValueEqual(filled, fluid.getAmount(), "fluid accepted by the basin");
        }
        return helper.getBlockEntity(pos);
    }

    private static Recipe<?> find(GameTestHelper helper, String id) {
        return helper.getLevel().getRecipeManager().byKey(BelgianSnacks.asResource(id)).map(holder -> holder.value()).orElse(null);
    }

    private static Recipe<?> recipe(GameTestHelper helper, String id) {
        Recipe<?> recipe = find(helper, id);
        helper.assertTrue(recipe != null, "recipe " + id + " is not loaded");
        return recipe;
    }

    private static void assertSingleInput(GameTestHelper helper, String id, Item input) {
        var ingredients = recipe(helper, id).getIngredients();
        helper.assertValueEqual(ingredients.size(), 1, id + " ingredient count");
        helper.assertTrue(ingredients.get(0).test(new ItemStack(input)), id + " should accept " + input);
    }

    private record Out(Item item, int count, float chance) {
    }

    private static Out out(Item item, int count, float chance) {
        return new Out(item, count, chance);
    }

    private static void assertOutputs(GameTestHelper helper, String id, List<Out> expected) {
        List<ProcessingOutput> actual = ((ProcessingRecipe<?, ?>) recipe(helper, id)).getRollableResults();
        helper.assertValueEqual(actual.size(), expected.size(), id + " output count");
        for (int i = 0; i < expected.size(); i++) {
            Out want = expected.get(i);
            ProcessingOutput got = actual.get(i);
            helper.assertTrue(got.getStack().is(want.item()) && got.getStack().getCount() == want.count() && got.getChance() == want.chance(),
                id + " output " + i + " is " + got.getStack() + " at " + got.getChance() + ", expected " + want);
        }
    }
}
