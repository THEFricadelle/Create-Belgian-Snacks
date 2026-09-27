/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
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
 * THE_FRICADELLE: whoever eats it is announced to the whole server, fireworks go off, the burp
 * shakes the walls, and THEFricadelle comes to congratulate them (decided on 27/09/2026).
 */
public class UltimateFricadelleItem extends FricadelleItem {
    public UltimateFricadelleItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void eaten(ServerLevel level, LivingEntity eater) {
        burp(level, eater, 2.0f, 0.4f);
        level.sendParticles(ParticleTypes.FIREWORK, eater.getX(), eater.getY() + 1.2, eater.getZ(), 60, 0.8, 0.8, 0.8, 0.15);
        level.playSound(null, eater.getX(), eater.getY(), eater.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.5f, 1.0f);
        level.playSound(null, eater.getX(), eater.getY(), eater.getZ(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 1.5f, 1.0f);
        if (eater instanceof ServerPlayer player) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(BelgianSnacks.MOD_ID + ".ultimate.eaten", player.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            TheFricadelleNpc.appear(level, player);
        }
    }
}
