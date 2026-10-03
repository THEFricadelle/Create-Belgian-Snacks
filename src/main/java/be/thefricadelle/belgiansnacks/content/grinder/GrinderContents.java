/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** A broken Supreme Grinder's collection and mode, carried by its item (D10). */
public record GrinderContents(List<ResourceLocation> consumed, String mode) {
    public static final Codec<GrinderContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.listOf().fieldOf("consumed").forGetter(GrinderContents::consumed),
        Codec.STRING.fieldOf("mode").forGetter(GrinderContents::mode)
    ).apply(instance, GrinderContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrinderContents> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), GrinderContents::consumed,
        ByteBufCodecs.STRING_UTF8, GrinderContents::mode,
        GrinderContents::new);

    public GrinderContents {
        consumed = List.copyOf(consumed);
    }
}
