/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.multiplayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Server half of the two-client run (tools/mp_smoke.py): sets up a frying fryer next to spawn,
 * waits for both clients, and stops the server once they have both left, writing what it saw.
 * Inert unless {@code -Dcreate_belgian_snacks.mpSmoke=server}, which only the serverSmoke run sets.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID)
public final class ServerMultiplayerSmoke {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = "server".equals(System.getProperty("create_belgian_snacks.mpSmoke"));
    private static final int TIMEOUT_TICKS = 20 * 60 * 6;

    private static final List<String> REPORT = new ArrayList<>();
    private static BlockPos fryerPos;
    private static BlockPos grinderPos;
    private static int joined;
    private static int ticks;
    private static boolean done;

    private ServerMultiplayerSmoke() {
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (!ENABLED) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        server.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, server);
        server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.setDayTime(6000);

        BlockPos spawn = level.getSharedSpawnPos();
        BlockPos column = spawn.east(2);
        fryerPos = new BlockPos(column.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()), column.getZ());
        // The burner replaces the ground block, so the fryer stands at the players' feet level.
        level.setBlockAndUpdate(fryerPos.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
            .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        if (level.getBlockEntity(fryerPos.below()) instanceof BlazeBurnerBlockEntity burner) {
            burner.isCreative = true;
        }
        level.setBlockAndUpdate(fryerPos, BSBlocks.FRYER.getDefaultState());
        FryerBlockEntity fryer = (FryerBlockEntity) level.getBlockEntity(fryerPos);
        fryer.getTank().getCapability().fill(new FluidStack(BSFluids.FRYING_OIL.get().getSource(), 2000), IFluidHandler.FluidAction.EXECUTE);
        fryer.getItemCapability().insertItem(0, BSItems.RAW_FRICADELLE.asStack(16), false);
        LOGGER.info("[mpsmoke] fryer placed at {}", fryerPos);
        REPORT.add("PASS server.setup - fryer at " + fryerPos.toShortString());

        // A running supreme grinder two blocks north of the fryer, within reach of both players.
        grinderPos = fryerPos.north(2);
        level.setBlockAndUpdate(grinderPos, BSBlocks.SUPREME_GRINDER.getDefaultState());
        level.setBlockAndUpdate(grinderPos.above(), AllBlocks.CREATIVE_MOTOR.getDefaultState()
            .setValue(BlockStateProperties.FACING, Direction.DOWN));
        ((CreativeMotorBlockEntity) level.getBlockEntity(grinderPos.above())).generatedSpeed.setValue(64);
        REPORT.add("PASS server.grinderSetup - supreme grinder at " + grinderPos.toShortString());
        var index = be.thefricadelle.belgiansnacks.content.food.FoodIndex.server();
        REPORT.add("PASS server.foodIndex - " + index.size() + " foods, fingerprint " + index.fingerprint());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (ENABLED) {
            // Both would spawn on the same block: put A west and B east of the fryer, facing each other
            // across it. The server does it, since a client walking through the fryer is rejected.
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player && fryerPos != null) {
                boolean a = player.getName().getString().endsWith("A");
                player.teleportTo(fryerPos.getX() + (a ? -1.5 : 2.5), fryerPos.getY(), fryerPos.getZ() + 0.5);
                // One food each for the grinder, a different one per player.
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(a ? Items.APPLE : Items.BREAD));
                // A also eats THE_FRICADELLE; B checks the visitor and the announcement arrive.
                if (a) {
                    player.getInventory().setItem(1, BSItems.ULTIMATE_FRICADELLE.asStack());
                }
            }
            joined++;
            LOGGER.info("[mpsmoke] {} joined ({} so far)", event.getEntity().getName().getString(), joined);
        }
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        if (!ENABLED || done) {
            return;
        }
        MinecraftServer server = event.getServer();
        ticks++;
        boolean allLeft = joined >= 2 && server.getPlayerList().getPlayerCount() == 0;
        if (!allLeft && ticks < TIMEOUT_TICKS) {
            return;
        }
        done = true;
        if (!allLeft) {
            REPORT.add("FAIL server.players - " + joined + " of 2 clients joined and left before the timeout");
        } else {
            REPORT.add("PASS server.players - both clients joined and left");
            FryerBlockEntity fryer = (FryerBlockEntity) server.overworld().getBlockEntity(fryerPos);
            boolean emptied = fryer.getOutput().getStackInSlot(0).isEmpty() && fryer.getOutput().getStackInSlot(1).isEmpty();
            REPORT.add((emptied ? "PASS" : "FAIL") + " server.outputTaken - output " + (emptied ? "taken by a client" : "still holds "
                + fryer.getOutput().getStackInSlot(0)));
            SupremeGrinderBlockEntity grinder = (SupremeGrinderBlockEntity) server.overworld().getBlockEntity(grinderPos);
            Set<ResourceLocation> expected = Set.of(ResourceLocation.parse("minecraft:apple"), ResourceLocation.parse("minecraft:bread"));
            boolean fed = grinder.getConsumed().equals(expected);
            REPORT.add((fed ? "PASS" : "FAIL") + " server.grinderFed - collection " + grinder.getConsumed() + " from both players");
        }
        boolean pass = REPORT.stream().noneMatch(line -> line.startsWith("FAIL"));
        REPORT.add(pass ? "RESULT PASS " + REPORT.size() + " checks" : "RESULT FAIL");
        try {
            Files.write(FMLPaths.GAMEDIR.get().resolve("smoke-mp-server.txt"), REPORT, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[mpsmoke] could not write the server report", e);
        }
        server.halt(false);
    }
}
