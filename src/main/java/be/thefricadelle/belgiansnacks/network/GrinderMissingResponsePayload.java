/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.network;

import java.util.List;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client: the foods a supreme grinder still misses, in index order. About 30 bytes an id,
 * so a pack of 1800 foods sends some 55 KB, far below the 1 MB payload limit; no paging needed.
 */
public record GrinderMissingResponsePayload(BlockPos pos, List<ResourceLocation> missing, int total) implements CustomPacketPayload {
    /** Larger lists are cut here and flagged by the total; no real pack comes close. */
    public static final int MAX_IDS = 20_000;

    public static final Type<GrinderMissingResponsePayload> TYPE = new Type<>(BelgianSnacks.asResource("grinder_missing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GrinderMissingResponsePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, GrinderMissingResponsePayload::pos,
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_IDS)), GrinderMissingResponsePayload::missing,
        ByteBufCodecs.VAR_INT, GrinderMissingResponsePayload::total,
        GrinderMissingResponsePayload::new);

    public GrinderMissingResponsePayload {
        missing = List.copyOf(missing.size() > MAX_IDS ? missing.subList(0, MAX_IDS) : missing);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
