/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import be.thefricadelle.belgiansnacks.BelgianSnacks;

/**
 * Every text of the Ponder scenes, in both languages, under the keys Ponder reads: scene titles
 * ({@code <modid>.ponder.<scene>.header}), shared lines ({@code <modid>.ponder.shared.<key>}) and
 * the tag. Common code: BSLang writes them to the lang files, the client plugin uses the keys.
 */
public final class BSPonderText {
    public record Line(String english, String french) {
    }

    private static final String PREFIX = BelgianSnacks.MOD_ID + ".ponder.";
    public static final String TAG = "belgian_snacks";
    /** Scene id to title. */
    public static final Map<String, Line> TITLES = new LinkedHashMap<>();
    /** Shared line key to text. */
    public static final Map<String, Line> LINES = new LinkedHashMap<>();

    static {
        title("fryer", "Frying in the Fryer", "Frire dans la Friteuse");
        line("fryer.heat", "The Fryer fries on a heated Blaze Burner", "La Friteuse frit sur un Blaze Burner allumé");
        line("fryer.superheat", "THE_FRICADELLE needs it superheated, with Blaze Cake",
            "THE_FRICADELLE demande un brûleur super-chauffé, au Blaze Cake");
        line("fryer.fat", "It fries in fat: fill it with buckets, a spout or pipes",
            "Elle frit dans la graisse : remplissez-la au seau, au Spout ou par tuyau");
        line("fryer.input", "Raw fricadelles arrive by belt, funnel or by hand", "Les fricadelles crues arrivent par tapis, funnel ou à la main");
        line("fryer.batch", "Up to 16 fry together; THE_FRICADELLE fries alone", "Jusqu'à 16 frisent ensemble ; THE_FRICADELLE frit seule");
        line("fryer.output", "Take the fried output by hand or with a funnel; the raw input stays in",
            "Récupérez la friture à la main ou au funnel ; l'entrée crue reste dedans");
        line("fryer.keep", "Broken with a wrench or a pickaxe, it keeps its fat", "Cassée à la clé ou à la pioche, elle garde sa graisse");

        title("supreme_grinder", "Collecting with the Supreme Grinder", "Collectionner avec le Hachoir Suprême");
        line("grinder.rotation", "The Supreme Grinder turns from above, at 64 RPM or more",
            "Le Hachoir Suprême tourne par le haut, à 64 tr/min ou plus");
        line("grinder.once", "It takes each food of the modpack once, one item at a time",
            "Il prend chaque aliment du modpack une seule fois, un item à la fois");
        line("grinder.refuse", "A food it already has waits on the belt, and so does anything that is not a food",
            "Un aliment déjà pris attend sur le tapis, comme tout ce qui n'est pas un aliment");
        line("grinder.gauge", "The gauge on its sides shows how close it is to its goal",
            "La jauge sur ses côtés montre où il en est de son objectif");

        title("supreme_grinder_modes", "Supreme Grinder modes", "Les modes du Hachoir Suprême");
        line("grinder.modes", "Choose what it collects for on its value panel", "Choisissez son objectif sur son panneau de valeur");
        line("grinder.the", "THE_: a share of every food, for an Exceptional Paste", "THE_ : une part de tous les aliments, pour une Pâte d'exception");
        line("grinder.ultimate", "ULTIMATE: every single food, for an Absolute Paste", "ULTIME : absolument tous les aliments, pour une Pâte absolue");
        line("grinder.paste", "At the goal the paste comes out below, and the whole collection is used up",
            "À l'objectif, la pâte sort par le bas et toute la collection est consommée");
        line("grinder.goggles", "With Goggles, sneak and right click with an empty hand for the list of missing foods",
            "Avec les Goggles, accroupi et main vide, un clic droit donne la liste des aliments manquants");

        title("the_fricadelle_line", "Assembling a THE_Fricadelle", "Assembler une THE_Fricadelle");
        line("line2.intro", "An Exceptional Paste becomes a raw THE_Fricadelle on a belt",
            "Une Pâte d'exception devient une THE_Fricadelle crue sur un tapis");
        line("line2.spices", "A Deployer adds Belgian spices", "Un Deployer ajoute des épices belges");
        line("line2.mayonnaise", "A Spout adds 100 mB of mayonnaise", "Un Spout ajoute 100 mB de mayonnaise");
        line("line2.curry", "A Spout adds 100 mB of curry ketchup", "Un Spout ajoute 100 mB de curry ketchup");
        line("line2.onion", "A Deployer adds an onion (a beetroot in packs without onions)",
            "Un Deployer ajoute un oignon (une betterave dans les packs sans oignon)");
        line("line2.press", "A Press shapes it", "Une Presse lui donne sa forme");
        line("line2.loops", "Three times round, then fry it in a heated Fryer with any frying fat",
            "Trois tours, puis faites-la frire dans une Friteuse chauffée, avec n'importe quelle graisse");

        title("ultimate_fricadelle_line", "Assembling THE_FRICADELLE", "Assembler THE_FRICADELLE");
        line("line3.intro", "The Absolute Paste becomes a raw THE_FRICADELLE on the same kind of line",
            "La Pâte absolue devient une THE_FRICADELLE crue sur le même genre de ligne");
        line("line3.tallow", "A Spout starts it with 250 mB of melted beef tallow",
            "Un Spout commence par 250 mB de blanc de bœuf fondu");
        line("line3.spices", "A Deployer adds Belgian spices", "Un Deployer ajoute des épices belges");
        line("line3.mayonnaise", "A Spout adds 250 mB of mayonnaise", "Un Spout ajoute 250 mB de mayonnaise");
        line("line3.curry", "A Spout adds 250 mB of curry ketchup", "Un Spout ajoute 250 mB de curry ketchup");
        line("line3.onion", "A Deployer adds an onion (a beetroot in packs without onions)",
            "Un Deployer ajoute un oignon (une betterave dans les packs sans oignons)");
        line("line3.press", "A Press shapes it", "Une Presse lui donne sa forme");
        line("line3.loops", "Five times round, then fry it superheated, in melted beef tallow only",
            "Cinq tours, puis friture super-chauffée, au blanc de bœuf fondu uniquement");
    }

    private BSPonderText() {
    }

    private static void title(String scene, String english, String french) {
        TITLES.put(scene, new Line(english, french));
    }

    private static void line(String key, String english, String french) {
        LINES.put(key, new Line(english, french));
    }

    public static String titleKey(String scene) {
        return PREFIX + scene + ".header";
    }

    public static String lineKey(String key) {
        return PREFIX + "shared." + key;
    }

    public static String tagKey() {
        return PREFIX + "tag." + TAG;
    }
}
