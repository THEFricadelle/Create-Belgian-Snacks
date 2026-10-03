/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.config;

import java.util.List;
import java.util.Set;

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
    private static final ModConfigSpec.ConfigValue<List<? extends String>> GRINDER_BLACKLISTED_MODS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> GRINDER_BLACKLISTED_ITEMS;
    private static final ModConfigSpec.DoubleValue GRINDER_THE_FRICADELLE_RATIO;
    private static final ModConfigSpec.DoubleValue GRINDER_ULTIMATE_RATIO;
    private static final ModConfigSpec.BooleanValue GRINDER_REJECT_DUPLICATES;
    private static final ModConfigSpec.IntValue GRINDER_MIN_SPEED;
    private static final ModConfigSpec.DoubleValue GRINDER_STRESS_IMPACT;
    private static final ModConfigSpec.BooleanValue ULTIMATE_ANNOUNCE;

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

        builder.push("grinder");
        GRINDER_BLACKLISTED_MODS = builder
            .comment("Mod ids whose foods never count for the Supreme Grinder (their items leave the food index).",
                "Default: cosmeticarmoursmod, whose edible items are decorative (D8).")
            .defineListAllowEmpty("blacklistedMods", List.of("cosmeticarmoursmod"), () -> "", o -> o instanceof String);
        GRINDER_BLACKLISTED_ITEMS = builder
            .comment("Item ids that never count as foods, on top of the create_belgian_snacks:grinder/blacklist tag.")
            .defineListAllowEmpty("blacklistedItems", List.of(), () -> "", o -> o instanceof String);
        GRINDER_THE_FRICADELLE_RATIO = builder
            .comment("Share of the food index the Supreme Grinder needs, in THE_ mode, for an Exceptional Paste (D1).",
                "Rounded up: 0.10 of 1804 foods is 181.")
            .defineInRange("theFricadelleRatio", 0.10, 0.0, 1.0);
        GRINDER_ULTIMATE_RATIO = builder
            .comment("Share of the food index needed, in ULTIMATE mode, for an Absolute Paste (D21). Keep 1.0 unless a server needs less.")
            .defineInRange("ultimateRatio", 1.0, 0.0, 1.0);
        GRINDER_REJECT_DUPLICATES = builder
            .comment("true: a food already collected stays on the belt or in the funnel. false: it is destroyed.")
            .define("rejectDuplicates", true);
        GRINDER_MIN_SPEED = builder
            .comment("Slowest rotation, in RPM, at which the Supreme Grinder takes foods (D11).")
            .defineInRange("minSpeed", 64, 1, 256);
        GRINDER_STRESS_IMPACT = builder
            .comment("Stress impact per RPM (D11). 8 is a crushing wheel's.")
            .defineInRange("stressImpact", 8.0, 0.0, 1024.0);
        builder.pop();

        builder.push("ultimate");
        ULTIMATE_ANNOUNCE = builder
            .comment("Tell the whole server in chat when a player eats THE_FRICADELLE.",
                "Operators can also switch it in game: /belgiansnacks announce true|false.")
            .define("announceInChat", true);
        builder.pop();
        SPEC = builder.build();
    }

    private BSConfig() {
    }

    public static boolean announceUltimate() {
        return SPEC.isLoaded() ? ULTIMATE_ANNOUNCE.get() : ULTIMATE_ANNOUNCE.getDefault();
    }

    /** Switches the chat announcement and writes it to the server config file. */
    public static void setAnnounceUltimate(boolean announce) {
        ULTIMATE_ANNOUNCE.set(announce);
        ULTIMATE_ANNOUNCE.save();
    }

    public static int fryerTankCapacity() {
        return SPEC.isLoaded() ? FRYER_TANK_CAPACITY.get() : FRYER_TANK_CAPACITY.getDefault();
    }

    public static double fryerSpeedMultiplier() {
        return SPEC.isLoaded() ? FRYER_SPEED_MULTIPLIER.get() : FRYER_SPEED_MULTIPLIER.getDefault();
    }

    public static Set<String> grinderBlacklistedMods() {
        return Set.copyOf(SPEC.isLoaded() ? GRINDER_BLACKLISTED_MODS.get() : GRINDER_BLACKLISTED_MODS.getDefault());
    }

    public static Set<String> grinderBlacklistedItems() {
        return Set.copyOf(SPEC.isLoaded() ? GRINDER_BLACKLISTED_ITEMS.get() : GRINDER_BLACKLISTED_ITEMS.getDefault());
    }

    public static double grinderTheFricadelleRatio() {
        return SPEC.isLoaded() ? GRINDER_THE_FRICADELLE_RATIO.get() : GRINDER_THE_FRICADELLE_RATIO.getDefault();
    }

    public static double grinderUltimateRatio() {
        return SPEC.isLoaded() ? GRINDER_ULTIMATE_RATIO.get() : GRINDER_ULTIMATE_RATIO.getDefault();
    }

    public static boolean grinderRejectDuplicates() {
        return SPEC.isLoaded() ? GRINDER_REJECT_DUPLICATES.get() : GRINDER_REJECT_DUPLICATES.getDefault();
    }

    public static int grinderMinSpeed() {
        return SPEC.isLoaded() ? GRINDER_MIN_SPEED.get() : GRINDER_MIN_SPEED.getDefault();
    }

    public static double grinderStressImpact() {
        return SPEC.isLoaded() ? GRINDER_STRESS_IMPACT.get() : GRINDER_STRESS_IMPACT.getDefault();
    }

    public static int fryerMaxBatch() {
        return SPEC.isLoaded() ? FRYER_MAX_BATCH.get() : FRYER_MAX_BATCH.getDefault();
    }
}
