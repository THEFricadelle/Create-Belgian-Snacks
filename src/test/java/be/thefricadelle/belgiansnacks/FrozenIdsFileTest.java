/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * The frozen id list (spec v2, rule R1) is well formed and covers the data files; the game registries
 * are compared to it by {@code RegistryGameTests}.
 */
class FrozenIdsFileTest {
    private static final String MOD_ID = "create_belgian_snacks";
    static final Path FILE = Path.of("src/gametest/resources/" + MOD_ID + "/frozen-ids.txt");
    private static final Path ADVANCEMENTS = Path.of("src/generated/resources/data/" + MOD_ID + "/advancement");

    /** Paths of the frozen ids of one registry or data folder, e.g. {@code data/recipe}. */
    static Set<String> ids(String registry) {
        String prefix = registry + " " + MOD_ID + ":";
        Set<String> ids = new TreeSet<>();
        for (String line : entries()) {
            if (line.startsWith(prefix)) {
                ids.add(line.substring(prefix.length()));
            }
        }
        return ids;
    }

    private static List<String> entries() {
        try {
            List<String> entries = new ArrayList<>();
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    entries.add(line);
                }
            }
            return entries;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void linesAreSortedUniqueAndWellFormed() {
        List<String> entries = entries();
        assertEquals(new ArrayList<>(new TreeSet<>(entries)), entries, "frozen-ids.txt must stay sorted, without duplicates");
        Set<String> seen = new HashSet<>();
        for (String line : entries) {
            String[] parts = line.split(" ");
            assertEquals(2, parts.length, "not \"<registry> <id>\": " + line);
            assertTrue(parts[1].startsWith(MOD_ID + ":"), "not one of our ids: " + line);
            assertTrue(seen.add(line), "duplicate: " + line);
        }
    }

    // A removed advancement loses players' progress; a new one must be frozen with it.
    @Test
    void advancementsAreExactlyTheFrozenOnes() throws IOException {
        Set<String> files = new TreeSet<>();
        try (Stream<Path> paths = Files.walk(ADVANCEMENTS)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    String relative = ADVANCEMENTS.relativize(path).toString().replace('\\', '/');
                    files.add(relative.substring(0, relative.length() - ".json".length()));
                });
        }
        assertTrue(!files.isEmpty(), "no generated advancements: run ./gradlew runData");
        assertEquals(ids("data/advancement"), files);
    }
}
