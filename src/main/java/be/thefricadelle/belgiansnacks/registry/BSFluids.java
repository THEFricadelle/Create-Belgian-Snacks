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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        CURRY_KETCHUP = fluid("curry_ketchup", "Curry Ketchup", MapColor.COLOR_RED),
        // v2 friterie sauces (docs/13, section 5.2), all mayonnaise-based.
        ANDALOUSE = fluid("andalouse", "Andalouse Sauce", MapColor.COLOR_ORANGE),
        SAMOURAI = fluid("samourai", "Samurai Sauce", MapColor.COLOR_RED),
        AMERICAINE = fluid("americaine", "Américaine Sauce", MapColor.COLOR_PINK),
        // v2 base ingredients (section 5.7) and the Liège sauce made from Liège syrup.
        CREAM = fluid("cream", "Cream", MapColor.SNOW),
        BECHAMEL = fluid("bechamel", "Béchamel", MapColor.SAND),
        LIEGE_SYRUP = fluid("liege_syrup", "Liège Syrup", MapColor.TERRACOTTA_BROWN),
        VINEGAR = fluid("vinegar", "Vinegar", MapColor.TERRACOTTA_YELLOW),
        SAUCE_LAPIN = fluid("sauce_lapin", "Liège Sauce", MapColor.COLOR_BROWN),
        // v2 crops (section 5.8): mustard from mustard seeds.
        MUSTARD = fluid("mustard", "Mustard", MapColor.COLOR_YELLOW);

    /** Every fluid of the mod, in registration order. */
    public static final List<FluidEntry<BaseFlowingFluid.Flowing>> ALL =
        List.of(FRYING_OIL, MELTED_BEEF_TALLOW, MAYONNAISE, CURRY_KETCHUP, ANDALOUSE, SAMOURAI, AMERICAINE,
            CREAM, BECHAMEL, LIEGE_SYRUP, VINEGAR, SAUCE_LAPIN, MUSTARD);

    /** The sauces a Spout puts on a cone, by the name their cones carry (BSItems.CONE_SAUCES). */
    public static final Map<String, FluidEntry<BaseFlowingFluid.Flowing>> CONE_SAUCES = orderedSauces();

    private BSFluids() {
    }

    private static Map<String, FluidEntry<BaseFlowingFluid.Flowing>> orderedSauces() {
        Map<String, FluidEntry<BaseFlowingFluid.Flowing>> sauces = new LinkedHashMap<>();
        sauces.put("mayonnaise", MAYONNAISE);
        sauces.put("curry_ketchup", CURRY_KETCHUP);
        sauces.put("andalouse", ANDALOUSE);
        sauces.put("samourai", SAMOURAI);
        sauces.put("americaine", AMERICAINE);
        sauces.put("sauce_lapin", SAUCE_LAPIN);
        return Collections.unmodifiableMap(sauces);
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
        REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> {
            var sauces = prov.addTag(BSTags.SAUCES);
            CONE_SAUCES.values().forEach(sauce -> sauces.add(sauce.get().getSource(), sauce.get()));
        });
    }
}
