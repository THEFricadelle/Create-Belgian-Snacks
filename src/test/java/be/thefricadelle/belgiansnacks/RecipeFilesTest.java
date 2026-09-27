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
import java.util.ArrayList;
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
        "pressing/fricadelle_paste",
        "frying/fricadelle", "frying/the_fricadelle", "frying/ultimate_fricadelle",
        "mechanical_crafting/fryer", "mechanical_crafting/supreme_grinder",
        "sequenced_assembly/raw_the_fricadelle", "sequenced_assembly/raw_the_fricadelle_from_beetroot",
        "sequenced_assembly/raw_ultimate_fricadelle", "sequenced_assembly/raw_ultimate_fricadelle_from_beetroot");

    // Recipe types this mod registers; every other folder is a Create type.
    private static final Set<String> OWN_TYPES = Set.of("frying");

    // Convention tags guaranteed non-empty: filled by NeoForge itself, or by us (checked below).
    // c:plates/copper and c:plates/brass are filled by Create, a required dependency.
    private static final Set<String> GUARANTEED_C_TAGS = Set.of("c:eggs", "c:seeds", "c:plates/copper", "c:plates/brass",
        "c:ingots/gold", "c:storage_blocks/iron");

    @Test
    void recipeIdsAreExactlyTheExpectedOnes() throws IOException {
        assertEquals(new TreeSet<>(EXPECTED_IDS), new TreeSet<>(recipes().keySet()));
    }

    @Test
    void typeMatchesTheFolder() throws IOException {
        recipes().forEach((id, json) -> {
            String folder = id.substring(0, id.indexOf('/'));
            String namespace = OWN_TYPES.contains(folder) ? MOD_ID : "create";
            assertEquals(namespace + ":" + folder, json.get("type").getAsString(), id + " type");
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
                // Create is a required dependency: its items are always there.
                if (!namespace.equals("minecraft") && !namespace.equals(MOD_ID) && !namespace.equals("create")) {
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

    // Every "item", "id" or "fluid" string in ingredients, results, a crafting key or its result.
    private static Set<String> references(JsonObject recipe) {
        Set<String> refs = new HashSet<>();
        for (JsonObject entry : entries(recipe)) {
            for (String field : List.of("item", "id", "fluid")) {
                if (entry.has(field)) {
                    refs.add(entry.get(field).getAsString());
                }
            }
        }
        return refs;
    }

    private static Set<String> itemTags(JsonObject recipe) {
        Set<String> tags = new HashSet<>();
        for (JsonObject entry : entries(recipe)) {
            // Sized fluid ingredients also carry a "tag", but with "type": "neoforge:tag" and an amount.
            if (entry.has("tag") && !entry.has("amount")) {
                tags.add(entry.get("tag").getAsString());
            }
        }
        return tags;
    }

    // Processing recipes list "ingredients"/"results"; crafting recipes use a "key" map and a "result";
    // a sequenced assembly has one "ingredient" and its steps, each a processing recipe, in "sequence".
    private static List<JsonObject> entries(JsonObject recipe) {
        List<JsonObject> entries = new ArrayList<>();
        if (recipe.has("ingredient")) {
            entries.add(recipe.getAsJsonObject("ingredient"));
        }
        if (recipe.has("sequence")) {
            recipe.getAsJsonArray("sequence").forEach(step -> entries.addAll(entries(step.getAsJsonObject())));
        }
        for (String key : List.of("ingredients", "results")) {
            if (recipe.has(key)) {
                recipe.getAsJsonArray(key).forEach(e -> entries.add(e.getAsJsonObject()));
            }
        }
        if (recipe.has("key")) {
            recipe.getAsJsonObject("key").entrySet().forEach(e -> entries.add(e.getValue().getAsJsonObject()));
        }
        if (recipe.has("result")) {
            entries.add(recipe.getAsJsonObject("result"));
        }
        return entries;
    }

    @Test
    void fryerIsCraftedOnAMechanicalCrafter() throws IOException {
        JsonObject recipe = json(RECIPES.resolve("mechanical_crafting/fryer.json"));
        assertEquals("create:mechanical_crafting", recipe.get("type").getAsString());
        assertEquals(MOD_ID + ":fryer", recipe.getAsJsonObject("result").get("id").getAsString());
        JsonObject key = recipe.getAsJsonObject("key");
        assertEquals("create:basin", key.getAsJsonObject("B").get("item").getAsString());
        assertEquals("create:fluid_tank", key.getAsJsonObject("T").get("item").getAsString());
        assertEquals("create:precision_mechanism", key.getAsJsonObject("P").get("item").getAsString());
        assertEquals("minecraft:iron_bars", key.getAsJsonObject("I").get("item").getAsString());
        assertEquals("c:plates/copper", key.getAsJsonObject("C").get("tag").getAsString());
    }

    @Test
    void supremeGrinderIsCraftedOnAMechanicalCrafter() throws IOException {
        JsonObject recipe = json(RECIPES.resolve("mechanical_crafting/supreme_grinder.json"));
        assertEquals("create:mechanical_crafting", recipe.get("type").getAsString());
        assertEquals(MOD_ID + ":supreme_grinder", recipe.getAsJsonObject("result").get("id").getAsString());
        JsonObject key = recipe.getAsJsonObject("key");
        assertEquals("create:crushing_wheel", key.getAsJsonObject("W").get("item").getAsString());
        assertEquals("create:brass_casing", key.getAsJsonObject("C").get("item").getAsString());
        assertEquals("create:precision_mechanism", key.getAsJsonObject("M").get("item").getAsString());
        assertEquals("c:plates/brass", key.getAsJsonObject("S").get("tag").getAsString());
        assertEquals("c:ingots/gold", key.getAsJsonObject("G").get("tag").getAsString());
        assertEquals("c:storage_blocks/iron", key.getAsJsonObject("I").get("tag").getAsString());
        // Two crushing wheels (D11).
        assertEquals(2, String.join("", recipe.getAsJsonArray("pattern").asList().stream().map(e -> e.getAsString()).toList())
            .chars().filter(c -> c == 'W').count());
    }

    // Tier 2 (D4, D5, D7): spices, mayonnaise, curry ketchup, onion, press; three loops; never fails.
    @Test
    void theFricadelleAssemblyIsTheDocumentedOne() throws IOException {
        for (String variant : List.of("raw_the_fricadelle", "raw_the_fricadelle_from_beetroot")) {
            JsonObject recipe = json(RECIPES.resolve("sequenced_assembly/" + variant + ".json"));
            assertEquals("create:sequenced_assembly", recipe.get("type").getAsString());
            assertEquals(MOD_ID + ":exceptional_paste", recipe.getAsJsonObject("ingredient").get("item").getAsString());
            assertEquals(3, recipe.get("loops").getAsInt(), variant + " loops");
            assertEquals(MOD_ID + ":incomplete_the_fricadelle", recipe.getAsJsonObject("transitional_item").get("id").getAsString());
            var results = recipe.getAsJsonArray("results");
            assertEquals(1, results.size(), variant + ": a single, certain result");
            assertEquals(MOD_ID + ":raw_the_fricadelle", results.get(0).getAsJsonObject().get("id").getAsString());

            var steps = recipe.getAsJsonArray("sequence");
            List<String> types = new ArrayList<>();
            steps.forEach(step -> types.add(step.getAsJsonObject().get("type").getAsString()));
            assertEquals(List.of("create:deploying", "create:filling", "create:filling", "create:deploying", "create:pressing"), types, variant);
            assertEquals(MOD_ID + ":belgian_spices", ingredient(steps, 0).get("item").getAsString());
            assertEquals(MOD_ID + ":mayonnaise", ingredient(steps, 1).get("fluid").getAsString());
            assertEquals(100, ingredient(steps, 1).get("amount").getAsInt(), "mayonnaise per step");
            assertEquals(MOD_ID + ":curry_ketchup", ingredient(steps, 2).get("fluid").getAsString());
            assertEquals(100, ingredient(steps, 2).get("amount").getAsInt(), "curry ketchup per step");
        }
        JsonObject onion = json(RECIPES.resolve("sequenced_assembly/raw_the_fricadelle.json"));
        assertEquals("c:crops/onion", ingredient(onion.getAsJsonArray("sequence"), 3).get("tag").getAsString());
        assertTrue(conditions(onion).contains("not tag_empty c:crops/onion"), "the onion recipe needs onions");
        JsonObject beetroot = json(RECIPES.resolve("sequenced_assembly/raw_the_fricadelle_from_beetroot.json"));
        assertEquals("minecraft:beetroot", ingredient(beetroot.getAsJsonArray("sequence"), 3).get("item").getAsString());
        assertTrue(conditions(beetroot).contains("tag_empty c:crops/onion"), "the beetroot recipe is only for packs without onions");
    }

    // Tier 3 (27/09/2026): beef tallow, spices, mayonnaise, curry ketchup, onion, press; five loops.
    @Test
    void ultimateFricadelleAssemblyIsTheDocumentedOne() throws IOException {
        for (String variant : List.of("raw_ultimate_fricadelle", "raw_ultimate_fricadelle_from_beetroot")) {
            JsonObject recipe = json(RECIPES.resolve("sequenced_assembly/" + variant + ".json"));
            assertEquals(MOD_ID + ":absolute_paste", recipe.getAsJsonObject("ingredient").get("item").getAsString());
            assertEquals(5, recipe.get("loops").getAsInt(), variant + " loops");
            assertEquals(MOD_ID + ":incomplete_ultimate_fricadelle", recipe.getAsJsonObject("transitional_item").get("id").getAsString());
            var results = recipe.getAsJsonArray("results");
            assertEquals(1, results.size(), variant + ": a single, certain result");
            assertEquals(MOD_ID + ":raw_ultimate_fricadelle", results.get(0).getAsJsonObject().get("id").getAsString());

            var steps = recipe.getAsJsonArray("sequence");
            List<String> types = new ArrayList<>();
            steps.forEach(step -> types.add(step.getAsJsonObject().get("type").getAsString()));
            assertEquals(List.of("create:filling", "create:deploying", "create:filling", "create:filling", "create:deploying", "create:pressing"),
                types, variant);
            String transition = MOD_ID + ":incomplete_ultimate_fricadelle";
            assertEquals(MOD_ID + ":melted_beef_tallow", ingredient(steps, 0, transition).get("fluid").getAsString());
            assertEquals(MOD_ID + ":belgian_spices", ingredient(steps, 1, transition).get("item").getAsString());
            assertEquals(MOD_ID + ":mayonnaise", ingredient(steps, 2, transition).get("fluid").getAsString());
            assertEquals(MOD_ID + ":curry_ketchup", ingredient(steps, 3, transition).get("fluid").getAsString());
            for (int step : new int[] {0, 2, 3}) {
                assertEquals(250, ingredient(steps, step, transition).get("amount").getAsInt(), variant + " step " + step);
            }
        }
        JsonObject onion = json(RECIPES.resolve("sequenced_assembly/raw_ultimate_fricadelle.json"));
        assertEquals("c:crops/onion", ingredient(onion.getAsJsonArray("sequence"), 4, MOD_ID + ":incomplete_ultimate_fricadelle")
            .get("tag").getAsString());
        assertTrue(conditions(onion).contains("not tag_empty c:crops/onion"));
        JsonObject beetroot = json(RECIPES.resolve("sequenced_assembly/raw_ultimate_fricadelle_from_beetroot.json"));
        assertEquals("minecraft:beetroot", ingredient(beetroot.getAsJsonArray("sequence"), 4, MOD_ID + ":incomplete_ultimate_fricadelle")
            .get("item").getAsString());
        assertTrue(conditions(beetroot).contains("tag_empty c:crops/onion"));
    }

    // Tier 3 frying: superheated, 30 s, 250 mB of melted beef tallow and nothing else.
    @Test
    void ultimateFricadelleFriesOnlyInBeefTallow() throws IOException {
        JsonObject recipe = json(RECIPES.resolve("frying/ultimate_fricadelle.json"));
        assertEquals("superheated", recipe.get("heat_requirement").getAsString());
        assertEquals(600, recipe.get("processing_time").getAsInt());
        var ingredients = recipe.getAsJsonArray("ingredients");
        assertEquals(MOD_ID + ":raw_ultimate_fricadelle", ingredients.get(0).getAsJsonObject().get("item").getAsString());
        JsonObject fat = ingredients.get(1).getAsJsonObject();
        assertEquals(MOD_ID + ":melted_beef_tallow", fat.get("fluid").getAsString(), "a single fluid, not the frying_oils tag");
        assertEquals(250, fat.get("amount").getAsInt());
        assertEquals(MOD_ID + ":ultimate_fricadelle", recipe.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
    }

    // The second ingredient of a step: the first is always the transitional item.
    private static JsonObject ingredient(com.google.gson.JsonArray steps, int step) {
        return ingredient(steps, step, MOD_ID + ":incomplete_the_fricadelle");
    }

    private static JsonObject ingredient(com.google.gson.JsonArray steps, int step, String transition) {
        var ingredients = steps.get(step).getAsJsonObject().getAsJsonArray("ingredients");
        assertEquals(transition, ingredients.get(0).getAsJsonObject().get("item").getAsString(), "step " + step);
        return ingredients.get(1).getAsJsonObject();
    }

    // D20: heated, any frying fat, 25 mB a raw THE_Fricadelle, 200 ticks.
    @Test
    void theFricadelleFriesWithAnyFat() throws IOException {
        JsonObject recipe = json(RECIPES.resolve("frying/the_fricadelle.json"));
        assertEquals(MOD_ID + ":frying", recipe.get("type").getAsString());
        assertEquals("heated", recipe.get("heat_requirement").getAsString());
        assertEquals(200, recipe.get("processing_time").getAsInt());
        var ingredients = recipe.getAsJsonArray("ingredients");
        assertEquals(MOD_ID + ":raw_the_fricadelle", ingredients.get(0).getAsJsonObject().get("item").getAsString());
        assertEquals(MOD_ID + ":frying_oils", ingredients.get(1).getAsJsonObject().get("tag").getAsString());
        assertEquals(25, ingredients.get(1).getAsJsonObject().get("amount").getAsInt());
        assertEquals(MOD_ID + ":the_fricadelle", recipe.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
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
