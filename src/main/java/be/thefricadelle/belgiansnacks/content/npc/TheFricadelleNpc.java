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
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THEFricadelle in person: appears with a chorus fruit's sound, runs up to whoever eats
 * THE_FRICADELLE, claps and says its line (aloud and above its head), then shoots up into
 * the sky like a rocket and bursts into fireworks. Purely visual: the
 * burst breaks nothing and hurts no one. Never saved (its entity type is noSave), so it cannot
 * outlive a restart; invulnerable, no collision, nothing to loot.
 */
public class TheFricadelleNpc extends PathfinderMob {
    /** The author's name, shown in chat and above the head. */
    public static final String NAME = "THEFricadelle";
    /** The author's Minecraft account, whose skin the client draws (not NAME: another player owns that one). */
    public static final String SKIN_ACCOUNT = "THE_Fricadelle";
    /** How far ahead of the eater it starts running, when the ground allows. */
    public static final double RUN_DISTANCE = 8;
    /** Within this horizontal distance of the eater, it stops and talks. */
    public static final double ARRIVED = 2.5;
    /** Longest run: a visitor that cannot reach the eater pops up beside them instead. */
    public static final int RUN_TICKS = 60;
    public static final int TALK_TICKS = 70;
    public static final int LAUNCH_TICKS = 25;
    /** Hard cap, above the three phases together. */
    public static final int LIFETIME_TICKS = 160;
    public static final int PHRASES = 5;
    /** The only line with a recorded voice so far: the visitor always says it. */
    public static final int SPOKEN_PHRASE = 4;
    private static final String PHRASE_KEY = BelgianSnacks.MOD_ID + ".npc.phrase.";
    private static final int[] TURNS = {0, 25, -25, 50, -50};
    private static final int[] HEIGHTS = {0, 1, -1, 2, -2, -3};
    private static final EntityDataAccessor<Byte> PHASE = SynchedEntityData.defineId(TheFricadelleNpc.class, EntityDataSerializers.BYTE);

    public enum Phase {
        RUN, TALK, LAUNCH
    }

    @Nullable
    private UUID target;
    // The eater itself: a fake player (a Deployer, a test) is not in the level's player list.
    @Nullable
    private Player eater;
    private int phaseTicks;

    public TheFricadelleNpc(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setInvulnerable(true);
        setSilent(true);
    }

