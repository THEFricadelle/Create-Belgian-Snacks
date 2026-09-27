/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress.Decision;

class GrinderProgressTest {
    private static final Set<String> CONSUMED = Set.of("minecraft:apple");

    @Test
    void decisions() {
        assertEquals(Decision.NEW, GrinderProgress.decide("minecraft:bread", true, CONSUMED, true));
        assertEquals(Decision.DUPLICATE_REJECT, GrinderProgress.decide("minecraft:apple", true, CONSUMED, true));
        assertEquals(Decision.DUPLICATE_DESTROY, GrinderProgress.decide("minecraft:apple", true, CONSUMED, false));
        assertEquals(Decision.NOT_FOOD, GrinderProgress.decide("minecraft:stone", false, CONSUMED, true));
        // A non-food is refused even when duplicates are destroyed.
        assertEquals(Decision.NOT_FOOD, GrinderProgress.decide("minecraft:stone", false, CONSUMED, false));
    }

    @Test
    void onlyNewFoodsAndDestroyedDuplicatesTakeTheItem() {
        assertTrue(Decision.NEW.takesItem());
        assertTrue(Decision.DUPLICATE_DESTROY.takesItem());
        assertFalse(Decision.DUPLICATE_REJECT.takesItem());
        assertFalse(Decision.NOT_FOOD.takesItem());
    }

    @Test
    void goalRoundsUp() {
        // Arcadia 2.0.32: 10 % of 1804 is 180.4, so 181 foods.
        assertEquals(181, GrinderProgress.goal(0.10, 1804));
        assertEquals(180, GrinderProgress.goal(0.10, 1800));
        assertEquals(1804, GrinderProgress.goal(1.0, 1804));
        assertEquals(5, GrinderProgress.goal(0.10, 46));
        assertEquals(1, GrinderProgress.goal(0.0, 46));
        assertEquals(46, GrinderProgress.goal(2.0, 46));
    }

    @Test
    void emptyIndexNeverProduces() {
        assertEquals(0, GrinderProgress.goal(0.10, 0));
        assertFalse(GrinderProgress.reached(0, 0));
        assertFalse(GrinderProgress.reached(5, 0));
    }

    @Test
    void reachedAtOrAboveTheGoal() {
        assertFalse(GrinderProgress.reached(180, 181));
        assertTrue(GrinderProgress.reached(181, 181));
        assertTrue(GrinderProgress.reached(900, 181));
    }

    @Test
    void countIsTheIntersectionWithTheIndex() {
        List<String> consumed = List.of("minecraft:apple", "minecraft:bread", "ghostmod:food");
        assertEquals(2, GrinderProgress.count(consumed, Set.of("minecraft:apple", "minecraft:bread", "minecraft:cake")));
        assertEquals(0, GrinderProgress.count(consumed, Set.of()));
        // The mod comes back: its food counts again.
        assertEquals(3, GrinderProgress.count(consumed, Set.of("minecraft:apple", "minecraft:bread", "ghostmod:food")));
    }
}
