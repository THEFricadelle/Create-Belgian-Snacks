/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A fricadelle: eaten like any food, then a satisfied burp everyone nearby hears. Effects, messages
 * and advancements are still open (docs/08) and will hook in here.
 */
public class FricadelleItem extends Item {
    public FricadelleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        ItemStack left = super.finishUsingItem(stack, level, eater);
        if (level instanceof ServerLevel server) {
            eaten(server, eater);
        }
        return left;
    }

    /** Server side, once the fricadelle is eaten: the burp every tier shares. */
    protected void eaten(ServerLevel level, LivingEntity eater) {
        burp(level, eater, 1.0f, 0.7f + level.random.nextFloat() * 0.1f);
    }

    protected static void burp(ServerLevel level, LivingEntity eater, float volume, float pitch) {
        // Null player: sent to every nearby player, the eater included.
        level.playSound(null, eater.getX(), eater.getY(), eater.getZ(), BSSoundEvents.FRICADELLE_BURP.get(), SoundSource.PLAYERS, volume, pitch);
    }
}
