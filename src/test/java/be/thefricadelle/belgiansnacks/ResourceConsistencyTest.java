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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
 * Checks the files produced by datagen and the hand-made assets against each other, without
 * loading Minecraft. Runs from the project root ({@code ./gradlew test}).
 */
class ResourceConsistencyTest {
    private static final String MOD_ID = "create_belgian_snacks";
    private static final Path GENERATED = Path.of("src/generated/resources");
    private static final Path MAIN = Path.of("src/main/resources");
    private static final Path LANG = GENERATED.resolve("assets/" + MOD_ID + "/lang");
    private static final Path ITEM_MODELS = GENERATED.resolve("assets/" + MOD_ID + "/models/item");
    private static final Path ITEM_TEXTURES = MAIN.resolve("assets/" + MOD_ID + "/textures/item");
    private static final Path TAGS = GENERATED.resolve("data");
    private static final Set<String> BRAND_ITEMS = Set.of("fricadelle", "the_fricadelle", "ultimate_fricadelle");
    // Namespaces always present at runtime; entries from any other mod must be optional.
    private static final Set<String> ALWAYS_LOADED = Set.of("minecraft", "c", MOD_ID);

    @Test
    void englishAndFrenchDefineTheSameKeys() throws IOException {
        Map<String, String> en = lang("en_us");
        Map<String, String> fr = lang("fr_fr");
        Set<String> onlyEn = new TreeSet<>(en.keySet());
        onlyEn.removeAll(fr.keySet());
        Set<String> onlyFr = new TreeSet<>(fr.keySet());
        onlyFr.removeAll(en.keySet());
        assertTrue(onlyEn.isEmpty(), "Missing in fr_fr: " + onlyEn);
        assertTrue(onlyFr.isEmpty(), "Missing in en_us: " + onlyFr);
    }

    @Test
    void noTranslationIsBlankOrARawKey() throws IOException {
        for (String locale : List.of("en_us", "fr_fr")) {
            lang(locale).forEach((key, value) -> {
                assertFalse(value.isBlank(), locale + " " + key + " is blank");
                assertFalse(value.equals(key), locale + " " + key + " is left untranslated");
            });
        }
    }

    @Test
    void fricadelleNamesAreIdenticalInEveryLanguage() throws IOException {
        Map<String, String> en = lang("en_us");
        Map<String, String> fr = lang("fr_fr");
        for (String id : BRAND_ITEMS) {
            String key = "item." + MOD_ID + "." + id;
            assertEquals(en.get(key), fr.get(key), key + " is a brand name and must not be translated");
        }
    }

    @Test
    void everyFricadelleHasATooltipInBothLanguages() throws IOException {
        for (String locale : List.of("en_us", "fr_fr")) {
            Map<String, String> lang = lang(locale);
            for (String id : BRAND_ITEMS) {
                assertTrue(lang.containsKey("item." + MOD_ID + "." + id + ".tooltip.summary"), locale + " tooltip for " + id);
            }
        }
    }

    @Test
    void everyItemModelHasANameInBothLanguages() throws IOException {
        Map<String, String> en = lang("en_us");
        Map<String, String> fr = lang("fr_fr");
        for (String id : itemModelIds()) {
            // A block item is named under the block key.
            String key = en.containsKey("block." + MOD_ID + "." + id) ? "block." + MOD_ID + "." + id : "item." + MOD_ID + "." + id;
            assertTrue(en.containsKey(key), "en_us name for " + id);
            assertTrue(fr.containsKey(key), "fr_fr name for " + id);
        }
    }

    @Test
    void everyItemModelPointsToAnExistingTexture() throws IOException {
        for (String id : itemModelIds()) {
            JsonObject textures = texturesOf(ITEM_MODELS.resolve(id + ".json"));
            assertTrue(textures != null && !textures.entrySet().isEmpty(), id + " model has no texture");
            for (Map.Entry<String, JsonElement> layer : textures.entrySet()) {
                String[] location = layer.getValue().getAsString().split(":", 2);
                Path png = MAIN.resolve("assets/" + location[0] + "/textures/" + location[1] + ".png");
                assertTrue(Files.isRegularFile(png), id + " references missing texture " + png);
            }
        }
    }

    @Test
    void soundsHaveSubtitlesInBothLanguages() throws IOException {
        JsonObject sounds = json(GENERATED.resolve("assets/" + MOD_ID + "/sounds.json"));
        assertFalse(sounds.entrySet().isEmpty(), "no sound event defined");
        Map<String, String> en = lang("en_us");
        Map<String, String> fr = lang("fr_fr");
        for (Map.Entry<String, JsonElement> event : sounds.entrySet()) {
            JsonObject definition = event.getValue().getAsJsonObject();
            assertTrue(definition.has("subtitle"), event.getKey() + " has no subtitle");
            String subtitle = definition.get("subtitle").getAsString();
            assertTrue(en.containsKey(subtitle) && fr.containsKey(subtitle), subtitle + " not translated in both languages");
            assertFalse(definition.getAsJsonArray("sounds").isEmpty(), event.getKey() + " plays nothing");
        }
    }

    @Test
    void brokenFryerKeepsItsFat() throws IOException {
        JsonObject table = json(GENERATED.resolve("data/" + MOD_ID + "/loot_table/blocks/fryer.json"));
        boolean copies = false;
        for (JsonElement function : table.getAsJsonArray("functions")) {
            JsonObject f = function.getAsJsonObject();
            if (f.get("function").getAsString().equals("minecraft:copy_components") && f.get("source").getAsString().equals("block_entity")) {
                for (JsonElement included : f.getAsJsonArray("include")) {
                    copies |= included.getAsString().equals(MOD_ID + ":fryer_fluid");
                }
            }
        }
        assertTrue(copies, "the fryer loot table must copy fryer_fluid from the block entity");
    }

