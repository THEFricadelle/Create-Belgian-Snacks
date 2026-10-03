/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.food;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Which items count as foods, as plain data: no Minecraft class, so it runs in plain JUnit.
 * <p>
 * foods = items with a food component + the extra tag - the blacklist tag - blacklisted mods -
 * blacklisted items. Ids are "namespace:path" strings; the result is sorted by id, so every side
 * and every run gets the same order whatever the registry iteration order.
 */
public final class FoodIndexRules {
    public enum Source {
        FOOD, EXTRA
    }

    public record Candidate(String id, boolean food) {
        public String modId() {
            return id.substring(0, id.indexOf(':'));
        }
    }

    public record Entry(String id, Source source) {
    }

    public record Result(List<Entry> foods, int excluded) {
    }

    private FoodIndexRules() {
    }

    public static Result compute(Collection<Candidate> candidates, Set<String> extra, Set<String> blacklistTag,
                                 Set<String> blacklistedMods, Set<String> blacklistedItems) {
        List<Entry> foods = new ArrayList<>();
        int excluded = 0;
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Candidate candidate : candidates) {
            String id = candidate.id();
            boolean isFood = candidate.food();
            boolean isExtra = extra.contains(id);
            if ((!isFood && !isExtra) || !seen.add(id)) {
                continue;
            }
            // The blacklist wins over the extra tag: an explicit exclusion is the stronger intent.
            if (blacklistTag.contains(id) || blacklistedMods.contains(candidate.modId()) || blacklistedItems.contains(id)) {
                excluded++;
                continue;
            }
            foods.add(new Entry(id, isFood ? Source.FOOD : Source.EXTRA));
        }
        foods.sort(Comparator.comparing(Entry::id));
        return new Result(List.copyOf(foods), excluded);
    }
}
