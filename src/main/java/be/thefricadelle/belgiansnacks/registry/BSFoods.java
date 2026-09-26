/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import net.minecraft.world.food.FoodProperties;

// Provisional values: nutrition, saturation and effects are still open (docs/08-decisions-ouvertes.md).
public final class BSFoods {
    public static final FoodProperties FRICADELLE = new FoodProperties.Builder()
        .nutrition(6)
        .saturationModifier(0.6f)
        .build();

    public static final FoodProperties THE_FRICADELLE = new FoodProperties.Builder()
        .nutrition(10)
        .saturationModifier(1.0f)
        .build();

    public static final FoodProperties ULTIMATE_FRICADELLE = new FoodProperties.Builder()
        .nutrition(20)
        .saturationModifier(2.0f)
        .alwaysEdible()
        .build();

    private BSFoods() {
    }
}
