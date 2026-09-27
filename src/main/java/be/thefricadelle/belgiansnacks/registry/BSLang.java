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
        add("subtitles." + ID + ".npc.speaks", "THEFricadelle speaks", "THEFricadelle parle");
        add("config.jade.plugin_" + ID + ".supreme_grinder", "Supreme Grinder", "Hachoir Suprême");
        add(ID + ".command.grinder.filled", "Supreme grinder filled with %s of %s foods", "Hachoir suprême rempli avec %s aliments sur %s");
        add(ID + ".command.grinder.cleared", "Supreme grinder emptied", "Hachoir suprême vidé");
        add(ID + ".command.announce.on", "Eating THE_FRICADELLE is announced in chat", "Manger THE_FRICADELLE est annoncé dans le chat");
        add(ID + ".command.announce.off", "Eating THE_FRICADELLE is no longer announced in chat",
            "Manger THE_FRICADELLE n'est plus annoncé dans le chat");
        add(ID + ".command.grinder.not_found", "No supreme grinder at %s", "Pas de hachoir suprême en %s");
        add(ID + ".ultimate.eaten", "%s ate THE_FRICADELLE. It literally contained everything.",
            "%s a mangé THE_FRICADELLE. Elle contenait littéralement tout.");
        add(ID + ".npc.phrase.0", "One fricadelle, coming up!", "Une fricadelle, une !");
        add(ID + ".npc.phrase.1", "You ate everything. Everything.", "Tu as tout mangé. Tout.");
        add(ID + ".npc.phrase.2", "Mayo or curry ketchup? Both, obviously.", "Mayo ou curry ketchup ? Les deux, évidemment.");
        add(ID + ".npc.phrase.3", "That was %s foods in one bite.", "%s aliments en une bouchée.");
        add(ID + ".npc.phrase.4", "The friterie is proud of you.", "La friterie est fière de toi.");
        advancement("root", "Create: Belgian Snacks", "Mince some meat: the friterie starts here",
            "Create: Belgian Snacks", "Hacher de la viande : la friterie commence ici");
        advancement("fryer", "Friterie Open", "Place a Fryer", "Friterie ouverte", "Poser une Friteuse");
        advancement("beef_tallow", "Beef Tallow", "Get a bucket of melted beef tallow, the fat of real Belgian fries",
            "Blanc de bœuf", "Obtenir un seau de blanc de bœuf fondu, la graisse des vraies frites belges");
        advancement("fricadelle", "One Fricadelle, Coming Up!", "Eat a Fricadelle", "Une fricadelle, une !", "Manger une Fricadelle");
        advancement("supreme_grinder", "The Supreme Grinder", "Place a Supreme Grinder", "Le Hachoir Suprême", "Poser un Hachoir Suprême");
        advancement("exceptional_paste", "Exceptional", "Get an Exceptional Paste", "Exceptionnelle", "Obtenir une Pâte d'exception");
        advancement("the_fricadelle", "Make It a Spéciale", "Eat a THE_Fricadelle", "Avec une spéciale", "Manger une THE_Fricadelle");
        advancement("grinder_half", "Halfway There", "Fill a Supreme Grinder with half of every food in the pack",
            "À mi-chemin", "Remplir un Hachoir Suprême avec la moitié de tous les aliments du pack");
        advancement("absolute_paste", "Absolute", "Get an Absolute Paste: every food of the pack in one paste",
            "Absolue", "Obtenir une Pâte absolue : tous les aliments du pack en une seule pâte");
        advancement("ultimate_fricadelle", "I Ate Everything", "Eat THE_FRICADELLE", "J'ai tout mangé", "Manger THE_FRICADELLE");
        // Ponder: the keys it reads, both languages from one table.
        BSPonderText.TITLES.forEach((scene, line) -> add(BSPonderText.titleKey(scene), line.english(), line.french()));
        BSPonderText.LINES.forEach((key, line) -> add(BSPonderText.lineKey(key), line.english(), line.french()));
        add(BSPonderText.tagKey(), "Create: Belgian Snacks", "Create: Belgian Snacks");
        add(BSPonderText.tagKey() + ".description", "The friterie: its machines and its pastes", "La friterie : ses machines et ses pâtes");
    }

    private BSLang() {
    }

    private static void advancement(String name, String englishTitle, String englishDescription, String frenchTitle, String frenchDescription) {
        add(BSAdvancements.KEY + name, englishTitle, frenchTitle);
        add(BSAdvancements.KEY + name + ".desc", englishDescription, frenchDescription);
    }

    private static void add(String key, String english, String french) {
        ENTRIES.put(key, new Entry(english, french));
    }

    // No-op outside datagen; must run during mod construction (see ERROR_LOG).
    public static void register() {
        ENTRIES.forEach((key, entry) -> BelgianSnacks.REGISTRATE.addRawLang(key, entry.english()));
    }
}
