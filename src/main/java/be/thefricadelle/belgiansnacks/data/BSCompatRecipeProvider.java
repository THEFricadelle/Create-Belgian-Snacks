/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * Recipes whose result is another mod's fluid, which does not exist when the datagen runs: written as
 * JSON, loaded only with that mod (like BSBotanyPotsProvider).
 * <p>
 * V-D10: with Create Diesel Generators, used frying oil, ethanol and a pinch of salt as the catalyst
 * become their biodiesel. The salt is there because a basin recipe without an item would take the
 * ethanol of any other basin (docs/03, basin rule 1).
 */
public final class BSCompatRecipeProvider implements DataProvider {
    private static final String DIESEL = "createdieselgenerators";
    private final PackOutput.PathProvider recipes;

    public BSCompatRecipeProvider(PackOutput output) {
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", DIESEL);
        JsonArray conditions = new JsonArray();
        conditions.add(condition);

        JsonArray ingredients = new JsonArray();
        ingredients.add(fluid("neoforge:single", "fluid", BelgianSnacks.MOD_ID + ":used_frying_oil", 250));
        ingredients.add(fluid("neoforge:tag", "tag", "c:ethanol", 100));
        JsonObject salt = new JsonObject();
        salt.addProperty("tag", BelgianSnacks.MOD_ID + ":salts");
        ingredients.add(salt);

        JsonObject biodiesel = new JsonObject();
        biodiesel.addProperty("id", DIESEL + ":biodiesel");
        biodiesel.addProperty("amount", 250);
        JsonArray results = new JsonArray();
        results.add(biodiesel);

        JsonObject json = new JsonObject();
        json.add("neoforge:conditions", conditions);
        json.addProperty("type", "create:mixing");
        json.add("ingredients", ingredients);
        json.add("results", results);
        return DataProvider.saveStable(output, json, recipes.json(BelgianSnacks.asResource("mixing/biodiesel_from_used_oil")));
    }

    private static JsonObject fluid(String type, String key, String value, int amount) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty(key, value);
        json.addProperty("amount", amount);
        return json;
    }

    @Override
    public String getName() {
        return "Compat recipes of " + BelgianSnacks.MOD_ID;
    }
}
