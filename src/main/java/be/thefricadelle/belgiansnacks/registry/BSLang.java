/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import be.thefricadelle.belgiansnacks.BelgianSnacks;

/**
 * Translations that belong to no registry entry (GUI, goggles, recipe categories, subtitles),
 * each key written once with its English and French text. English goes through Registrate,
 * French through BSFrenchLangProvider.
 */
public final class BSLang {
    public record Entry(String english, String french) {
    }

    private static final String ID = BelgianSnacks.MOD_ID;
    public static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    static {
        add(ID + ".fryer.status.idle", "Idle", "Au repos");
        add(ID + ".fryer.status.frying", "Frying: %s%%", "Friture : %s %%");
        add(ID + ".fryer.status.no_recipe", "Nothing to fry", "Rien à frire");
        add(ID + ".fryer.status.no_heat", "Needs more heat", "Pas assez chaud");
        add(ID + ".fryer.status.no_fat", "Not enough fat", "Pas assez de graisse");
        add(ID + ".fryer.status.output_full", "Output full", "Sortie pleine");
        add(ID + ".fryer.heat.none", "Not heated", "Non chauffée");
        add(ID + ".fryer.heat.heated", "Heated", "Chauffée");
        add(ID + ".fryer.heat.superheated", "Superheated", "Surchauffée");
        add(ID + ".recipe.frying", "Frying", "Friture");
        add(ID + ".recipe.frying.fat", "Fat per item", "Graisse par item");
        add("subtitles." + ID + ".fryer.sizzle", "Fryer sizzles", "La friteuse grésille");
        add("subtitles." + ID + ".fricadelle.burp", "Satisfied burp", "Rot satisfait");
        add("config.jade.plugin_" + ID + ".fryer", "Fryer", "Friteuse");
    }

    private BSLang() {
    }

    private static void add(String key, String english, String french) {
        ENTRIES.put(key, new Entry(english, french));
    }

    // No-op outside datagen; must run during mod construction (see ERROR_LOG).
    public static void register() {
        ENTRIES.forEach((key, entry) -> BelgianSnacks.REGISTRATE.addRawLang(key, entry.english()));
    }
}
