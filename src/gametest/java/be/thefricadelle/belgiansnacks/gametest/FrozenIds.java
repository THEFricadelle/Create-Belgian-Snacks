/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * The frozen id list ({@code create_belgian_snacks/frozen-ids.txt}, spec v2 rule R1): every id the
 * mod ever shipped, one {@code <registry> <id>} per line. Worlds, other mods and KubeJS scripts keep
 * these ids, so one may never disappear. Lines whose registry has no namespace ({@code data/recipe},
 * {@code data/advancement}) are data files, checked by the JUnit tests.
 */
final class FrozenIds {
    static final String RESOURCE = "/create_belgian_snacks/frozen-ids.txt";

    private static SortedSet<String> lines;

    private FrozenIds() {
    }

    static synchronized SortedSet<String> lines() {
        if (lines == null) {
            try (InputStream in = FrozenIds.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(RESOURCE + " is missing from the gametest resources");
                }
                TreeSet<String> read = new TreeSet<>();
                for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                    String trimmed = line.strip();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        read.add(trimmed);
                    }
                }
                lines = Collections.unmodifiableSortedSet(read);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return lines;
    }

    /** The frozen lines of game registries, leaving the data files out. */
    static SortedSet<String> registryLines() {
        TreeSet<String> registries = new TreeSet<>();
        for (String line : lines()) {
            if (line.substring(0, line.indexOf(' ')).contains(":")) {
                registries.add(line);
            }
        }
        return registries;
    }

    /** Paths of the frozen ids of one registry, e.g. {@code minecraft:item}. */
    static SortedSet<String> paths(String registry) {
        String prefix = registry + " " + BelgianSnacks.MOD_ID + ":";
        TreeSet<String> paths = new TreeSet<>();
        for (String line : lines()) {
            if (line.startsWith(prefix)) {
                paths.add(line.substring(prefix.length()));
            }
        }
        return paths;
    }

    /** Every id of our namespace in every game registry, as frozen-file lines. */
    static SortedSet<String> registered() {
        TreeSet<String> ids = new TreeSet<>();
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            String name = registry.key().location().toString();
            for (ResourceLocation id : registry.keySet()) {
                if (id.getNamespace().equals(BelgianSnacks.MOD_ID)) {
                    ids.add(name + " " + id);
                }
            }
        }
        return ids;
    }
}
