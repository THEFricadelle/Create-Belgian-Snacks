/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.processing;

import java.util.List;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Whether a whole batch fits in an output inventory, checked on a copy before the batch starts. */
public final class OutputSpace {
    private OutputSpace() {
    }

    /** Every result of the recipe, times {@code count}, as if all chances succeeded. */
    public static boolean fits(ItemStackHandler output, List<ProcessingOutput> results, int count) {
        ItemStackHandler simulated = new ItemStackHandler(output.getSlots());
        for (int slot = 0; slot < output.getSlots(); slot++) {
            simulated.setStackInSlot(slot, output.getStackInSlot(slot).copy());
        }
        for (ProcessingOutput result : results) {
            ItemStack stack = result.getStack();
            int total = stack.getCount() * count;
            while (total > 0) {
                int part = Math.min(total, stack.getMaxStackSize());
                if (!ItemHandlerHelper.insertItemStacked(simulated, stack.copyWithCount(part), false).isEmpty()) {
                    return false;
                }
                total -= part;
            }
        }
        return true;
    }
}
