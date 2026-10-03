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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Client side only: the fryer's status is already synced to the client block entity. */
public enum FryerStatusProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = BelgianSnacks.asResource("fryer");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof FryerBlockEntity fryer)) {
            return;
        }
        FryerBlockEntity.Status status = fryer.status();
        tooltip.add(status == FryerBlockEntity.Status.FRYING
            ? Component.translatable(status.key(), Math.round(fryer.getProgress() * 100))
            : Component.translatable(status.key()));
        tooltip.add(Component.translatable(FryerBlockEntity.heatKey(fryer.heatBelow())));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
