/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A fricadelle: eaten like any food; its effects come from BSFoods, and THE_FRICADELLE hooks its gags
 * into {@link #eaten}. (The burp was removed on 27/09/2026.)
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

    /** Server side, once the fricadelle is eaten: nothing more for tiers 1 and 2 (THE_FRICADELLE adds its gags). */
    protected void eaten(ServerLevel level, LivingEntity eater) {
    }
}
