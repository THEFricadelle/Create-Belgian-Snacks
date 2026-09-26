/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config, synced to clients: create_belgian_snacks-server.toml in the world's serverconfig.
 * Server configs load with the world, so every getter falls back to the default before that.
 */
public final class BSConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue FRYER_TANK_CAPACITY;
    private static final ModConfigSpec.DoubleValue FRYER_SPEED_MULTIPLIER;
    private static final ModConfigSpec.IntValue FRYER_MAX_BATCH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("fryer");
        FRYER_TANK_CAPACITY = builder
            .comment("Fat tank capacity in mB. Applies to fryers loaded after a change.")
            .defineInRange("tankCapacity", 4000, 100, 64000);
        FRYER_SPEED_MULTIPLIER = builder
            .comment("Divides every frying time. 2.0 fries twice as fast.")
            .defineInRange("speedMultiplier", 1.0, 0.1, 100.0);
        FRYER_MAX_BATCH = builder
            .comment("Items fried together in one batch. Items tagged create_belgian_snacks:fryer/one_at_a_time always fry alone.")
            .defineInRange("maxBatch", 16, 1, 64);
        builder.pop();
        SPEC = builder.build();
    }

    private BSConfig() {
    }

    public static int fryerTankCapacity() {
        return SPEC.isLoaded() ? FRYER_TANK_CAPACITY.get() : FRYER_TANK_CAPACITY.getDefault();
    }

    public static double fryerSpeedMultiplier() {
        return SPEC.isLoaded() ? FRYER_SPEED_MULTIPLIER.get() : FRYER_SPEED_MULTIPLIER.getDefault();
    }

    public static int fryerMaxBatch() {
        return SPEC.isLoaded() ? FRYER_MAX_BATCH.get() : FRYER_MAX_BATCH.getDefault();
    }
}
