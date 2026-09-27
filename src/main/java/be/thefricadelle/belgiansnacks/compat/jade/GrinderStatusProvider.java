/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.jade;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Client side only: count, goal and mode are already synced to the client block entity. */
public enum GrinderStatusProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = BelgianSnacks.asResource("supreme_grinder");
    private static final String KEY = BelgianSnacks.MOD_ID + ".grinder.goggles.";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof SupremeGrinderBlockEntity grinder)) {
            return;
        }
        tooltip.add(Component.translatable(KEY + "mode", Component.translatable(grinder.getMode().getTranslationKey())));
        tooltip.add(Component.translatable(KEY + "progress", grinder.getCount(), grinder.getGoal(), grinder.percent()));
        if (!grinder.isFastEnough()) {
            tooltip.add(Component.translatable(KEY + "too_slow", BSConfig.grinderMinSpeed()).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
