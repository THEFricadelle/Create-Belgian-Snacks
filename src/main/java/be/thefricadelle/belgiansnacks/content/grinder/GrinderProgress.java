/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import java.util.Collection;
import java.util.Set;

/**
 * Supreme Grinder rules, generic over the id type and free of Minecraft classes so JUnit can test them.
 */
public final class GrinderProgress {
    public enum Decision {
        /** A food not collected yet: one item is consumed and recorded. */
        NEW,
        /** Already collected and duplicates are rejected: the item stays where it is. */
        DUPLICATE_REJECT,
        /** Already collected and duplicates are not rejected: the item is destroyed, nothing recorded. */
        DUPLICATE_DESTROY,
        /** Not in the food index: never taken. */
        NOT_FOOD;

        public boolean takesItem() {
            return this == NEW || this == DUPLICATE_DESTROY;
        }
    }

    private GrinderProgress() {
    }

    public static <T> Decision decide(T id, boolean isFood, Set<T> consumed, boolean rejectDuplicates) {
        if (!isFood) {
            return Decision.NOT_FOOD;
        }
        if (!consumed.contains(id)) {
            return Decision.NEW;
        }
        return rejectDuplicates ? Decision.DUPLICATE_REJECT : Decision.DUPLICATE_DESTROY;
    }

    /** Foods needed for a paste: at least {@code ratio} of the index, rounded up; 0 when the index is empty. */
    public static int goal(double ratio, int total) {
        if (total <= 0) {
            return 0;
        }
        double clamped = Math.max(0, Math.min(1, ratio));
        // The epsilon keeps 0.1 * 1800 at 180 instead of 181 from floating point noise.
        return Math.max(1, Math.min(total, (int) Math.ceil(clamped * total - 1e-9)));
    }

    /** Collected foods still in the index: ids of removed mods or newly blacklisted items do not count. */
    public static <T> int count(Collection<T> consumed, Set<T> index) {
        int count = 0;
        for (T id : consumed) {
            if (index.contains(id)) {
                count++;
            }
        }
        return count;
    }

    public static boolean reached(int count, int goal) {
        return goal > 0 && count >= goal;
    }
}
