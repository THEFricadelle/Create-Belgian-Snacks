/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import com.tterrag.registrate.util.entry.FluidEntry;

import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSLang;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BSFrenchLangProvider extends LanguageProvider {
    public BSFrenchLangProvider(PackOutput output) {
        super(output, BelgianSnacks.MOD_ID, "fr_fr");
    }

    @Override
    protected void addTranslations() {
        add(BSCreativeTabs.MAIN_TITLE, "Create: Belgian Snacks");

        add(BSItems.MINCED_PORK.get(), "Hachis de porc");
        add(BSItems.MINCED_BEEF.get(), "Hachis de bœuf");
        add(BSItems.MINCED_CHICKEN.get(), "Hachis de poulet");
        add(BSItems.BEEF_TALLOW.get(), "Blanc de bœuf");
        add(BSItems.BREAD_CRUMBS.get(), "Chapelure");
        add(BSItems.FRICADELLE_PASTE.get(), "Pâte à fricadelle");
        add(BSItems.RAW_FRICADELLE.get(), "Fricadelle crue");
        // The three fricadelle names are a brand and stay identical in every language.
        add(BSItems.FRICADELLE.get(), "Fricadelle");

        add(BSItems.BELGIAN_SPICES.get(), "Épices belges");
        add(BSItems.EXCEPTIONAL_PASTE.get(), "Pâte d'exception");
        add(BSItems.INCOMPLETE_THE_FRICADELLE.get(), "THE_Fricadelle en cours");
        add(BSItems.RAW_THE_FRICADELLE.get(), "THE_Fricadelle crue");
        add(BSItems.THE_FRICADELLE.get(), "THE_Fricadelle");

        add(BSItems.ABSOLUTE_PASTE.get(), "Pâte absolue");
        add(BSItems.RAW_ULTIMATE_FRICADELLE.get(), "THE_FRICADELLE crue");
        add(BSItems.ULTIMATE_FRICADELLE.get(), "THE_FRICADELLE");

        fluid(BSFluids.FRYING_OIL, "Huile de friture", "Seau d'huile de friture");
        fluid(BSFluids.MELTED_BEEF_TALLOW, "Blanc de bœuf fondu", "Seau de blanc de bœuf fondu");
        fluid(BSFluids.MAYONNAISE, "Mayonnaise", "Seau de mayonnaise");
        fluid(BSFluids.CURRY_KETCHUP, "Curry ketchup", "Seau de curry ketchup");

        add(BSBlocks.FRYER.get(), "Friteuse");
        add(BSBlocks.SUPREME_GRINDER.get(), "Hachoir Suprême");
        BSLang.ENTRIES.forEach((key, entry) -> add(key, entry.french()));

        add(BSItems.tooltipKey("fricadelle"), "Personne ne sait vraiment ce qu'il y a dedans.");
        add(BSItems.tooltipKey("the_fricadelle"), "Avec une spéciale, s'il vous plaît.");
        add(BSItems.tooltipKey("ultimate_fricadelle"), "Elle contient littéralement tout.");
    }

    // A fluid shows up under three keys: the fluid type, its placed block and its bucket.
    private void fluid(FluidEntry<?> entry, String name, String bucketName) {
        add(entry.getType().getDescriptionId(), name);
        // FluidEntry.getBlock() resolves to empty during datagen, so the key is built from the source id.
        String id = BuiltInRegistries.FLUID.getKey(entry.getSource()).getPath();
        add("block." + BelgianSnacks.MOD_ID + "." + id, name);
        entry.getBucket().ifPresent(bucket -> add((Item) bucket, bucketName));
    }
}
