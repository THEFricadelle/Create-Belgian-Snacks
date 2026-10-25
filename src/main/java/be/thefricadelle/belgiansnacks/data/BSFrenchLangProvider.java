/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import com.tterrag.registrate.util.entry.FluidEntry;

import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSEntities;
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
        add(BSItems.INCOMPLETE_ULTIMATE_FRICADELLE.get(), "THE_FRICADELLE en cours");
        add(BSEntities.THE_FRICADELLE_NPC.get(), "THEFricadelle");
        add(BSItems.ULTIMATE_FRICADELLE.get(), "THE_FRICADELLE");

        add(BSItems.PEARL_SUGAR.get(), "Sucre perlé");
        add(BSItems.CASSONADE.get(), "Cassonade");
        add(BSItems.POWDERED_SUGAR.get(), "Sucre glace");
        add(BSItems.SALT.get(), "Sel");
        add(BSItems.BUTTER.get(), "Beurre");
        add(BSItems.YOUNG_HERVE_CHEESE.get(), "Fromage de Herve jeune");
        add(BSItems.HERVE_CHEESE.get(), "Fromage de Herve");
        add(BSItems.MATON.get(), "Maton");
        add(BSItems.BREWERS_YEAST.get(), "Levure de bière");
        add(BSItems.SPROUTED_GRAIN.get(), "Grain germé");
        add(BSItems.MALT.get(), "Malt");
        add(BSItems.PISTOLET_DOUGH.get(), "Pâton de pistolet");
        add(BSItems.PISTOLET.get(), "Pistolet");
        add(BSItems.PUFF_PASTRY.get(), "Pâte feuilletée");
        add(BSItems.BRIOCHE_DOUGH.get(), "Pâte à brioche");
        add(BSItems.BURGER_BUN_DOUGH.get(), "Pâton de pain à burger");
        add(BSItems.BURGER_BUN.get(), "Pain à burger");
        add(BSItems.PITA_DOUGH.get(), "Pâte à pita");
        add(BSItems.RAW_PITA.get(), "Pita crue");
        add(BSItems.PITA_BREAD.get(), "Pain pita");
        add(BSItems.NOODLES.get(), "Nouilles");
        add(BSItems.LARDONS.get(), "Lardons");
        add(BSItems.BRINED_HAM.get(), "Jambon en saumure");
        add(BSItems.ARDENNES_HAM.get(), "Jambon d'Ardenne");
        add(BSItems.RAW_MERINGUE.get(), "Meringue crue");
        add(BSItems.MERINGUE.get(), "Meringue");
        add(BSItems.RAISINS.get(), "Raisins secs");
        add(BSItems.APPLE_COMPOTE.get(), "Compote de pommes");
        add(BSItems.CHOCOLATE_SHAVINGS.get(), "Copeaux de chocolat");
        add(BSItems.VANILLA_ICE_CREAM.get(), "Glace vanille");
        add(BSItems.SAUCED_CONES.get("sauce_lapin").get(), "Cornet de frites sauce lapin");
        add(BSItems.SAUCED_TALLOW_CONES.get("sauce_lapin").get(), "Cornet de frites au blanc, sauce lapin");
        add(BSCrops.CHICORY_ROOT.get(), "Racine de chicorée");
        add(BSCrops.CHICON.get(), "Chicon");
        add(BSCrops.LEEK.get(), "Poireau");
        add(BSCrops.HOP_SEEDS.get(), "Graines de houblon");
        add(BSCrops.HOPS.get(), "Cônes de houblon");
        add(BSCrops.HOP_SHOOTS.get(), "Jets de houblon");
        add(BSCrops.BRUSSELS_SPROUT.get(), "Chou de Bruxelles");
        add(BSCrops.ASPARAGUS_CROWN.get(), "Griffe d'asperge");
        add(BSCrops.WHITE_ASPARAGUS.get(), "Asperge blanche");
        add(BSCrops.GREEN_ASPARAGUS.get(), "Asperge verte");
        add(BSCrops.BUCKWHEAT.get(), "Sarrasin");
        add(BSCrops.BUCKWHEAT_FLOUR.get(), "Farine de sarrasin");
        add(BSCrops.MUSTARD_SEEDS.get(), "Graines de moutarde");
        add(BSCrops.MUSTARD_POWDER.get(), "Poudre de moutarde");
        add(BSCrops.MUSSEL.get(), "Moule");
        add(BSCrops.CHICON_CROP.get(), "Chicons");
        add(BSCrops.LEEK_CROP.get(), "Poireaux");
        add(BSCrops.HOPS_CROP.get(), "Houblon");
        add(BSCrops.BRUSSELS_SPROUTS_CROP.get(), "Choux de Bruxelles");
        add(BSCrops.ASPARAGUS_CROP.get(), "Asperges");
        add(BSCrops.BUCKWHEAT_CROP.get(), "Sarrasin");
        add(BSCrops.MUSTARD_CROP.get(), "Moutarde");
        add(BSCrops.BOUCHOT.get(), "Bouchot");
        add(BSItems.RAW_FRIES.get(), "Frites crues");
        add(BSItems.BLANCHED_FRIES.get(), "Frites blanchies");
        add(BSItems.FRIES.get(), "Frites");
        add(BSItems.FRIES_BEEF_TALLOW.get(), "Frites au blanc de bœuf");
        add(BSItems.PAPER_CONE.get(), "Cornet en papier");
        add(BSItems.CONE_OF_FRIES.get(), "Cornet de frites");
        add(BSItems.CONE_OF_FRIES_BEEF_TALLOW.get(), "Cornet de frites au blanc");
        add(BSItems.SAUCED_CONES.get("mayonnaise").get(), "Cornet de frites mayonnaise");
        add(BSItems.SAUCED_TALLOW_CONES.get("mayonnaise").get(), "Cornet de frites au blanc, mayonnaise");
        add(BSItems.SAUCED_CONES.get("curry_ketchup").get(), "Cornet de frites curry ketchup");
        add(BSItems.SAUCED_TALLOW_CONES.get("curry_ketchup").get(), "Cornet de frites au blanc, curry ketchup");
        add(BSItems.SAUCED_CONES.get("andalouse").get(), "Cornet de frites andalouse");
        add(BSItems.SAUCED_TALLOW_CONES.get("andalouse").get(), "Cornet de frites au blanc, andalouse");
        add(BSItems.SAUCED_CONES.get("samourai").get(), "Cornet de frites samouraï");
        add(BSItems.SAUCED_TALLOW_CONES.get("samourai").get(), "Cornet de frites au blanc, samouraï");
        add(BSItems.SAUCED_CONES.get("americaine").get(), "Cornet de frites américaine");
        add(BSItems.SAUCED_TALLOW_CONES.get("americaine").get(), "Cornet de frites au blanc, américaine");

        fluid(BSFluids.FRYING_OIL, "Huile de friture", "Seau d'huile de friture");
        fluid(BSFluids.MELTED_BEEF_TALLOW, "Blanc de bœuf fondu", "Seau de blanc de bœuf fondu");
        fluid(BSFluids.MAYONNAISE, "Mayonnaise", "Seau de mayonnaise");
        fluid(BSFluids.CURRY_KETCHUP, "Curry ketchup", "Seau de curry ketchup");
        fluid(BSFluids.ANDALOUSE, "Sauce andalouse", "Seau de sauce andalouse");
        fluid(BSFluids.SAMOURAI, "Sauce samouraï", "Seau de sauce samouraï");
        fluid(BSFluids.AMERICAINE, "Sauce américaine", "Seau de sauce américaine");
        fluid(BSFluids.CREAM, "Crème", "Seau de crème");
        fluid(BSFluids.BECHAMEL, "Béchamel", "Seau de béchamel");
        fluid(BSFluids.LIEGE_SYRUP, "Sirop de Liège", "Seau de sirop de Liège");
        fluid(BSFluids.VINEGAR, "Vinaigre", "Seau de vinaigre");
        fluid(BSFluids.SAUCE_LAPIN, "Sauce lapin", "Seau de sauce lapin");
        fluid(BSFluids.MUSTARD, "Moutarde", "Seau de moutarde");
        fluid(BSFluids.WORT, "Moût", "Seau de moût");
        fluid(BSFluids.BLONDE_BEER, "Bière blonde", "Seau de bière blonde");
        fluid(BSFluids.BROWN_BEER, "Bière brune", "Seau de bière brune");
        fluid(BSFluids.GUEUZE, "Gueuze", "Seau de gueuze");
        fluid(BSFluids.KRIEK, "Kriek", "Seau de kriek");

        add(BSBlocks.FRYER.get(), "Friteuse");
        add(BSBlocks.SUPREME_GRINDER.get(), "Hachoir Suprême");
        BSLang.ENTRIES.forEach((key, entry) -> add(key, entry.french()));

        add(BSItems.tooltipKey("fricadelle"), "Personne ne sait vraiment ce qu'il y a dedans.");
        add(BSItems.tooltipKey("the_fricadelle"), "Avec une spéciale, s'il vous plaît.");
        add(BSItems.tooltipKey("ultimate_fricadelle"), "Elle contient littéralement tout.");
        add(BSItems.tooltipKey("fries"), "Cuites deux fois, comme le veut la loi.");
        add(BSItems.tooltipKey("fries_beef_tallow"), "Frites dans la graisse de bœuf, comme chez bobonne.");
        add(BSItems.tooltipKey("cone_of_fries"), "Mayonnaise non comprise.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow"), "La vraie. Ton cardiologue n'est pas d'accord.");
        add(BSItems.tooltipKey("cone_of_fries_mayonnaise"), "Le classique belge. Oui, sur des frites.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow_mayonnaise"), "Le classique belge. Oui, sur des frites.");
        add(BSItems.tooltipKey("cone_of_fries_curry_ketchup"), "Sucré, épicé, très belge.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow_curry_ketchup"), "Sucré, épicé, très belge.");
        add(BSItems.tooltipKey("cone_of_fries_andalouse"), "Une mayonnaise partie en vacances en Espagne.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow_andalouse"), "Une mayonnaise partie en vacances en Espagne.");
        add(BSItems.tooltipKey("cone_of_fries_samourai"), "Ça pique. Garde une boisson à portée de main.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow_samourai"), "Ça pique. Garde une boisson à portée de main.");
        add(BSItems.tooltipKey("cone_of_fries_americaine"), "Ne vient pas d'Amérique. Personne ne sait pourquoi.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow_americaine"), "Ne vient pas d'Amérique. Personne ne sait pourquoi.");
        add(BSItems.tooltipKey("cone_of_fries_sauce_lapin"), "Sauce lapin, sans lapin.");
        add(BSItems.tooltipKey("cone_of_fries_beef_tallow_sauce_lapin"), "Sauce lapin, sans lapin.");
        add(BSItems.tooltipKey("herve_cheese"), "Tes voisins le sauront.");
        add(BSItems.tooltipKey("pistolet"), "Le petit pain du dimanche matin.");
        add(BSItems.tooltipKey("ardennes_ham"), "Fumé en Ardenne. Ou devant un ventilateur.");
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
