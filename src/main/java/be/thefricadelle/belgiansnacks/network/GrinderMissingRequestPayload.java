/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.network;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: a player asks which foods a supreme grinder still misses. */
public record GrinderMissingRequestPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<GrinderMissingRequestPayload> TYPE = new Type<>(BelgianSnacks.asResource("grinder_missing_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GrinderMissingRequestPayload> STREAM_CODEC =
        BlockPos.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().map(GrinderMissingRequestPayload::new, GrinderMissingRequestPayload::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
