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
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
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

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void theFricadelleComesToCongratulateAndLeaves(GameTestHelper helper) {
        ServerPlayer player = eat(helper, BSItems.ULTIMATE_FRICADELLE.asStack());
        List<TheFricadelleNpc> visitors = helper.getLevel().getEntitiesOfClass(TheFricadelleNpc.class, player.getBoundingBox().inflate(4));
        helper.assertValueEqual(visitors.size(), 1, "visitors beside the eater");
        TheFricadelleNpc npc = visitors.get(0);
        helper.assertTrue(player.getUUID().equals(npc.getTargetPlayerId()), "the visitor looks at someone else");
        helper.assertTrue(npc.isCustomNameVisible(), "the line is not shown above the head");
        helper.assertTrue(npc.getCustomName() != null && npc.getCustomName().getContents() instanceof TranslatableContents line
            && line.getKey().startsWith(BelgianSnacks.MOD_ID + ".npc.phrase."), "not one of the lines: " + npc.getCustomName());
        helper.assertFalse(BSEntities.THE_FRICADELLE_NPC.get().canSerialize(), "the visitor could be saved with the chunk");
        helper.assertFalse(npc.hurt(helper.getLevel().damageSources().playerAttack(player), 100), "the visitor took damage");
        helper.assertFalse(npc.isPushable(), "the visitor can be pushed");
        helper.runAfterDelay(TheFricadelleNpc.LIFETIME_TICKS + 5, () -> {
            helper.assertTrue(npc.isRemoved(), "the visitor stayed past " + TheFricadelleNpc.LIFETIME_TICKS + " ticks");
            helper.succeed();
        });
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
