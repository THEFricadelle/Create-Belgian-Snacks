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
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BSFrenchLangProvider extends LanguageProvider {
    public BSFrenchLangProvider(PackOutput output) {
        super(output, BelgianSnacks.MOD_ID, "fr_fr");
    }

    @Override
    protected void addTranslations() {
        add(BSCreativeTabs.MAIN_TITLE, "Create: Belgian Snacks");
    }
}
