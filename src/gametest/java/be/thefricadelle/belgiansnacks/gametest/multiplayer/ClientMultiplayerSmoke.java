/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.multiplayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.client.GrinderMissingScreen;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.network.GrinderMissingRequestPayload;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client half of the two-client run (tools/mp_smoke.py). Both clients join the dedicated server and
 * check they see the same frying fryer and each other. Client B crouches once it has seen the full
 * output; client A waits for that crouch, synced through the server, then takes the output with a
 * real right click sent over the network, and client B checks it sees the output go. Each writes a
 * report.
 * Inert unless {@code -Dcreate_belgian_snacks.mpSmoke=A} or {@code =B}.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class ClientMultiplayerSmoke {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ROLE = System.getProperty("create_belgian_snacks.mpSmoke", "");
    private static final boolean ENABLED = ROLE.equals("A") || ROLE.equals("B");
    private static final int TIMEOUT_TICKS = 20 * 60 * 5;
    // Settle time between B's crouch reaching A and A's right click.
    private static final int TAKE_DELAY_TICKS = 10;

    private static final List<String> REPORT = new ArrayList<>();
    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean planned;
    private static boolean finished;
    private static boolean joined;
    private static int ticks;
    private static int wait;
    private static int failures;
    private static BlockPos fryerPos;
    private static BlockPos grinderPos;

    private ClientMultiplayerSmoke() {
    }

    private record Step(String name, int delayTicks, BooleanSupplier ready, Runnable action) {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || finished) {
            return;
        }
        if (!planned) {
            plan();
            planned = true;
        }
        ticks++;
        if (ticks > TIMEOUT_TICKS) {
            Step stuck = STEPS.peek();
            fail("runner.timeout", "stuck waiting on " + (stuck == null ? "nothing" : stuck.name()));
            finish();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (joined && (mc.getConnection() == null || mc.level == null || mc.player == null)) {
            Step stuck = STEPS.peek();
            fail("runner.disconnected", "connection lost while waiting on " + (stuck == null ? "nothing" : stuck.name()));
            finish();
            return;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.peek();
        if (step == null) {
            finish();
            return;
        }
        if (!step.ready().getAsBoolean()) {
            return;
        }
        STEPS.poll();
        LOGGER.info("[mpsmoke {}] {}", ROLE, step.name());
        try {
            step.action().run();
        } catch (Throwable t) {
            LOGGER.error("[mpsmoke {}] step {} threw", ROLE, step.name(), t);
            fail(step.name(), "threw " + t);
        }
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delayTicks();
    }

    private static void plan() {
        Minecraft mc = Minecraft.getInstance();
        step("joined", 0, () -> mc.player != null && mc.level != null && mc.screen == null && mc.getConnection() != null,
            () -> {
                joined = true;
                pass("mp.joined", "connected as " + mc.player.getName().getString());
            });
        step("fryer.found", 20, () -> findFryer() != null, () -> {
            fryerPos = findFryer();
            pass("mp.fryerSynced", "fryer block entity at " + fryerPos.toShortString());
        });
        step("face.fryer", 10, () -> true, () -> lookAt(fryerPos));
        step("foodIndex", 0, () -> be.thefricadelle.belgiansnacks.content.food.FoodIndex.client().size() > 0, () -> {
            var index = be.thefricadelle.belgiansnacks.content.food.FoodIndex.client();
            // tools/mp_smoke.py compares this fingerprint with the server's report.
            pass("mp.foodIndex", index.size() + " foods, fingerprint " + index.fingerprint());
        });
        step("players", 10, () -> mc.getConnection().getOnlinePlayers().size() >= 2 && mc.level.players().size() >= 2,
            () -> pass("mp.twoPlayers", mc.level.players().size() + " players in view, "
                + mc.getConnection().getOnlinePlayers().size() + " in the tab list"));
        step("batch.synced", 0, () -> fried() == 16, () -> {
            FryerBlockEntity fryer = fryer();
            require(fryer.getTank().getPrimaryHandler().getFluidAmount() == 2000 - 160,
                "client sees " + fryer.getTank().getPrimaryHandler().getFluidAmount() + " mB, expected 1840");
            pass("mp.batchSynced", "16 fricadelles and 1840 mB seen after the server fried the batch");
        });
        step("shot", 10, () -> true, () -> screenshot("mp-" + ROLE.toLowerCase() + "-fryer"));
        // Each player feeds the same grinder the food the server gave them (A an apple, B a bread).
        step("grinder.found", 0, () -> findGrinder() != null, () -> grinderPos = findGrinder());
        step("grinder.feed", 5, () -> true, () -> {
            require(FoodIndex.client().contains(BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem())),
                "no food in hand: " + mc.player.getMainHandItem());
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(grinderPos), Direction.UP, grinderPos, false));
        });
        step("grinder.fed", 0, () -> mc.player.getMainHandItem().isEmpty(),
            () -> pass("mp.grinderFeed", "right click over the network put the food in the grinder"));
        step("grinder.shared", 0, () -> grinder() != null && grinder().getCount() == 2,
            () -> pass("mp.grinderShared", "both players' foods counted here: 2 / " + grinder().getGoal()));
        if (ROLE.equals("A")) {
            // A real right click held until THE_FRICADELLE is eaten, then back to the empty first slot.
            // Looking at the sky: the held click repeats once after the last bite, and on the fryer it
            // would take the batch with the hand just emptied.
            step("ultimate.select", 5, () -> mc.player.getInventory().getItem(1).is(BSItems.ULTIMATE_FRICADELLE.get()), () -> {
                mc.player.setXRot(-90f);
                select(1);
            });
            step("ultimate.eat", 5, () -> true, () -> mc.options.keyUse.setDown(true));
            step("ultimate.eaten", 0, () -> mc.player.getInventory().getItem(1).isEmpty(), () -> {
                mc.options.keyUse.setDown(false);
                select(0);
                pass("mp.ultimateEaten", "THE_FRICADELLE eaten by holding right click");
            });
        }
        // Both players see THEFricadelle run up and say its line above its head (not in the chat), and
        // read the server's announcement.
        step("visitor.seen", 0, () -> visitor() != null
            && visitor().getPhase() != be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.Phase.RUN && CHAT.stream().anyMatch(line -> line.contains("ate THE_FRICADELLE"))
            && !visitor().getCustomName().getString().equals(be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.NAME), () -> {
            require(CHAT.stream().noneMatch(line -> line.startsWith("<THEFricadelle> ")), "the visitor wrote in the chat: " + CHAT);
            pass("mp.visitorSeen",
                "THEFricadelle appeared saying \"" + visitor().getCustomName().getString() + "\"; chat: " + String.join(" | ", CHAT));
        });
        if (ROLE.equals("B")) {
            step("grinder.missing.ask", 0, () -> true, () -> PacketDistributor.sendToServer(new GrinderMissingRequestPayload(grinderPos)));
            step("grinder.missing", 0, () -> mc.screen instanceof GrinderMissingScreen, () -> {
                int expected = FoodIndex.client().size() - 2;
                int listed = ((GrinderMissingScreen) mc.screen).missingCount();
                require(listed == expected, "the screen lists " + listed + " missing foods, expected " + expected);
                pass("mp.grinderMissing", listed + " missing foods sent by the dedicated server");
            });
            step("grinder.missing.shot", 10, () -> true, () -> screenshot("mp-b-grinder-missing"));
            step("grinder.missing.close", 5, () -> true, () -> mc.setScreen(null));
        }
        if (ROLE.equals("A")) {
            step("other.ready", 0, () -> otherPlayerCrouching(),
                () -> pass("mp.handshake", "the other client's crouch arrived through the server"));
            step("face.fryer.again", 0, () -> true, () -> lookAt(fryerPos));
            step("take", TAKE_DELAY_TICKS, () -> true, () -> {
                require(mc.player.getMainHandItem().isEmpty(), "client A holds " + mc.player.getMainHandItem() + " in slot " + mc.player.getInventory().selected);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(fryerPos), Direction.UP, fryerPos, false);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
            });
            step("take.received", 0, () -> mc.player.getInventory().countItem(BSItems.FRICADELLE.get()) >= 16,
                () -> pass("mp.takeByHand", "right click over the network moved 16 fricadelles into the inventory"));
        } else {
            // Tells A it may take the output: B has seen the batch and client A.
            step("signal", 0, () -> true, () -> mc.options.keyShift.setDown(true));
            step("take.seen", 0, () -> fried() == 0,
                () -> pass("mp.takeSeenByOther", "the output emptied by the other player is empty here too"));
        }
        step("seen.empty", 0, () -> fried() == 0, () -> pass("mp.outputSynced", "output empty on this client"));
    }

    private static void step(String name, int delay, BooleanSupplier ready, Runnable action) {
        STEPS.add(new Step(name, delay, ready, action));
    }

    // ------------------------------------------------------------------ helpers

    private static boolean otherPlayerCrouching() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.players().stream().anyMatch(player -> player != mc.player && player.isShiftKeyDown());
    }

    private static final List<String> CHAT = java.util.Collections.synchronizedList(new ArrayList<>());

    @SubscribeEvent
    public static void onSystemChat(net.neoforged.neoforge.client.event.ClientChatReceivedEvent.System event) {
        if (ENABLED) {
            CHAT.add(event.getMessage().getString());
        }
    }

    private static be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc visitor() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.getEntitiesOfClass(be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc.class,
            mc.player.getBoundingBox().inflate(16)).stream().findFirst().orElse(null);
    }

    private static void select(int slot) {
        Minecraft mc = Minecraft.getInstance();
        mc.player.getInventory().selected = slot;
        mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(slot));
    }

    private static BlockPos findGrinder() {
        Minecraft mc = Minecraft.getInstance();
        BlockPos origin = mc.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -3, -6), origin.offset(6, 3, 6))) {
            if (mc.level.getBlockEntity(pos) instanceof SupremeGrinderBlockEntity) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static SupremeGrinderBlockEntity grinder() {
        return Minecraft.getInstance().level.getBlockEntity(grinderPos) instanceof SupremeGrinderBlockEntity grinder ? grinder : null;
    }

    private static BlockPos findFryer() {
        Minecraft mc = Minecraft.getInstance();
        BlockPos origin = mc.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -3, -6), origin.offset(6, 3, 6))) {
            if (mc.level.getBlockEntity(pos) instanceof FryerBlockEntity) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static FryerBlockEntity fryer() {
        return (FryerBlockEntity) Minecraft.getInstance().level.getBlockEntity(fryerPos);
    }

    private static int fried() {
        FryerBlockEntity fryer = fryer();
        if (fryer == null) {
            return -1;
        }
        int total = 0;
        for (int slot = 0; slot < fryer.getOutput().getSlots(); slot++) {
            if (fryer.getOutput().getStackInSlot(slot).is(BSItems.FRICADELLE.get())) {
                total += fryer.getOutput().getStackInSlot(slot).getCount();
            }
        }
        return total;
    }

    private static void lookAt(BlockPos pos) {
        var player = Minecraft.getInstance().player;
        Vec3 eye = player.getEyePosition();
        Vec3 target = Vec3.atCenterOf(pos);
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        player.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90));
        player.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    private static void screenshot(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, "smoke-" + name + ".png", mc.getMainRenderTarget(),
            message -> LOGGER.info("[mpsmoke {}] screenshot {}: {}", ROLE, name, message.getString()));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void pass(String name, String detail) {
        REPORT.add("PASS " + name + " - " + detail);
        LOGGER.info("[mpsmoke {}] PASS {} - {}", ROLE, name, detail);
    }

    private static void fail(String name, String detail) {
        failures++;
        REPORT.add("FAIL " + name + " - " + detail);
        LOGGER.error("[mpsmoke {}] FAIL {} - {}", ROLE, name, detail);
    }

    private static void finish() {
        finished = true;
        List<String> lines = new ArrayList<>(REPORT);
        lines.add(failures == 0 ? "RESULT PASS " + REPORT.size() + " checks" : "RESULT FAIL " + failures + " of " + REPORT.size() + " checks failed");
        try {
            Files.write(FMLPaths.GAMEDIR.get().resolve("smoke-mp-report.txt"), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[mpsmoke {}] could not write the report", ROLE, e);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.disconnect();
        }
        mc.stop();
    }
}
