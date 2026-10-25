/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.uninstall;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Another mod's data saved as a list of item stacks (each a compound with an {@code id}). Entries of
 * a namespace are removed by rebuilding the list without them: the data is reloaded from the shorter
 * list, so no empty stack is left in place of a removed one.
 */
public final class SavedStackLists {
    private SavedStackLists() {
    }

    public static int count(INBTSerializable<ListTag> data, HolderLookup.Provider registries, String namespace) {
        int found = 0;
        for (Tag entry : data.serializeNBT(registries)) {
            if (inNamespace(entry, namespace)) {
                found++;
            }
        }
        return found;
    }

    public static int purge(INBTSerializable<ListTag> data, HolderLookup.Provider registries, String namespace) {
        ListTag kept = new ListTag();
        int removed = 0;
        for (Tag entry : data.serializeNBT(registries)) {
            if (inNamespace(entry, namespace)) {
                removed++;
            } else {
                kept.add(entry);
            }
        }
        if (removed > 0) {
            data.deserializeNBT(registries, kept);
        }
        return removed;
    }

    private static boolean inNamespace(Tag entry, String namespace) {
        return entry instanceof CompoundTag stack && stack.getString("id").startsWith(namespace + ":");
    }
}
