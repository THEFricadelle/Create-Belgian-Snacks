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
import net.minecraft.world.item.Items;

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

    // v2 fries (docs/08, provisional): a cone is a meal, fries alone a snack; beef tallow is the better fat.
    public static final FoodProperties FRIES = snack(3, 0.4f);
    public static final FoodProperties FRIES_BEEF_TALLOW = snack(3, 0.6f);
    public static final FoodProperties CONE_OF_FRIES = snack(6, 0.6f);
    public static final FoodProperties CONE_OF_FRIES_BEEF_TALLOW = snack(6, 0.8f);
    // A sauce adds a little (M12, provisional).
    public static final FoodProperties CONE_OF_FRIES_SAUCED = snack(7, 0.7f);
    public static final FoodProperties CONE_OF_FRIES_BEEF_TALLOW_SAUCED = snack(7, 0.9f);

    // v2 base ingredients that can be eaten as they are (M13, provisional).
    public static final FoodProperties HERVE_CHEESE = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f)
        // Its smell, not the cheese.
        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 3 * SECONDS, 0), 1.0f).build();
    public static final FoodProperties PISTOLET = snack(4, 0.5f);
    public static final FoodProperties ARDENNES_HAM = snack(6, 0.8f);
    public static final FoodProperties MERINGUE = snack(2, 0.2f);
    public static final FoodProperties RAISINS = snack(2, 0.3f);
    public static final FoodProperties APPLE_COMPOTE = new FoodProperties.Builder().nutrition(5).saturationModifier(0.5f)
        .usingConvertsTo(Items.BOWL).build();
    public static final FoodProperties VANILLA_ICE_CREAM = snack(4, 0.4f);

    // v2 friterie snacks (M16, provisional): a fried snack is a small meal, a sandwich with fries a full one.
    public static final FoodProperties MITRAILLETTE = snack(14, 1.2f);
    public static final FoodProperties BOULET = snack(6, 0.7f);
    public static final FoodProperties BOULETS_LIEGEOIS = new FoodProperties.Builder().nutrition(12).saturationModifier(1.0f)
        .usingConvertsTo(Items.BOWL).build();
    public static final FoodProperties CROQUETTE = snack(5, 0.6f);
    public static final FoodProperties LUCIFER = snack(6, 0.6f);
    public static final FoodProperties POTATO_CROQUETTE = snack(4, 0.5f);
    public static final FoodProperties SAUSAGE = snack(6, 0.6f);
    public static final FoodProperties FILET_AMERICAIN = snack(5, 0.6f);
    public static final FoodProperties PISTOLET_AMERICAIN = snack(9, 0.9f);
    public static final FoodProperties AMERICAIN_FRITES = snack(12, 1.0f);
    public static final FoodProperties PATTY = snack(6, 0.7f);
    public static final FoodProperties BURGER = snack(10, 0.9f);
    public static final FoodProperties CHEESEBURGER = snack(11, 1.0f);
    public static final FoodProperties HOT_DOG = snack(8, 0.8f);
    public static final FoodProperties BROCHETTE = snack(7, 0.8f);
    public static final FoodProperties NUGGETS = snack(4, 0.5f);
    public static final FoodProperties CHICKEN_CRUNCH = snack(6, 0.7f);

    // v2 Belgian dishes (M17, provisional): a plate with fries is a feast, a stew a meal, a side a snack.
    public static final FoodProperties DISH_WITH_FRIES = snack(14, 1.2f);
    public static final FoodProperties DISH = snack(10, 1.0f);
    public static final FoodProperties SIDE_DISH_LARGE = snack(8, 0.8f);
    public static final FoodProperties SIDE_DISH = snack(6, 0.6f);

    // v2 waffles (M18, provisional): a topping adds a little, chocolate the most.
    public static final FoodProperties LIEGE_WAFFLE = snack(7, 0.7f);
    public static final FoodProperties BRUSSELS_WAFFLE = snack(6, 0.6f);
    public static final FoodProperties BRUSSELS_WAFFLE_SUGARED = snack(7, 0.7f);
    public static final FoodProperties BRUSSELS_WAFFLE_CHOCOLATE = snack(8, 0.8f);
    public static final FoodProperties LACQUEMANT = snack(6, 0.7f);

    /** The same food, eaten from a bowl that comes back. */
    public static FoodProperties inBowl(FoodProperties food) {
        return new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(), food.eatSeconds(),
            java.util.Optional.of(new net.minecraft.world.item.ItemStack(Items.BOWL)), food.effects());
    }

    private static FoodProperties snack(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private BSFoods() {
    }
}
