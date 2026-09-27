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
        add(ID + ".command.foods.count", "%s foods (%s from the extra tag), %s excluded, computed in %s ms",
            "%s aliments (dont %s par le tag extra), %s exclus, calculés en %s ms");
        add(ID + ".command.foods.export", "Exported %s foods to %s", "%s aliments exportés dans %s");
        add(ID + ".command.foods.export_failed", "Food export failed: %s", "Échec de l'export des aliments : %s");
        add(ID + ".grinder.mode", "Collecting for", "Collection pour");
        add(ID + ".grinder.mode.the", "THE_Fricadelle (Exceptional Paste)", "THE_Fricadelle (Pâte d'exception)");
        add(ID + ".grinder.mode.ultimate", "THE_FRICADELLE (Absolute Paste)", "THE_FRICADELLE (Pâte absolue)");
        add(ID + ".grinder.goggles.mode", "Mode: %s", "Mode : %s");
        add(ID + ".grinder.goggles.progress", "%s / %s foods (%s%%)", "%s / %s aliments (%s %%)");
        add(ID + ".grinder.goggles.too_slow", "Needs at least %s RPM", "Il faut au moins %s tr/min");
        add(ID + ".grinder.goggles.output_full", "Take the paste out", "Retirez la pâte");
        add(ID + ".grinder.goggles.missing", "Still missing, for instance:", "Il manque encore, par exemple :");
        add(ID + ".grinder.goggles.full_list", "Sneak + right click, empty hand: full list", "Accroupi + clic droit, main vide : liste complète");
        add(ID + ".grinder.excess", "A THE_FRICADELLE attempt that fell short: %s extra foods went into the paste",
            "Une tentative de THE_FRICADELLE ratée : %s aliments en trop sont partis dans la pâte");
        add(ID + ".grinder.screen.title", "Supreme Grinder: missing foods", "Hachoir Suprême : aliments manquants");
        add(ID + ".grinder.screen.count", "%s missing out of %s", "%s manquants sur %s");
        add(ID + ".grinder.screen.none", "Nothing missing", "Il ne manque rien");
        add(ID + ".recipe.grinding_goal", "Supreme Grinding", "Hachoir Suprême");
        add(ID + ".recipe.grinding_goal.foods", "%s of %s foods (%s%%)", "%s aliments sur %s (%s %%)");
        add(ID + ".recipe.grinding_goal.each", "Each food counts once", "Chaque aliment compte une fois");
        add("subtitles." + ID + ".grinder.grind", "Grinder grinds", "Le hachoir broie");
        add("subtitles." + ID + ".grinder.complete", "Grinder finishes a paste", "Le hachoir termine une pâte");
        add("config.jade.plugin_" + ID + ".supreme_grinder", "Supreme Grinder", "Hachoir Suprême");
        add(ID + ".command.grinder.filled", "Supreme grinder filled with %s of %s foods", "Hachoir suprême rempli avec %s aliments sur %s");
        add(ID + ".command.grinder.cleared", "Supreme grinder emptied", "Hachoir suprême vidé");
        add(ID + ".command.grinder.not_found", "No supreme grinder at %s", "Pas de hachoir suprême en %s");
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
