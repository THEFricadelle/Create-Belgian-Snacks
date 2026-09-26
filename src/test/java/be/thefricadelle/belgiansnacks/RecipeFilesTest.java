/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Checks the generated recipe files without loading Minecraft ({@code ./gradlew test}).
 */
class RecipeFilesTest {
    private static final String MOD_ID = "create_belgian_snacks";
    private static final Path RECIPES = Path.of("src/generated/resources/data/" + MOD_ID + "/recipe");
    private static final Path GENERATED_DATA = Path.of("src/generated/resources/data");
    private static final Path ITEM_MODELS = Path.of("src/generated/resources/assets/" + MOD_ID + "/models/item");
    private static final Path FLUID_TEXTURES = Path.of("src/main/resources/assets/" + MOD_ID + "/textures/fluid");

    // KubeJS scripts refer to these ids: renaming or removing one is a breaking change.
    private static final Set<String> EXPECTED_IDS = Set.of(
        "crushing/porkchop", "crushing/beef", "crushing/chicken", "crushing/bread",
        "milling/dried_kelp",
        "mixing/fricadelle_paste", "mixing/melted_beef_tallow", "mixing/mayonnaise",
        "mixing/curry_ketchup", "mixing/curry_ketchup_from_beetroot",
        "compacting/frying_oil_from_seeds",
        "pressing/fricadelle_paste");

    // Convention tags guaranteed non-empty: filled by NeoForge itself, or by us (checked below).
    private static final Set<String> GUARANTEED_C_TAGS = Set.of("c:eggs", "c:seeds");

    @Test
    void recipeIdsAreExactlyTheExpectedOnes() throws IOException {
        assertEquals(new TreeSet<>(EXPECTED_IDS), new TreeSet<>(recipes().keySet()));
    }

    @Test
    void typeMatchesTheFolder() throws IOException {
        recipes().forEach((id, json) -> {
            String folder = id.substring(0, id.indexOf('/'));
            assertEquals("create:" + folder, json.get("type").getAsString(), id + " type");
        });
    }

    @Test
    void ourItemsAndFluidsExist() throws IOException {
        Set<String> items = new HashSet<>();
        try (Stream<Path> files = Files.list(ITEM_MODELS)) {
            files.forEach(p -> items.add(p.getFileName().toString().replace(".json", "")));
        }
        recipes().forEach((id, json) -> {
            for (String ref : references(json)) {
                if (!ref.startsWith(MOD_ID + ":")) {
                    continue;
                }
                String path = ref.substring(MOD_ID.length() + 1);
                boolean known = items.contains(path) || Files.isRegularFile(FLUID_TEXTURES.resolve(path + "_still.png"));
                assertTrue(known, id + " references unknown " + ref);
            }
        });
    }

    @Test
    void otherModsAreOnlyReachedThroughTagsOrModLoaded() throws IOException {
        recipes().forEach((id, json) -> {
            Set<String> loadedGuards = modLoadedGuards(json);
            for (String ref : references(json)) {
                String namespace = ref.substring(0, ref.indexOf(':'));
                if (!namespace.equals("minecraft") && !namespace.equals(MOD_ID)) {
                    assertTrue(loadedGuards.contains(namespace), id + " uses " + ref + " without a mod_loaded condition");
                }
            }
        });
    }

    @Test
    void conventionTagsThatCanBeEmptyHaveAFallbackTwin() throws IOException {
        Map<String, JsonObject> recipes = recipes();
        recipes.forEach((id, json) -> {
            for (String tag : itemTags(json)) {
                if (!tag.startsWith("c:") || GUARANTEED_C_TAGS.contains(tag) || weFill(tag)) {
                    continue;
                }
                assertTrue(conditions(json).contains("not tag_empty " + tag), id + " reads " + tag + " but does not require it to be filled");
                boolean twin = recipes.values().stream().anyMatch(other -> conditions(other).contains("tag_empty " + tag));
                assertTrue(twin, id + " reads " + tag + " but no fallback recipe covers it being empty");
            }
        });
    }

