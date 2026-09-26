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

import com.tterrag.registrate.util.entry.FluidEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Fluid, bucket and fluid tag contracts. Run with {@code ./gradlew runGameTestServer}.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluidGameTests {
    private static final String TEMPLATE = "empty";
    private static final List<FluidEntry<BaseFlowingFluid.Flowing>> FLUIDS =
        List.of(BSFluids.FRYING_OIL, BSFluids.MELTED_BEEF_TALLOW, BSFluids.MAYONNAISE, BSFluids.CURRY_KETCHUP);

    private FluidGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void everyFluidHasASourceAFlowAndABucket(GameTestHelper helper) {
        for (var entry : FLUIDS) {
            var source = entry.get().getSource();
            var flowing = entry.get().getFlowing();
            helper.assertTrue(BuiltInRegistries.FLUID.getKey(source).getNamespace().equals(BelgianSnacks.MOD_ID), entry.getId() + " source registered");
            helper.assertTrue(source != flowing && flowing != Fluids.EMPTY, entry.getId() + " has a distinct flowing fluid");
            var bucket = entry.getBucket().orElse(null);
            helper.assertTrue(bucket instanceof BucketItem, entry.getId() + " has a bucket");
            helper.assertTrue(((BucketItem) bucket).content == source, entry.getId() + " bucket holds its own fluid");
            helper.assertTrue(new ItemStack((BucketItem) bucket).is(Tags.Items.BUCKETS), entry.getId() + " bucket in c:buckets");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fryingOilsAcceptOilAndTallowButNotSauces(GameTestHelper helper) {
        helper.assertTrue(BSFluids.FRYING_OIL.get().getSource().defaultFluidState().is(BSTags.FRYING_OILS), "frying oil is a frying oil");
        helper.assertTrue(BSFluids.MELTED_BEEF_TALLOW.get().getSource().defaultFluidState().is(BSTags.FRYING_OILS), "melted tallow is a frying oil");
        helper.assertTrue(BSFluids.FRYING_OIL.get().getFlowing().defaultFluidState().is(BSTags.FRYING_OILS), "flowing oil keeps the tag");
        helper.assertFalse(BSFluids.MAYONNAISE.get().getSource().defaultFluidState().is(BSTags.FRYING_OILS), "mayonnaise is not a frying oil");
        helper.assertFalse(BSFluids.CURRY_KETCHUP.get().getSource().defaultFluidState().is(BSTags.FRYING_OILS), "curry ketchup is not a frying oil");
        helper.assertFalse(Fluids.WATER.defaultFluidState().is(BSTags.FRYING_OILS), "water is not a frying oil");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void emptyingABucketPlacesASourceBlock(GameTestHelper helper) {
        for (int i = 0; i < FLUIDS.size(); i++) {
            var entry = FLUIDS.get(i);
            // Corners of the 3x3 floor, so no two fluids touch.
            BlockPos pos = new BlockPos((i % 2) * 2, 1, (i / 2) * 2);
            BucketItem bucket = (BucketItem) entry.getBucket().orElseThrow();
            boolean placed = bucket.emptyContents(null, helper.getLevel(), helper.absolutePos(pos), null, new ItemStack(bucket));
            FluidState state = helper.getLevel().getFluidState(helper.absolutePos(pos));
            helper.assertTrue(placed, entry.getId() + " bucket could not be emptied");
            helper.assertTrue(state.isSource() && state.getType() == entry.get().getSource(), entry.getId() + " did not place its source");
        }
        helper.succeed();
    }
}
