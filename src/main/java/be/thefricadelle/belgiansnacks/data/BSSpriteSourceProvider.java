/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.tterrag.registrate.util.entry.FluidEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

/**
 * The block atlas only scans textures/block and textures/item, so fluid textures must be listed
 * explicitly or they render as the missing texture. One entry per file, like Create does.
 */
public class BSSpriteSourceProvider extends SpriteSourceProvider {
    public BSSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(output, registries, BelgianSnacks.MOD_ID, fileHelper);
    }

    @Override
    protected void gather() {
        SourceList blocks = atlas(BLOCKS_ATLAS);
        for (FluidEntry<?> fluid : List.of(BSFluids.FRYING_OIL, BSFluids.MELTED_BEEF_TALLOW, BSFluids.MAYONNAISE, BSFluids.CURRY_KETCHUP)) {
            String id = BuiltInRegistries.FLUID.getKey(fluid.getSource()).getPath();
            for (String suffix : List.of("_still", "_flow")) {
                blocks.addSource(new SingleFile(BelgianSnacks.asResource("fluid/" + id + suffix), Optional.empty()));
            }
        }
    }
}
