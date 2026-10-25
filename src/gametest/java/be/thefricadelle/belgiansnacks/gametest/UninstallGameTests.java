/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.ArrayList;
import java.util.List;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.uninstall.SavedStackLists;
import be.thefricadelle.belgiansnacks.content.uninstall.UninstallScan;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Preparing a removal of the mod (spec v2, rule R4): the scan, the purge of other mods' data and the
 * preparation switch.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UninstallGameTests {
    private static final String TEMPLATE = "empty";

    private UninstallGameTests() {
    }

    // Other tests run beside this one, so the scan is checked on the lines of this test's positions.
    @GameTest(template = TEMPLATE)
    public static void theScanFindsOurItemsFluidsBlocksAndPlayers(GameTestHelper helper) {
        BlockPos chestPos = new BlockPos(0, 1, 0);
        BlockPos fryerPos = new BlockPos(2, 1, 0);
        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(BSItems.FRICADELLE.get(), 3));
        chest.setItem(1, new ItemStack(Items.BREAD, 5));
        helper.setBlock(fryerPos, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = helper.getBlockEntity(fryerPos);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 500), IFluidHandler.FluidAction.EXECUTE);
        ServerPlayer player = TestPlayers.inLevel(helper, new Vec3(1.5, 1, 2.5));
        player.getInventory().add(new ItemStack(BSItems.MINCED_PORK.get(), 2));
        player.getEnderChestInventory().setItem(0, BSItems.ULTIMATE_FRICADELLE.asStack());
        try {
            UninstallScan.Report report = UninstallScan.run(helper.getLevel().getServer());
            String dimension = helper.getLevel().dimension().location().toString();
            String name = player.getGameProfile().getName();
            expect(helper, report, dimension + " " + helper.absolutePos(chestPos).toShortString() + ": 3 of our items");
            expect(helper, report, dimension + " " + helper.absolutePos(fryerPos).toShortString() + ": " + BelgianSnacks.MOD_ID + ":fryer");
            expect(helper, report, dimension + " " + helper.absolutePos(fryerPos).toShortString() + ": 500 mB of our fluids");
            expect(helper, report, "player " + name + " inventory: 2 of our items");
            expect(helper, report, "player " + name + " ender chest: 1 of our items");
            helper.assertTrue(report.items() >= 6 && report.fluidMillibuckets() >= 500 && report.blocks() >= 1,
                "totals too low: " + report);
        } finally {
            TestPlayers.remove(helper, player);
        }
        helper.succeed();
    }

    // Solonion's history in miniature: saved stacks, and saving an empty stack throws, as there.
    @GameTest(template = TEMPLATE)
    public static void thePurgeRemovesOnlyOurEntriesAndLeavesNoEmptyStack(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        History history = new History(List.of(new ItemStack(Items.APPLE), BSItems.FRICADELLE.asStack(),
            new ItemStack(Items.BREAD), BSItems.ULTIMATE_FRICADELLE.asStack()));
        helper.assertValueEqual(SavedStackLists.count(history, registries, BelgianSnacks.MOD_ID), 2, "entries of ours before");
        helper.assertValueEqual(SavedStackLists.purge(history, registries, BelgianSnacks.MOD_ID), 2, "entries removed");
        helper.assertValueEqual(history.stacks.size(), 2, "entries left");
        helper.assertTrue(history.stacks.get(0).is(Items.APPLE) && history.stacks.get(1).is(Items.BREAD),
            "foreign entries lost or reordered: " + history.stacks);
        helper.assertValueEqual(history.serializeNBT(registries).size(), 2, "saved after the purge");
        helper.assertValueEqual(SavedStackLists.purge(history, registries, BelgianSnacks.MOD_ID), 0, "a second purge");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "uninstall_prepare")
    public static void operatorsSwitchThePreparationAndPurgeWithoutSolonion(GameTestHelper helper) {
        boolean before = BSConfig.uninstallPrepare();
        try {
            helper.assertFalse(BSConfig.uninstallPrepare(), "preparing by default");
            command(helper, "belgiansnacks uninstall prepare true");
            helper.assertTrue(BSConfig.uninstallPrepare(), "the command did not turn it on");
            command(helper, "belgiansnacks uninstall prepare false");
            helper.assertFalse(BSConfig.uninstallPrepare(), "the command did not turn it off");
            // No Solonion in the test runtime: nothing to purge, and no error.
            helper.assertValueEqual(command(helper, "belgiansnacks uninstall purge-references"), 0, "entries purged");
        } finally {
            BSConfig.setUninstallPrepare(before);
        }
        helper.succeed();
    }

    private static void expect(GameTestHelper helper, UninstallScan.Report report, String line) {
        helper.assertTrue(report.lines().contains(line), "the scan misses \"" + line + "\": " + report.lines());
    }

    private static int command(GameTestHelper helper, String command) {
        var server = helper.getLevel().getServer();
        try {
            return server.getCommands().getDispatcher().execute(command,
                server.createCommandSourceStack().withLevel(helper.getLevel()).withSuppressedOutput());
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw new IllegalStateException("/" + command + ": " + e.getMessage(), e);
        }
    }

    private static final class History implements INBTSerializable<ListTag> {
        List<ItemStack> stacks;

        History(List<ItemStack> stacks) {
            this.stacks = new ArrayList<>(stacks);
        }

        @Override
        public ListTag serializeNBT(HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (ItemStack stack : stacks) {
                list.add(stack.save(registries));
            }
            return list;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, ListTag list) {
            stacks = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                stacks.add(ItemStack.parseOptional(registries, entry));
            }
        }
    }
}
