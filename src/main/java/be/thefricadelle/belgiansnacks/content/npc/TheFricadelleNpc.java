/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.npc;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.registry.BSEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THEFricadelle in person: appears beside whoever eats THE_FRICADELLE, looks at them, claps, says
 * one of a few lines, and leaves after a few seconds. Never saved (its entity type is noSave), so it
 * cannot outlive a restart; invulnerable, no collision, nothing to loot.
 */
public class TheFricadelleNpc extends PathfinderMob {
    /** The author's name, shown in chat and above the head. */
    public static final String NAME = "THEFricadelle";
    /** The author's Minecraft account, whose skin the client draws (not NAME: another player owns that one). */
    public static final String SKIN_ACCOUNT = "THE_Fricadelle";
    public static final int LIFETIME_TICKS = 160;
    public static final int PHRASES = 5;
    private static final String PHRASE_KEY = BelgianSnacks.MOD_ID + ".npc.phrase.";
    private static final double CHAT_RANGE = 32;

    @Nullable
    private UUID target;

    public TheFricadelleNpc(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setInvulnerable(true);
        setSilent(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes();
    }

    /** Spawns the NPC beside the eater, facing them, and has it say one of its lines. */
    public static TheFricadelleNpc appear(ServerLevel level, ServerPlayer eater) {
        TheFricadelleNpc npc = BSEntities.THE_FRICADELLE_NPC.create(level);
        Vec3 look = eater.getLookAngle().multiply(1, 0, 1);
        Vec3 ahead = look.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : look.normalize();
        Vec3 spot = eater.position().add(ahead.scale(1.5));
        npc.moveTo(spot.x, eater.getY(), spot.z, 0, 0);
        // A wall in the way: stand where the eater stands rather than inside a block.
        if (!level.noCollision(npc)) {
            npc.moveTo(eater.getX(), eater.getY(), eater.getZ(), 0, 0);
        }
        npc.target = eater.getUUID();
        npc.faceTarget();
        Component phrase = phrase(level.random.nextInt(PHRASES));
        npc.setCustomName(phrase);
        npc.setCustomNameVisible(true);
        level.addFreshEntity(npc);
        npc.poof();
        Component line = Component.translatable("chat.type.text", Component.literal(NAME), phrase);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(npc) <= CHAT_RANGE * CHAT_RANGE) {
                player.sendSystemMessage(line);
            }
        }
        return npc;
    }

    public static Component phrase(int index) {
        // Line 3 counts the foods that went into the paste.
        return index == 3
            ? Component.translatable(PHRASE_KEY + index, FoodIndex.server().size())
            : Component.translatable(PHRASE_KEY + index);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (tickCount >= LIFETIME_TICKS) {
            poof();
            discard();
            return;
        }
        faceTarget();
    }

    private void faceTarget() {
        Player player = target == null ? null : level().getPlayerByUUID(target);
        if (player == null) {
            return;
        }
        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        double dy = player.getEyeY() - getEyeY();
        setXRot((float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG));
    }

    private void poof() {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 20, 0.3, 0.5, 0.3, 0.02);
        }
    }

    @Nullable
    public UUID getTargetPlayerId() {
        return target;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }
}