    @Test
    void everyFluidHasStillFlowAnimationAndBucket() throws IOException {
        try (Stream<Path> files = Files.list(FLUID_TEXTURES)) {
            List<String> fluids = files.map(p -> p.getFileName().toString())
                .filter(n -> n.endsWith("_still.png"))
                .map(n -> n.replace("_still.png", ""))
                .toList();
            assertTrue(fluids.size() >= 4, "fluid textures found: " + fluids);
            // The block atlas does not scan textures/fluid: without an explicit entry the fluid renders as missing.
            String atlas = Files.readString(Path.of("src/generated/resources/assets/minecraft/atlases/blocks.json"), StandardCharsets.UTF_8);
            for (String fluid : fluids) {
                assertTrue(atlas.contains("\"" + MOD_ID + ":fluid/" + fluid + "_still\""), fluid + " still texture not in the block atlas");
                assertTrue(atlas.contains("\"" + MOD_ID + ":fluid/" + fluid + "_flow\""), fluid + " flow texture not in the block atlas");
                assertTrue(Files.isRegularFile(FLUID_TEXTURES.resolve(fluid + "_flow.png")), fluid + " flow texture");
                assertTrue(Files.isRegularFile(FLUID_TEXTURES.resolve(fluid + "_flow.png.mcmeta")), fluid + " flow animation");
                assertTrue(Files.isRegularFile(ITEM_MODELS.resolve(fluid + "_bucket.json")), fluid + " bucket model");
            }
        }
    }

    private static boolean weFill(String tag) {
        String path = tag.substring(2);
        Path file = GENERATED_DATA.resolve("c/tags/item/" + path + ".json");
        if (!Files.isRegularFile(file)) {
            return false;
        }
        try {
            for (JsonElement value : json(file).getAsJsonArray("values")) {
                if (value.isJsonPrimitive() && value.getAsString().startsWith(MOD_ID + ":")) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return false;
    }

    private static Map<String, JsonObject> recipes() throws IOException {
        Map<String, JsonObject> map = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(RECIPES)) {
            for (Path file : walk.filter(p -> p.toString().endsWith(".json")).toList()) {
                String id = RECIPES.relativize(file).toString().replace('\\', '/').replace(".json", "");
                map.put(id, json(file));
            }
        }
        return map;
    }

    // Every "item", "id" or "fluid" string in ingredients and results.
    private static Set<String> references(JsonObject recipe) {
        Set<String> refs = new HashSet<>();
        for (String key : List.of("ingredients", "results")) {
            if (recipe.has(key)) {
                for (JsonElement element : recipe.getAsJsonArray(key)) {
                    JsonObject entry = element.getAsJsonObject();
                    for (String field : List.of("item", "id", "fluid")) {
                        if (entry.has(field)) {
                            refs.add(entry.get(field).getAsString());
                        }
                    }
                }
            }
        }
        return refs;
    }

    private static Set<String> itemTags(JsonObject recipe) {
        Set<String> tags = new HashSet<>();
        for (JsonElement element : recipe.getAsJsonArray("ingredients")) {
            JsonObject entry = element.getAsJsonObject();
            // Sized fluid ingredients also carry a "tag", but with "type": "neoforge:tag" and an amount.
            if (entry.has("tag") && !entry.has("amount")) {
                tags.add(entry.get("tag").getAsString());
            }
        }
        return tags;
    }

    private static Set<String> modLoadedGuards(JsonObject recipe) {
        Set<String> mods = new HashSet<>();
        for (String condition : conditions(recipe)) {
            if (condition.startsWith("mod_loaded ")) {
                mods.add(condition.substring("mod_loaded ".length()));
            }
        }
        return mods;
    }

    // Flattened conditions: "tag_empty c:x", "not tag_empty c:x", "mod_loaded m", "not mod_loaded m".
    private static Set<String> conditions(JsonObject recipe) {
        Set<String> out = new HashSet<>();
        if (recipe.has("neoforge:conditions")) {
            for (JsonElement element : recipe.getAsJsonArray("neoforge:conditions")) {
                out.add(describe(element.getAsJsonObject()));
            }
        }
        return out;
    }

    private static String describe(JsonObject condition) {
        String type = condition.get("type").getAsString();
        return switch (type) {
            case "neoforge:not" -> "not " + describe(condition.getAsJsonObject("value"));
            case "neoforge:tag_empty" -> "tag_empty " + condition.get("tag").getAsString();
            case "neoforge:mod_loaded" -> "mod_loaded " + condition.get("modid").getAsString();
            default -> type;
        };
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
