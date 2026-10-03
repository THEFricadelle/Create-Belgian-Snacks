/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.showcase;

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
import be.thefricadelle.belgiansnacks.network.GrinderMissingRequestPayload;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The spark scenario of docs/10 run by itself in the showcase world ({@code ./gradlew runShowcaseSpark};
 * spark must be in run/showcase/mods, never in the build). Reports are saved to files, never uploaded:
 * publishing one is the author's call. Every chat line spark prints goes to run/showcase/spark-report.txt.
 */
@EventBusSubscriber(modid = BelgianSnacks.MOD_ID, value = Dist.CLIENT)
public final class ShowcaseSpark {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WARM_UP_TICKS = 20 * 30;
    private static final int PROFILE_TICKS = 20 * 60;
    private static final int VISITORS = 20;
    private static final int SCREEN_CYCLES = 50;

    private record Step(String name, int delay, BooleanSupplier ready, Runnable action) {
    }

    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static final List<String> CHAT = new ArrayList<>();
    private static boolean planned;
    private static int wait;
    private static int screenCycles;

    private ShowcaseSpark() {
    }

    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent.System event) {
        if (Showcase.MODE.equals("spark")) {
            CHAT.add(event.getMessage().getString());
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Showcase.MODE.equals("spark") || mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        if (!planned) {
            planned = true;
            plan();
            wait = STEPS.peek().delay();
        }
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.peek();
        if (step == null || !step.ready().getAsBoolean()) {
            return;
        }
        STEPS.poll();
        LOGGER.info("[showcase-spark] {}", step.name());
        step.action().run();
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delay();
    }

    private static void plan() {
        // The player stands over the path, every line within the simulation distance.
        add("view", 0, () -> teleport(-4.5, 6, 4, 0, 30));
        add("heap.before", WARM_UP_TICKS, () -> command("spark heapsummary --save-to-file"));
        add("server.start", 100, () -> command("spark profiler start"));
        add("server.tps", PROFILE_TICKS - 20, () -> command("spark tps"));
        add("server.stop", 20, () -> command("spark profiler stop --save-to-file"));
        add("client.start", 100, () -> command("sparkc profiler start"));
        add("client.stop", PROFILE_TICKS, () -> command("sparkc profiler stop --save-to-file"));
        add("visitors", 100, ShowcaseSpark::eatUltimates);
        // Next to the THE_ grinder: the list is only sent within reach.
        add("grinder.near", 20 * 15, () -> teleport(Showcase.TIER2.getX() - 1.5, 3, Showcase.TIER2.getZ() - 1.5, -45, 20));
        for (int i = 0; i < SCREEN_CYCLES; i++) {
            add("screen.ask", 10, () -> PacketDistributor.sendToServer(new GrinderMissingRequestPayload(grinder())));
            add("screen.close", 0, () -> Minecraft.getInstance().screen instanceof GrinderMissingScreen, () -> {
                Minecraft.getInstance().setScreen(null);
                screenCycles++;
            });
        }
        add("heap.after", 100, () -> command("spark heapsummary --save-to-file"));
        add("report", 200, ShowcaseSpark::report);
    }

    private static void add(String name, int delay, Runnable action) {
        STEPS.add(new Step(name, delay, () -> true, action));
    }

    private static void add(String name, int delay, BooleanSupplier ready, Runnable action) {
        STEPS.add(new Step(name, delay, ready, action));
    }

    private static void command(String command) {
        Minecraft.getInstance().player.connection.sendCommand(command);
    }

    private static BlockPos grinder() {
        return Minecraft.getInstance().getSingleplayerServer().overworld().getSharedSpawnPos().offset(Showcase.TIER2).above(3);
    }

    private static void teleport(double x, double y, double z, float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            var level = server.overworld();
            BlockPos o = level.getSharedSpawnPos();
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(level, o.getX() + x, o.getY() + y, o.getZ() + z, yaw, pitch);
        });
    }

    // Twenty visitors at once, as the scenario asks: each must be gone once its lifetime is over.
    private static void eatUltimates() {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            for (int i = 0; i < VISITORS; i++) {
                ItemStack food = BSItems.ULTIMATE_FRICADELLE.asStack();
                food.getItem().finishUsingItem(food, player.serverLevel(), player);
            }
        });
    }

    private static void report() {
        Minecraft mc = Minecraft.getInstance();
        List<String> lines = new ArrayList<>(CHAT);
        lines.add("screen cycles: " + screenCycles + " of " + SCREEN_CYCLES);
        try {
            Files.write(mc.gameDirectory.toPath().resolve("spark-report.txt"), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[showcase-spark] could not write the report", e);
        }
        mc.stop();
    }
}