    @Test
    void everyItemTextureIsASixteenPixelSquareUsedByAModel() throws IOException {
        Set<String> models = itemModelIds();
        try (Stream<Path> files = Files.list(ITEM_TEXTURES)) {
            for (Path png : files.filter(p -> p.toString().endsWith(".png")).toList()) {
                String id = png.getFileName().toString().replace(".png", "");
                assertTrue(models.contains(id), "Orphan texture " + png);
                int[] size = pngSize(png);
                assertEquals(16, size[0], png + " width");
                assertEquals(16, size[1], png + " height");
            }
        }
    }

    @Test
    void tagEntriesExistOrAreOptional() throws IOException {
        Set<String> models = itemModelIds();
        List<Path> tagFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(TAGS)) {
            walk.filter(p -> p.toString().replace('\\', '/').contains("/tags/item/") && p.toString().endsWith(".json"))
                .forEach(tagFiles::add);
        }
        assertFalse(tagFiles.isEmpty(), "No generated item tag found");
        for (Path file : tagFiles) {
            for (JsonElement value : json(file).getAsJsonArray("values")) {
                boolean optional = value.isJsonObject() && !value.getAsJsonObject().get("required").getAsBoolean();
                String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
                if (id.startsWith("#")) {
                    continue;
                }
                String namespace = id.substring(0, id.indexOf(':'));
                if (namespace.equals(MOD_ID)) {
                    assertTrue(models.contains(id.substring(id.indexOf(':') + 1)), file + " references unknown item " + id);
                } else if (!ALWAYS_LOADED.contains(namespace)) {
                    assertTrue(optional, file + " requires " + id + " from an optional mod");
                }
            }
        }
    }

    @Test
    void modsTomlDeclaresCreateRequiredAndCompatOptional() throws IOException {
        String toml = Files.readString(Path.of("src/main/templates/META-INF/neoforge.mods.toml"), StandardCharsets.UTF_8);
        Map<String, String> types = new TreeMap<>();
        for (String block : toml.split("\\[\\[dependencies\\.")) {
            String modId = field(block, "modId");
            if (modId != null) {
                types.put(modId, field(block, "type"));
            }
        }
        assertEquals("required", types.get("create"));
        for (String optional : List.of("jei", "jade", "kubejs", "farmersdelight", "sliceanddice")) {
            assertEquals("optional", types.get(optional), optional + " must stay an optional dependency");
        }
    }

    @Test
    void modsTomlLogoIsShippedSquare() throws IOException {
        String toml = Files.readString(Path.of("src/main/templates/META-INF/neoforge.mods.toml"), StandardCharsets.UTF_8);
        String logo = field(toml.split("\\[\\[dependencies\\.")[0], "logoFile");
        assertNotNull(logo, "no logoFile in neoforge.mods.toml");
        byte[] png = Files.readAllBytes(Path.of("src/main/resources", logo));
        var header = java.nio.ByteBuffer.wrap(png, 16, 8);
        int width = header.getInt();
        int height = header.getInt();
        assertEquals(width, height, logo + " must be square");
    }

    private static String field(String block, String name) {
        for (String line : block.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith(name + "=")) {
                return trimmed.substring(name.length() + 1).replace("\"", "").trim();
            }
        }
        return null;
    }

    private static Map<String, String> lang(String locale) throws IOException {
        Map<String, String> map = new TreeMap<>();
        json(LANG.resolve(locale + ".json")).entrySet().forEach(e -> map.put(e.getKey(), e.getValue().getAsString()));
        return map;
    }

    private static Set<String> itemModelIds() throws IOException {
        try (Stream<Path> files = Files.list(ITEM_MODELS)) {
            Set<String> ids = new TreeSet<>();
            files.forEach(p -> ids.add(p.getFileName().toString().replace(".json", "")));
            if (ids.isEmpty()) {
                fail("No generated item model; run ./gradlew runData");
            }
            return ids;
        }
    }

    // Textures of a model, merged along its parents in our namespace (a block item inherits its block
    // model; a generated model may be the child of a hand-made one in src/main/resources that it
    // only overrides a texture of). The child's entries win, as in the game.
    private static JsonObject texturesOf(Path model) throws IOException {
        JsonObject json = json(model);
        JsonObject textures = new JsonObject();
        if (json.has("parent") && json.get("parent").getAsString().startsWith(MOD_ID + ":")) {
            String parent = "assets/" + MOD_ID + "/models/" + json.get("parent").getAsString().substring(MOD_ID.length() + 1) + ".json";
            Path generated = GENERATED.resolve(parent);
            JsonObject inherited = texturesOf(Files.isRegularFile(generated) ? generated : MAIN.resolve(parent));
            if (inherited != null) {
                inherited.entrySet().forEach(e -> textures.add(e.getKey(), e.getValue()));
            }
        }
        if (json.has("textures")) {
            json.getAsJsonObject("textures").entrySet().forEach(e -> textures.add(e.getKey(), e.getValue()));
        }
        return textures.size() == 0 ? null : textures;
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    // Width and height from the IHDR chunk, which always directly follows the 8-byte signature.
    private static int[] pngSize(Path png) throws IOException {
        try (InputStream in = Files.newInputStream(png); DataInputStream data = new DataInputStream(in)) {
            data.skipNBytes(16);
            return new int[] {data.readInt(), data.readInt()};
        }
    }
}
