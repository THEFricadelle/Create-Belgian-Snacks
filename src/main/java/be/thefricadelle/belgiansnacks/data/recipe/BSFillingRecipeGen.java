/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.FillingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import be.thefricadelle.belgiansnacks.registry.BSSweets;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.world.item.Items;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class BSFillingRecipeGen extends FillingRecipeGen {
    // v2 (section 5.2): a Spout puts 50 mB of a sauce on a plain cone, of fries or of beef tallow fries.
    private static final int SAUCE = 50;

    public BSFillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
        // v2 waffles (section 5.6): Create's chocolate on a Brussels waffle, Liège syrup in the lacquemant.
        create("brussels_waffle_chocolate", b -> b
            .require(BSWaffles.BRUSSELS_WAFFLE.get())
            .require(com.simibubi.create.AllFluids.CHOCOLATE.get(), SAUCE)
            .output(BSWaffles.BRUSSELS_WAFFLE_CHOCOLATE.get()));
        create("lacquemant", b -> b
            .require(BSWaffles.LACQUEMANT_WAFER.get())
            .require(BSFluids.LIEGE_SYRUP.get(), SAUCE)
            .output(BSWaffles.LACQUEMANT.get()));
        // v2 sweets (section 5.12). Pralines at the spout: under a press, Create turns lone chocolate into bars.
        create("pistolet_speculoos", b -> b
            .require(BSItems.PISTOLET.get()).require(BSFluids.SPECULOOS_SPREAD.get(), SAUCE).output(BSSweets.PISTOLET_SPECULOOS.get()));
        create("brussels_waffle_speculoos", b -> b
            .require(BSWaffles.BRUSSELS_WAFFLE.get()).require(BSFluids.SPECULOOS_SPREAD.get(), SAUCE).output(BSSweets.BRUSSELS_WAFFLE_SPECULOOS.get()));
        create("praline", b -> b
            .require(BSTags.PRALINE_FILLINGS).require(com.simibubi.create.AllFluids.CHOCOLATE.get(), SAUCE).output(BSSweets.PRALINE.get()));
        create("praline_kriek", b -> b
            .require(BSSweets.PRALINE.get()).require(BSFluids.KRIEK.get(), SAUCE).output(BSSweets.PRALINE_KRIEK.get()));
        create("dame_blanche", b -> b
            .require(BSTags.VANILLA_ICE_CREAMS).require(com.simibubi.create.AllFluids.CHOCOLATE.get(), SAUCE).output(BSSweets.DAME_BLANCHE.get()));
        create("hot_chocolate_bottle", b -> b
            .require(Items.GLASS_BOTTLE).require(BSFluids.HOT_CHOCOLATE.get(), 250).output(BSSweets.HOT_CHOCOLATE_BOTTLE.get()));
        create("spicy_chicken_wings", b -> b
            .require(BSSnacks.FRIED_CHICKEN_WINGS.get())
            .require(BSFluids.SAMOURAI.get(), 25)
            .output(BSSnacks.SPICY_CHICKEN_WINGS.get()));
        BSFluids.CONE_SAUCES.forEach((sauce, fluid) -> {
            create("cone_of_fries_" + sauce, b -> b
                .require(BSItems.CONE_OF_FRIES.get())
                .require(fluid.get(), SAUCE)
                .output(BSItems.SAUCED_CONES.get(sauce).get()));
            create("cone_of_fries_beef_tallow_" + sauce, b -> b
                .require(BSItems.CONE_OF_FRIES_BEEF_TALLOW.get())
                .require(fluid.get(), SAUCE)
                .output(BSItems.SAUCED_TALLOW_CONES.get(sauce).get()));
        });
    }
}
