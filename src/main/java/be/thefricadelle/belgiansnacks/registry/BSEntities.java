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

import com.tterrag.registrate.util.entry.EntityEntry;

import be.thefricadelle.belgiansnacks.client.TheFricadelleNpcRenderer;
import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import net.minecraft.world.entity.MobCategory;

public final class BSEntities {
    // noSave: never written to a chunk, so it cannot outlive a restart. Registrate only touches the
    // renderer supplier on the client.
    public static final EntityEntry<TheFricadelleNpc> THE_FRICADELLE_NPC = REGISTRATE
        .entity("thefricadelle", TheFricadelleNpc::new, MobCategory.MISC)
        .properties(b -> b.sized(0.6f, 1.8f).clientTrackingRange(10).noSave())
        .attributes(TheFricadelleNpc::attributes)
        .renderer(() -> TheFricadelleNpcRenderer::new)
        .lang(TheFricadelleNpc.NAME)
        .register();

    private BSEntities() {
    }

    public static void register() {
        // Loads the class so every entry above is created during mod construction.
    }
}
