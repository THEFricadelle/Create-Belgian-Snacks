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

import com.simibubi.create.api.data.recipe.PressingRecipeGen;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSDishes;
import be.thefricadelle.belgiansnacks.registry.BSSweets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import com.simibubi.create.AllItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;

public class BSPressingRecipeGen extends PressingRecipeGen {
    GeneratedRecipe FRICADELLE_PASTE = create("fricadelle_paste", b -> b
        .require(BSItems.FRICADELLE_PASTE.get())
        .output(BSItems.RAW_FRICADELLE.get()));

    GeneratedRecipe RAW_PITA = create("raw_pita", b -> b
        .require(BSItems.PITA_DOUGH.get())
        .output(BSItems.RAW_PITA.get()));

    // v2 friterie snacks (section 5.10): mixes shaped into sausages, balls, patties and discs.
    GeneratedRecipe
        RAW_BOULET = press("raw_boulet", BSSnacks.BOULET_PASTE.get(), BSSnacks.RAW_BOULET.get()),
        RAW_CERVELAS = press("raw_cervelas", BSSnacks.CERVELAS_MIX.get(), BSSnacks.RAW_CERVELAS.get()),
        RAW_MEXICANO = press("raw_mexicano", BSSnacks.MEXICANO_MIX.get(), BSSnacks.RAW_MEXICANO.get()),
        RAW_VIANDELLE = press("raw_viandelle", BSSnacks.VIANDELLE_MIX.get(), BSSnacks.RAW_VIANDELLE.get()),
        RAW_BAMI_DISC = press("raw_bami_disc", BSSnacks.BAMI_MIX.get(), BSSnacks.RAW_BAMI_DISC.get()),
        RAW_NASI_DISC = press("raw_nasi_disc", BSSnacks.NASI_MIX.get(), BSSnacks.RAW_NASI_DISC.get()),
        LOEMPIA_WRAPPER = press("loempia_wrapper", AllItems.DOUGH.get(), BSSnacks.LOEMPIA_WRAPPER.get()),
        HOT_DOG_BUN_DOUGH = press("hot_dog_bun_dough", BSItems.BURGER_BUN_DOUGH.get(), BSSnacks.HOT_DOG_BUN_DOUGH.get()),
        TART_SHELL = press("tart_shell", BSItems.PUFF_PASTRY.get(), BSDishes.TART_SHELL.get()),
        RAW_SPECULOOS = press("raw_speculoos", BSSweets.SPECULOOS_DOUGH.get(), BSSweets.RAW_SPECULOOS.get()),
        RAW_COUQUE = press("raw_couque", BSSweets.COUQUE_DOUGH.get(), BSSweets.RAW_COUQUE.get()),
        FLAT_BRIOCHE = press("flat_brioche", BSItems.BRIOCHE_DOUGH.get(), BSSweets.FLAT_BRIOCHE.get()),
        RAW_PATTY = create("raw_patty", b -> b.require(BSTags.MINCED_BEEF).output(BSSnacks.RAW_PATTY.get()));

    public BSPressingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BelgianSnacks.MOD_ID);
    }

    private GeneratedRecipe press(String name, ItemLike input, ItemLike result) {
        return create(name, b -> b.require(input).output(result));
    }
}
