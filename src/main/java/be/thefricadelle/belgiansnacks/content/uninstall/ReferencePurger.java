/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.uninstall;

import net.minecraft.server.level.ServerPlayer;

/**
 * Data of another mod that keeps our ids in a player and does not survive their disappearance
 * (spec v2, rule R4). Run before the mod is removed: once it is gone, nothing of ours runs anymore.
 */
public interface ReferencePurger {
    /** Short name shown in the scan report, usually the other mod's id. */
    String name();

    /** How many entries of ours this player's data holds. */
    int count(ServerPlayer player);

    /** Removes every entry of ours from this player's data; returns how many went. */
    int purge(ServerPlayer player);
}
