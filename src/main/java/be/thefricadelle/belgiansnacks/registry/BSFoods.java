/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

// Provisional values: nutrition, saturation and effects are still open (docs/08-decisions-ouvertes.md).
public final class BSFoods {
    private static final int SECONDS = 20;

    public static final FoodProperties FRICADELLE = new FoodProperties.Builder()
        .nutrition(6)
        .saturationModifier(0.6f)
        .build();

    // Effects decided on 27/09/2026 (docs/08). Durations in ticks, amplifier 0 is level I.
    public static final FoodProperties THE_FRICADELLE = new FoodProperties.Builder()
        .nutrition(10)
        .saturationModifier(1.0f)
        .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 10 * SECONDS, 0), 1.0f)
        .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 60 * SECONDS, 0), 1.0f)
        .build();

    public static final FoodProperties ULTIMATE_FRICADELLE = new FoodProperties.Builder()
        .nutrition(20)
        .saturationModifier(2.0f)
        .alwaysEdible()
        .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 60 * SECONDS, 1), 1.0f)
        .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 180 * SECONDS, 3), 1.0f)
        .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300 * SECONDS, 1), 1.0f)
        .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300 * SECONDS, 1), 1.0f)
        // It comes out of the fryer.
        .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300 * SECONDS, 0), 1.0f)
        .build();

    private BSFoods() {
    }
}
