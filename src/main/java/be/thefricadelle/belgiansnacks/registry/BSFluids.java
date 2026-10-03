/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public final class BSFluids {
    // Frying fat for tiers 1 and 2; any oil of the frying_oils tag also works.
    public static final FluidEntry<BaseFlowingFluid.Flowing>
        FRYING_OIL = fluid("frying_oil", "Frying Oil", MapColor.COLOR_YELLOW),
        // Only made from beef tallow; the one fat THE_FRICADELLE accepts.
        MELTED_BEEF_TALLOW = fluid("melted_beef_tallow", "Melted Beef Tallow", MapColor.SAND),
        MAYONNAISE = fluid("mayonnaise", "Mayonnaise", MapColor.SAND),
        CURRY_KETCHUP = fluid("curry_ketchup", "Curry Ketchup", MapColor.COLOR_RED);

    private BSFluids() {
    }

    private static FluidEntry<BaseFlowingFluid.Flowing> fluid(String name, String englishName, MapColor mapColor) {
        return REGISTRATE.standardFluid(name)
            .lang(englishName)
            .properties(p -> p.viscosity(1500).density(1200))
            .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(20).slopeFindDistance(3).explosionResistance(100f))
            // Same workaround as Create's own fluids: Registrate's default source is not a BaseFlowingFluid.Source.
            .source(BaseFlowingFluid.Source::new)
            .block()
            .properties(p -> p.mapColor(mapColor))
            .build()
            .bucket()
            .tag(Tags.Items.BUCKETS)
            .build()
            .register();
    }

    public static void register() {
        REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.addTag(BSTags.FRYING_OILS)
            .add(FRYING_OIL.get().getSource(), FRYING_OIL.get(), MELTED_BEEF_TALLOW.get().getSource(), MELTED_BEEF_TALLOW.get())
            .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "plantoil"))
            .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "vegetable_oil")));
    }
}
