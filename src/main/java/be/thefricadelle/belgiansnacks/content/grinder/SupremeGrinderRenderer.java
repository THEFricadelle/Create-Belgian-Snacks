/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import be.thefricadelle.belgiansnacks.client.BSPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** Spins the blades when Flywheel is off; with Flywheel on, SingleAxisRotatingVisual does it. */
public class SupremeGrinderRenderer extends KineticBlockEntityRenderer<SupremeGrinderBlockEntity> {
    public SupremeGrinderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SupremeGrinderBlockEntity be, BlockState state) {
        return CachedBuffers.partial(BSPartialModels.GRINDER_BLADES, state);
    }
}
