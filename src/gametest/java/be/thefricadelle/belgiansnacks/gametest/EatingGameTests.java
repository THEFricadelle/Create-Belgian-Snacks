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
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.food.UltimateFricadelleItem;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSEntities;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tier effects, THE_FRICADELLE's gags and the advancement tab (decided on 27/09/2026, docs/08).
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EatingGameTests {
    private static final String TEMPLATE = "empty";
    private static final int SECONDS = 20;

    private EatingGameTests() {
    }

    // ------------------------------------------------------------------ effects

    @GameTest(template = TEMPLATE)
    public static void aFricadelleGivesNoEffect(GameTestHelper helper) {
        ServerPlayer player = eat(helper, BSItems.FRICADELLE.asStack());
        helper.assertTrue(player.getActiveEffects().isEmpty(), "effects after a fricadelle: " + player.getActiveEffects());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theFricadelleGivesALightBoost(GameTestHelper helper) {
        ServerPlayer player = eat(helper, BSItems.THE_FRICADELLE.asStack());
        assertEffects(helper, player, Map.of(
            MobEffects.REGENERATION, new int[] {0, 10 * SECONDS},
            MobEffects.ABSORPTION, new int[] {0, 60 * SECONDS}));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void ultimateFricadelleGivesTheBigOne(GameTestHelper helper) {
        ServerPlayer player = eat(helper, BSItems.ULTIMATE_FRICADELLE.asStack());
        assertEffects(helper, player, Map.of(
            MobEffects.REGENERATION, new int[] {1, 60 * SECONDS},
            MobEffects.ABSORPTION, new int[] {3, 180 * SECONDS},
            MobEffects.DAMAGE_BOOST, new int[] {1, 300 * SECONDS},
            MobEffects.DAMAGE_RESISTANCE, new int[] {1, 300 * SECONDS},
            MobEffects.FIRE_RESISTANCE, new int[] {0, 300 * SECONDS}));
        helper.succeed();
    }

    // ------------------------------------------------------------------ THE_FRICADELLE's visitor

    // It runs up from a few blocks ahead, stops beside the eater to say its line, then takes off and
    // bursts in the sky, all within its lifetime (the timeout).
    @GameTest(template = TEMPLATE, timeoutTicks = TheFricadelleNpc.LIFETIME_TICKS + 30)
    public static void theFricadelleRunsUpCongratulatesAndTakesOff(GameTestHelper helper) {
        // Entities tick around the eater, as they do around a real player (simulation distance).
        var level = helper.getLevel();
        net.minecraft.world.level.ChunkPos centre = new net.minecraft.world.level.ChunkPos(helper.absolutePos(BlockPos.ZERO));
        List<net.minecraft.world.level.ChunkPos> forced = new java.util.ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                var chunk = new net.minecraft.world.level.ChunkPos(centre.x + dx, centre.z + dz);
                if (level.setChunkForced(chunk.x, chunk.z, true)) {
                    forced.add(chunk);
                }
            }
        }
        // The forced chunks load first.
        helper.runAfterDelay(20, () -> {
            ServerPlayer player = eat(helper, BSItems.ULTIMATE_FRICADELLE.asStack());
            List<TheFricadelleNpc> visitors = helper.getLevel().getEntitiesOfClass(TheFricadelleNpc.class,
                player.getBoundingBox().inflate(TheFricadelleNpc.RUN_DISTANCE + 2));
            helper.assertValueEqual(visitors.size(), 1, "visitors around the eater");
            TheFricadelleNpc npc = visitors.get(0);
            helper.assertTrue(player.getUUID().equals(npc.getTargetPlayerId()), "the visitor runs to someone else");
            helper.assertValueEqual(npc.getPhase(), TheFricadelleNpc.Phase.RUN, "phase when it appears");
            helper.assertTrue(npc.distanceTo(player) > 3, "the visitor starts beside the eater: nothing to run");
            helper.assertValueEqual(npc.getCustomName().getString(), TheFricadelleNpc.NAME, "the name above the head while running");
            helper.assertFalse(BSEntities.THE_FRICADELLE_NPC.get().canSerialize(), "the visitor could be saved with the chunk");
            helper.assertFalse(npc.hurt(helper.getLevel().damageSources().playerAttack(player), 100), "the visitor took damage");
            helper.assertFalse(npc.isPushable(), "the visitor can be pushed");
            double[] talkedAt = new double[1];
            helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(npc.getPhase(), TheFricadelleNpc.Phase.TALK, "phase"))
                .thenExecute(() -> {
                    double dx = npc.getX() - player.getX();
                    double dz = npc.getZ() - player.getZ();
                    helper.assertTrue(Math.sqrt(dx * dx + dz * dz) <= TheFricadelleNpc.ARRIVED + 0.5, "talks from " + npc.distanceTo(player) + " blocks");
                    helper.assertTrue(npc.isCustomNameVisible(), "the line is not shown above the head");
                    helper.assertTrue(npc.getCustomName() != null && npc.getCustomName().getContents() instanceof TranslatableContents line
                        && line.getKey().startsWith(BelgianSnacks.MOD_ID + ".npc.phrase."), "not one of the lines: " + npc.getCustomName());
                    talkedAt[0] = npc.getY();
                })
                .thenWaitUntil(() -> helper.assertValueEqual(npc.getPhase(), TheFricadelleNpc.Phase.LAUNCH, "phase"))
                .thenWaitUntil(() -> helper.assertTrue(npc.getY() > talkedAt[0] + 10, "still at " + (npc.getY() - talkedAt[0]) + " blocks up"))
                .thenWaitUntil(() -> helper.assertTrue(npc.isRemoved(), "the visitor is still flying"))
                .thenExecute(() -> forced.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false)))
                .thenSucceed();
        });
    }

    // Server operators can silence the chat line, in game or in the server config; the rest of the gag stays.
    @GameTest(template = TEMPLATE, batch = "ultimate_announce")
    public static void operatorsCanTurnOffTheChatAnnouncement(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "eater"));
        boolean before = BSConfig.announceUltimate();
        try {
            helper.assertTrue(BSConfig.announceUltimate(), "announced by default");
            helper.assertTrue(UltimateFricadelleItem.announcement(player) != null, "no line while announcing");
            command(helper, "belgiansnacks announce false");
            helper.assertFalse(BSConfig.announceUltimate(), "the command did not turn it off");
            helper.assertTrue(UltimateFricadelleItem.announcement(player) == null, "a line while turned off");
            command(helper, "belgiansnacks announce true");
            helper.assertTrue(UltimateFricadelleItem.announcement(player) != null, "the command did not turn it back on");
        } finally {
            BSConfig.setAnnounceUltimate(before);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theVisitorHasFiveLinesOneCountingTheFoods(GameTestHelper helper) {
        for (int i = 0; i < TheFricadelleNpc.PHRASES; i++) {
            var contents = (TranslatableContents) TheFricadelleNpc.phrase(i).getContents();
            helper.assertValueEqual(contents.getKey(), BelgianSnacks.MOD_ID + ".npc.phrase." + i, "line " + i);
        }
        var counting = (TranslatableContents) TheFricadelleNpc.phrase(3).getContents();
        helper.assertValueEqual(counting.getArgs().length, 1, "line 3 arguments");
        helper.assertValueEqual(counting.getArgs()[0], FoodIndex.server().size(), "line 3 counts the foods");
        helper.succeed();
    }

    // ------------------------------------------------------------------ advancements

    @GameTest(template = TEMPLATE)
    public static void theAdvancementTabIsComplete(GameTestHelper helper) {
        Map<String, String> parents = Map.of(
            "fryer", "root", "beef_tallow", "fryer", "fricadelle", "fryer", "supreme_grinder", "fricadelle",
            "exceptional_paste", "supreme_grinder", "the_fricadelle", "exceptional_paste", "grinder_half", "supreme_grinder",
            "absolute_paste", "grinder_half", "ultimate_fricadelle", "absolute_paste");
        helper.assertTrue(advancement(helper, "root") != null, "no root advancement");
        parents.forEach((name, parent) -> {
            AdvancementHolder holder = advancement(helper, name);
            helper.assertTrue(holder != null, "advancement " + name + " not loaded");
            helper.assertTrue(holder.value().parent().map(BelgianSnacks.asResource(parent)::equals).orElse(false),
                name + " should follow " + parent);
        });
        helper.succeed();
    }

    // Half of every food of the index, whatever the mode: players within 16 blocks earn it.
    @GameTest(template = TEMPLATE)
    public static void aGrinderAtHalfwayRewardsThePlayersAround(GameTestHelper helper) {
        BlockPos grinderPos = new BlockPos(1, 1, 1);
        helper.setBlock(grinderPos, BSBlocks.SUPREME_GRINDER.getDefaultState());
        helper.setBlock(grinderPos.above(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.DOWN));
        ((CreativeMotorBlockEntity) helper.getBlockEntity(grinderPos.above())).generatedSpeed.setValue(64);
        SupremeGrinderBlockEntity grinder = helper.getBlockEntity(grinderPos);
        grinder.setMode(GrinderMode.ULTIMATE);
        ServerPlayer player = TestPlayers.inLevel(helper, new net.minecraft.world.phys.Vec3(2.5, 1, 2.5));
        AdvancementHolder halfway = advancement(helper, "grinder_half");
        List<ResourceLocation> index = FoodIndex.server().ids();
        int half = GrinderProgress.goal(0.5, index.size());
        grinder.setConsumed(index.subList(0, half - 1));
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertValueEqual(grinder.getCount(), half - 1, "one short of half"))
            .thenExecute(() -> helper.assertFalse(player.getAdvancements().getOrStartProgress(halfway).isDone(), "awarded one food early"))
            .thenExecute(() -> grinder.setConsumed(index.subList(0, half)))
            .thenWaitUntil(() -> helper.assertTrue(player.getAdvancements().getOrStartProgress(halfway).isDone(), "not awarded at half"))
            .thenExecute(() -> TestPlayers.remove(helper, player))
            .thenSucceed();
    }

    // ------------------------------------------------------------------ helpers

    // A fake player: it eats, gets effects and is congratulated, without logging in (the mods of the
    // dev runtime would try to send it their login payloads).
    private static void command(GameTestHelper helper, String command) {
        var server = helper.getLevel().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withLevel(helper.getLevel()).withSuppressedOutput(), command);
    }

    private static ServerPlayer eat(GameTestHelper helper, ItemStack food) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "eater"));
        player.removeAllEffects();
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 1, 2.5)));
        food.getItem().finishUsingItem(food, helper.getLevel(), player);
        return player;
    }

    private static void assertEffects(GameTestHelper helper, ServerPlayer player, Map<Holder<MobEffect>, int[]> expected) {
        helper.assertValueEqual(player.getActiveEffects().size(), expected.size(), "effects: " + player.getActiveEffects());
        expected.forEach((effect, levelAndTicks) -> {
            MobEffectInstance active = player.getEffect(effect);
            helper.assertTrue(active != null, effect.getRegisteredName() + " missing");
            helper.assertValueEqual(active.getAmplifier(), levelAndTicks[0], effect.getRegisteredName() + " amplifier");
            helper.assertValueEqual(active.getDuration(), levelAndTicks[1], effect.getRegisteredName() + " duration");
        });
    }

    private static AdvancementHolder advancement(GameTestHelper helper, String name) {
        return helper.getLevel().getServer().getAdvancements().get(BelgianSnacks.asResource(name));
    }
}
