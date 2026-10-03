/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.network;

import java.util.List;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: the food index, as sorted item ids (a few tens of KB for a large pack). */
public record FoodIndexSyncPayload(List<ResourceLocation> ids) implements CustomPacketPayload {
    public static final Type<FoodIndexSyncPayload> TYPE = new Type<>(BelgianSnacks.asResource("food_index"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodIndexSyncPayload> STREAM_CODEC =
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).<RegistryFriendlyByteBuf>cast()
            .map(FoodIndexSyncPayload::new, FoodIndexSyncPayload::ids);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
