/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.util.entry.FluidEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderContents;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSDataComponents;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Phase 1 of the removal smoke run (tools/uninstall_smoke.py, spec v2 rule R5): a fresh flat world
 * where every kind of thing of ours is left where a removal would find it, then saved and closed.
 * Phases 2 and 3 open this world without the mod, then with it again.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class UninstallSeedClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = "seed".equals(System.getProperty("create_belgian_snacks.uninstallSmoke"));
    static final String WORLD = "belgian-snacks-uninstall";
    private static final int TIMEOUT_TICKS = 20 * 60 * 4;

    private static int ticks;
    private static int stage;
    private static int inWorld;
    private static final List<String> REPORT = new ArrayList<>();
    private static volatile boolean seeded;

    private UninstallSeedClient() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || stage == 3) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (ticks > TIMEOUT_TICKS) {
            REPORT.add("FAIL timeout - stage " + stage);
            finish(mc);
            return;
        }
        if (stage == 0 && mc.getOverlay() == null && mc.screen != null && ticks > 20) {
            stage = 1;
            LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                    .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                mc.screen);
        } else if (stage == 1) {
            inWorld = mc.player != null && mc.level != null && mc.screen == null ? inWorld + 1 : 0;
            if (inWorld >= 40) {
                stage = 2;
                var server = mc.getSingleplayerServer();
                var id = mc.player.getUUID();
                server.execute(() -> {
                    try {
                        seed(server.getPlayerList().getPlayer(id));
                        server.saveEverything(false, true, true);
                    } catch (RuntimeException e) {
                        LOGGER.error("[uninstall-seed] seeding threw", e);
                        REPORT.add("FAIL seed - threw " + e);
                    }
                    seeded = true;
                });
            }
        } else if (stage == 2 && seeded) {
            finish(mc);
        }
    }

    // Everything of ours, around the player: in their inventory and ender chest, in vanilla and Create
    // containers, our machines with their contents, and our fluids in tanks and as placed blocks.
    private static void seed(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos o = player.blockPosition();
        List<Item> ours = BuiltInRegistries.ITEM.stream()
            .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(BelgianSnacks.MOD_ID))
            .toList();

        for (Item item : ours) {
            player.getInventory().add(new ItemStack(item));
        }
        player.getEnderChestInventory().setItem(0, BSItems.ULTIMATE_FRICADELLE.asStack());
        player.getEnderChestInventory().setItem(1, fryerWithFat());
        REPORT.add("PASS player - " + ours.size() + " items given, 2 in the ender chest");

        BlockPos chestPos = o.offset(2, 0, 0);
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        for (int slot = 0; slot < Math.min(ours.size(), chest.getContainerSize()); slot++) {
            chest.setItem(slot, new ItemStack(ours.get(slot)));
        }
        BlockPos barrelPos = o.offset(2, 0, 1);
        level.setBlockAndUpdate(barrelPos, Blocks.BARREL.defaultBlockState());
        var barrel = (net.minecraft.world.level.block.entity.BarrelBlockEntity) level.getBlockEntity(barrelPos);
        barrel.setItem(0, fryerWithFat());
        barrel.setItem(1, grinderWithContents());

        BlockPos depotPos = o.offset(2, 0, 2);
        level.setBlockAndUpdate(depotPos, AllBlocks.DEPOT.getDefaultState());
        IItemHandler depot = level.getCapability(Capabilities.ItemHandler.BLOCK, depotPos, null);
        ItemStack left = ItemHandlerHelper.insertItem(depot, BSItems.FRICADELLE.asStack(), false);
        REPORT.add((left.isEmpty() ? "PASS" : "FAIL") + " depot - a fricadelle on a Create depot");

        List<? extends FluidEntry<?>> fluids = BSFluids.ALL;
        for (int i = 0; i < fluids.size(); i++) {
            BlockPos tankPos = o.offset(4 + i, 0, 0);
            level.setBlockAndUpdate(tankPos, AllBlocks.FLUID_TANK.getDefaultState());
            IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, tankPos, null);
            int filled = tank.fill(new FluidStack(fluids.get(i).get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE);
            REPORT.add((filled == 1000 ? "PASS" : "FAIL") + " tank - " + filled + " mB of " + fluids.get(i).getId() + " in a Create tank");
            level.setBlockAndUpdate(o.offset(4 + i, 0, 3), fluids.get(i).get().getSource().defaultFluidState().createLegacyBlock());
        }

        BlockPos fryerPos = o.offset(-2, 0, 0);
        level.setBlockAndUpdate(fryerPos, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = (FryerBlockEntity) level.getBlockEntity(fryerPos);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE);
        BlockPos grinderPos = o.offset(-2, 0, 2);
        level.setBlockAndUpdate(grinderPos, BSBlocks.SUPREME_GRINDER.getDefaultState());
        SupremeGrinderBlockEntity grinder = (SupremeGrinderBlockEntity) level.getBlockEntity(grinderPos);
        grinder.setMode(GrinderMode.ULTIMATE);
        grinder.setConsumed(FoodIndex.server().ids().subList(0, 10));
        REPORT.add("PASS machines - a fryer with 1000 mB of oil, a grinder in ULTIMATE mode with 10 foods");
    }

    private static ItemStack fryerWithFat() {
        ItemStack stack = BSBlocks.FRYER.asStack();
        stack.set(BSDataComponents.FRYER_FLUID.get(), SimpleFluidContent.copyOf(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 500)));
        return stack;
    }

    private static ItemStack grinderWithContents() {
        ItemStack stack = BSBlocks.SUPREME_GRINDER.asStack();
        stack.set(BSDataComponents.GRINDER_CONTENTS.get(), new GrinderContents(FoodIndex.server().ids().subList(0, 5), GrinderMode.ULTIMATE.id()));
        return stack;
    }

    private static void finish(Minecraft mc) {
        stage = 3;
        boolean failed = REPORT.stream().anyMatch(line -> line.startsWith("FAIL"));
        List<String> lines = new ArrayList<>(REPORT);
        lines.add(failed ? "RESULT FAIL" : "RESULT PASS " + REPORT.size() + " checks");
        Path report = FMLPaths.GAMEDIR.get().resolve("uninstall-seed-report.txt");
        try {
            Files.write(report, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[uninstall-seed] could not write {}", report, e);
        }
        LOGGER.info("[uninstall-seed] {}", lines);
        if (mc.level != null) {
            mc.level.disconnect();
            mc.disconnect();
        }
        mc.stop();
    }
}
