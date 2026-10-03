/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import org.jetbrains.annotations.Nullable;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * THE_FRICADELLE: whoever eats it is announced to the whole server (unless its operators turned it
 * off), fireworks go off, and THEFricadelle comes to congratulate them (decided on 27/09/2026).
 */
public class UltimateFricadelleItem extends FricadelleItem {
    public UltimateFricadelleItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void eaten(ServerLevel level, LivingEntity eater) {
        level.sendParticles(ParticleTypes.FIREWORK, eater.getX(), eater.getY() + 1.2, eater.getZ(), 60, 0.8, 0.8, 0.8, 0.15);
        level.playSound(null, eater.getX(), eater.getY(), eater.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.5f, 1.0f);
        level.playSound(null, eater.getX(), eater.getY(), eater.getZ(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 1.5f, 1.0f);
        if (eater instanceof ServerPlayer player) {
            Component line = announcement(player);
            if (line != null) {
                level.getServer().getPlayerList().broadcastSystemMessage(line, false);
            }
            TheFricadelleNpc.appear(level, player);
        }
    }

    /** The line told to the whole server, or null when its operators turned it off (BSConfig). */
    @Nullable
    public static Component announcement(ServerPlayer player) {
        return BSConfig.announceUltimate()
            ? Component.translatable(BelgianSnacks.MOD_ID + ".ultimate.eaten", player.getDisplayName()).withStyle(ChatFormatting.GOLD)
            : null;
    }
}
