/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Where the v2 seeds and mussels come from (docs/13, section 5.8): wild grass and fishing. */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LootGameTests {
    private static final String TEMPLATE = "empty";
    private static final int ROLLS = 3000;

    private LootGameTests() {
    }

    // About one grass in seven gives a seed: every one of the seven turns up in a few thousand.
    @GameTest(template = TEMPLATE)
    public static void wildGrassGivesEverySeed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 origin = helper.absoluteVec(new Vec3(1, 1, 1));
        Set<Item> found = new HashSet<>();
        for (int i = 0; i < ROLLS; i++) {
            LootParams.Builder params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
            Blocks.SHORT_GRASS.defaultBlockState().getDrops(params).forEach(stack -> found.add(stack.getItem()));
        }
        for (Item seed : List.of(BSCrops.CHICORY_ROOT.get(), BSCrops.LEEK.get(), BSCrops.HOP_SEEDS.get(), BSCrops.BRUSSELS_SPROUT.get(),
            BSCrops.ASPARAGUS_CROWN.get(), BSCrops.BUCKWHEAT.get(), BSCrops.MUSTARD_SEEDS.get())) {
            helper.assertTrue(found.contains(seed), seed + " never came out of " + ROLLS + " grass: " + found);
        }
        helper.assertTrue(found.contains(Items.WHEAT_SEEDS), "the vanilla seeds are gone: " + found);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fishingBringsMussels(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_FISH);
        int mussels = 0;
        for (int i = 0; i < ROLLS; i++) {
            LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, helper.absoluteVec(new Vec3(1, 1, 1)))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD)).create(LootContextParamSets.FISHING);
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(BSCrops.MUSSEL.get())) {
                    mussels++;
                }
            }
        }
        // 5 % of 3000: about 150; far from zero and far from every catch.
        helper.assertTrue(mussels > 50 && mussels < 400, mussels + " mussels in " + ROLLS + " catches");
        helper.succeed();
    }
}
