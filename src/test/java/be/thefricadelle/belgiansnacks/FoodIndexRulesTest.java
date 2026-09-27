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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import be.thefricadelle.belgiansnacks.content.food.FoodIndexRules;
import be.thefricadelle.belgiansnacks.content.food.FoodIndexRules.Candidate;
import be.thefricadelle.belgiansnacks.content.food.FoodIndexRules.Entry;
import be.thefricadelle.belgiansnacks.content.food.FoodIndexRules.Source;

class FoodIndexRulesTest {
    private static final List<Candidate> ITEMS = List.of(
        new Candidate("minecraft:apple", true),
        new Candidate("minecraft:bread", true),
        new Candidate("minecraft:cake", false),
        new Candidate("minecraft:stone", false),
        new Candidate("minecraft:enchanted_golden_apple", true),
        new Candidate("farmersdelight:tomato", true),
        new Candidate("farmersdelight:pie", false),
        new Candidate("create_belgian_snacks:fricadelle", true));

    @Test
    void foodsPlusExtraMinusBlacklists() {
        var result = FoodIndexRules.compute(ITEMS, Set.of("minecraft:cake"),
            Set.of("minecraft:enchanted_golden_apple", "create_belgian_snacks:fricadelle"), Set.of(), Set.of());
        assertEquals(List.of("farmersdelight:tomato", "minecraft:apple", "minecraft:bread", "minecraft:cake"), ids(result));
        assertEquals(2, result.excluded());
    }

    @Test
    void sourceTellsFoodFromExtra() {
        var result = FoodIndexRules.compute(ITEMS, Set.of("minecraft:cake"), Set.of(), Set.of(), Set.of());
        assertEquals(Source.EXTRA, find(result, "minecraft:cake").source());
        assertEquals(Source.FOOD, find(result, "minecraft:apple").source());
    }

    @Test
    void blacklistedModsAndItemsAreRemoved() {
        var result = FoodIndexRules.compute(ITEMS, Set.of(), Set.of(), Set.of("farmersdelight"), Set.of("minecraft:bread"));
        assertEquals(List.of("create_belgian_snacks:fricadelle", "minecraft:apple", "minecraft:enchanted_golden_apple"), ids(result));
        assertEquals(2, result.excluded());
    }

    @Test
    void blacklistWinsOverExtra() {
        var result = FoodIndexRules.compute(ITEMS, Set.of("minecraft:cake"), Set.of("minecraft:cake"), Set.of(), Set.of());
        assertEquals(false, ids(result).contains("minecraft:cake"));
    }

    @Test
    void nonFoodsOutsideTheExtraTagNeverCount() {
        var result = FoodIndexRules.compute(ITEMS, Set.of(), Set.of(), Set.of(), Set.of());
        assertEquals(false, ids(result).contains("minecraft:stone"));
        assertEquals(false, ids(result).contains("farmersdelight:pie"));
    }

    @Test
    void duplicatesCountOnce() {
        List<Candidate> twice = new ArrayList<>(ITEMS);
        twice.addAll(ITEMS);
        var result = FoodIndexRules.compute(twice, Set.of(), Set.of(), Set.of(), Set.of());
        assertEquals(ids(FoodIndexRules.compute(ITEMS, Set.of(), Set.of(), Set.of(), Set.of())), ids(result));
    }

    @Test
    void orderDoesNotDependOnTheInputOrder() {
        List<Candidate> shuffled = new ArrayList<>(ITEMS);
        Collections.reverse(shuffled);
        Collections.swap(shuffled, 1, 5);
        assertEquals(ids(FoodIndexRules.compute(ITEMS, Set.of("minecraft:cake"), Set.of(), Set.of(), Set.of())),
            ids(FoodIndexRules.compute(shuffled, Set.of("minecraft:cake"), Set.of(), Set.of(), Set.of())));
    }

    private static List<String> ids(FoodIndexRules.Result result) {
        return result.foods().stream().map(Entry::id).toList();
    }

    private static Entry find(FoodIndexRules.Result result, String id) {
        return result.foods().stream().filter(e -> e.id().equals(id)).findFirst().orElseThrow();
    }
}
