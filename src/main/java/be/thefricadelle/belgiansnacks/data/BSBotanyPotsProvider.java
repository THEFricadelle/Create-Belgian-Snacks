/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCrops;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * Our crops in Botany Pots (present in Arcadia): a block-derived crop each, loaded only with that mod.
 * Plain JSON: Botany Pots is not a dependency, not even at compile time.
 */
public final class BSBotanyPotsProvider implements DataProvider {
    private final PackOutput.PathProvider recipes;

    public BSBotanyPotsProvider(PackOutput output) {
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        for (var crop : BSCrops.CROPS) {
            String block = BuiltInRegistries.BLOCK.getKey(crop.get()).toString();
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "neoforge:mod_loaded");
            condition.addProperty("modid", "botanypots");
            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            JsonObject json = new JsonObject();
            json.add("neoforge:conditions", conditions);
            json.addProperty("type", "botanypots:block_derived_crop");
            json.addProperty("block", block);
            writes.add(DataProvider.saveStable(output, json,
                recipes.json(BelgianSnacks.asResource("botanypots/" + crop.getId().getPath().replace("_crop", "")))));
        }
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Botany Pots crops of " + BelgianSnacks.MOD_ID;
    }
}
