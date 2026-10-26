/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.jade;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.waffle.WaffleIronBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Status and heat of the waffle iron, from the client's synced block entity. */
public enum WaffleIronStatusProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = BelgianSnacks.asResource("waffle_iron");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof WaffleIronBlockEntity iron)) {
            return;
        }
        WaffleIronBlockEntity.Status status = iron.status();
        tooltip.add(status == WaffleIronBlockEntity.Status.BAKING
            ? Component.translatable(status.key(), Math.round(iron.getProgress() * 100))
            : Component.translatable(status.key()));
        tooltip.add(Component.translatable(FryerBlockEntity.heatKey(iron.heatBelow())));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
