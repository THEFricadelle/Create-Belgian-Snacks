/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import be.thefricadelle.belgiansnacks.client.BSPartialModels;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.fryer.FryerRenderer;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderRenderer;

public final class BSBlockEntities {
    // Registrate only touches the renderer and visual suppliers on the client.
    public static final BlockEntityEntry<FryerBlockEntity> FRYER = REGISTRATE
        .blockEntity("fryer", FryerBlockEntity::new)
        .validBlocks(BSBlocks.FRYER)
        .renderer(() -> FryerRenderer::new)
        .register();

    public static final BlockEntityEntry<SupremeGrinderBlockEntity> SUPREME_GRINDER = REGISTRATE
        .blockEntity("supreme_grinder", SupremeGrinderBlockEntity::new)
        .visual(() -> SingleAxisRotatingVisual.of(BSPartialModels.GRINDER_BLADES), false)
        .validBlocks(BSBlocks.SUPREME_GRINDER)
        .renderer(() -> SupremeGrinderRenderer::new)
        .register();

    private BSBlockEntities() {
    }

    public static void register() {
        // Loads the class so every entry above is created during mod construction.
    }
}