    public static AttributeSupplier.Builder attributes() {
        // Faster than a sprinting player: a visitor in a hurry.
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.4);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, (byte) Phase.RUN.ordinal());
    }

    /** Spawns the NPC a few blocks ahead of the eater, running towards them. */
    public static TheFricadelleNpc appear(ServerLevel level, ServerPlayer eater) {
        TheFricadelleNpc npc = BSEntities.THE_FRICADELLE_NPC.create(level);
        npc.target = eater.getUUID();
        npc.eater = eater;
        npc.placeAhead(level, eater);
        npc.faceTarget(eater);
        npc.setCustomName(Component.literal(NAME));
        npc.setCustomNameVisible(true);
        npc.setSprinting(true);
        level.addFreshEntity(npc);
        npc.poof();
        level.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.NEUTRAL, 1.5f, 1f);
        return npc;
    }

    public static Component phrase(int index) {
        // Line 3 counts the foods that went into the paste.
        return index == 3
            ? Component.translatable(PHRASE_KEY + index, FoodIndex.server().size())
            : Component.translatable(PHRASE_KEY + index);
    }

    // The farthest standable spot ahead of the eater, in their field of view; beside them if none.
    private void placeAhead(ServerLevel level, Player player) {
        Vec3 ahead = ahead(player);
        for (double distance = RUN_DISTANCE; distance >= 3; distance--) {
            for (int turn : TURNS) {
                Vec3 spot = player.position().add(ahead.yRot(turn * Mth.DEG_TO_RAD).scale(distance));
                for (int dy : HEIGHTS) {
                    BlockPos feet = BlockPos.containing(spot.x, player.getY() + dy, spot.z);
                    moveTo(spot.x, feet.getY(), spot.z, 0, 0);
                    if (standable(level, feet)) {
                        return;
                    }
                }
            }
        }
        placeBeside(player);
    }

    private boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos ground = feet.below();
        // Only where entities tick: elsewhere it would stand frozen until its chunk unloads.
        return level.isPositionEntityTicking(feet) && level.noCollision(this) && level.getFluidState(feet).isEmpty()
            && !level.getBlockState(ground).getCollisionShape(level, ground).isEmpty();
    }

    private void placeBeside(Player player) {
        Vec3 spot = player.position().add(ahead(player).scale(1.5));
        moveTo(spot.x, player.getY(), spot.z, getYRot(), 0);
        // A wall in the way: stand where the eater stands rather than inside a block.
        if (!level().noCollision(this)) {
            moveTo(player.getX(), player.getY(), player.getZ(), getYRot(), 0);
        }
    }

    private static Vec3 ahead(Player player) {
        Vec3 look = player.getLookAngle().multiply(1, 0, 1);
        return look.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : look.normalize();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (tickCount >= LIFETIME_TICKS) {
            burst();
            discard();
            return;
        }
        phaseTicks++;
        Player player = targetPlayer();
        switch (getPhase()) {
            case RUN -> run(player);
            case TALK -> {
                faceTarget(player);
                if (phaseTicks >= TALK_TICKS) {
                    launch();
                }
            }
            case LAUNCH -> rise();
        }
    }

    private void run(@Nullable Player player) {
        if (player == null) {
            arrive(null);
            return;
        }
        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        if (dx * dx + dz * dz <= ARRIVED * ARRIVED && Math.abs(player.getY() - getY()) < 3) {
            arrive(player);
            return;
        }
        if (phaseTicks >= RUN_TICKS) {
            // No way through: pop up beside the eater rather than talk from afar.
            poof();
            placeBeside(player);
            poof();
            arrive(player);
            return;
        }
        if (phaseTicks % 10 == 1) {
            getNavigation().moveTo(player, 1.0);
        }
        getLookControl().setLookAt(player, 30, 30);
    }

    private void arrive(@Nullable Player player) {
        getNavigation().stop();
        setSprinting(false);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        setPhase(Phase.TALK);
        faceTarget(player);
        // Said aloud and written above the head; nothing in the chat.
        setCustomName(phrase(SPOKEN_PHRASE));
        level().playSound(null, getX(), getY(), getZ(), BSSoundEvents.NPC_VOICE.get(), SoundSource.NEUTRAL, 1.5f, 1f);
    }

    private void launch() {
        setPhase(Phase.LAUNCH);
        getNavigation().stop();
        setNoGravity(true);
        // Straight up through anything above: nothing stops a rocket.
        noPhysics = true;
        poof();
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 2f, 1f);
    }

    private void rise() {
        setDeltaMovement(0, Math.min(1.6, 0.2 + 0.08 * phaseTicks), 0);
        hasImpulse = true;
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FIREWORK, getX(), getY(), getZ(), 3, 0.1, 0, 0.1, 0.02);
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 2, 0.1, 0, 0.1, 0.01);
        }
        if (phaseTicks >= LAUNCH_TICKS) {
            burst();
            discard();
        }
    }

    // Particles and sound only: no Explosion, so no block is broken and no one is hurt.
    private void burst() {
        if (level() instanceof ServerLevel server) {
            double y = getY() + 1;
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), y, getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.FIREWORK, getX(), y, getZ(), 80, 0.5, 0.5, 0.5, 0.25);
            server.playSound(null, getX(), y, getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 4f, 1f);
            server.playSound(null, getX(), y, getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.NEUTRAL, 4f, 1f);
            server.playSound(null, getX(), y, getZ(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 4f, 1f);
        }
    }

    @Nullable
    private Player targetPlayer() {
        if (eater != null && !eater.isRemoved() && eater.level() == level()) {
            return eater;
        }
        return target == null ? null : level().getPlayerByUUID(target);
    }

    private void faceTarget(@Nullable Player player) {
        if (player == null) {
            return;
        }
        getLookControl().setLookAt(player, 30, 30);
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

    public Phase getPhase() {
        return Phase.values()[Mth.clamp(entityData.get(PHASE), 0, Phase.values().length - 1)];
    }

    private void setPhase(Phase phase) {
        entityData.set(PHASE, (byte) phase.ordinal());
        phaseTicks = 0;
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
