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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Removing the mod must never break a world (spec v2, section 3): our ids stay frozen, and nothing
 * fragile of ours is left in data that outlives the mod.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RegistryGameTests {
    private static final String TEMPLATE = "empty";

    // Registries whose entries are saved inside players, villagers or other mods' items: an entry of
    // ours there would be decoded without us once the mod is removed.
    private static final Set<ResourceLocation> FRAGILE = Set.of(
        NeoForgeRegistries.Keys.ATTACHMENT_TYPES.location(),
        Registries.MOB_EFFECT.location(),
        Registries.ATTRIBUTE.location(),
        Registries.VILLAGER_PROFESSION.location(),
        Registries.POINT_OF_INTEREST_TYPE.location(),
        Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.location());

    private RegistryGameTests() {
    }

    // Rule R1: every registered id is in the frozen list and every frozen id is still registered.
    // On a difference, the actual list is written next to the server for review.
    @GameTest(template = TEMPLATE)
    public static void everyRegisteredIdIsFrozen(GameTestHelper helper) {
        SortedSet<String> registered = FrozenIds.registered();
        SortedSet<String> frozen = FrozenIds.registryLines();
        SortedSet<String> removed = new TreeSet<>(frozen);
        removed.removeAll(registered);
        SortedSet<String> added = new TreeSet<>(registered);
        added.removeAll(frozen);
        if (!removed.isEmpty() || !added.isEmpty()) {
            Path actual = helper.getLevel().getServer().getServerDirectory().resolve("frozen-ids.actual.txt");
            try {
                Files.write(actual, registered, StandardCharsets.UTF_8);
            } catch (IOException e) {
                BelgianSnacks.LOGGER.warn("Could not write {}", actual, e);
            }
            helper.fail("Frozen ids no longer registered (breaking): " + removed
                + "; registered ids missing from frozen-ids.txt: " + added + "; actual list in " + actual.toAbsolutePath());
        }
        helper.succeed();
    }

    // Rule R2: nothing of ours is saved where it would outlive the mod.
    @GameTest(template = TEMPLATE)
    public static void nothingFragileIsRegistered(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (String line : FrozenIds.registered()) {
            ResourceLocation registry = ResourceLocation.parse(line.substring(0, line.indexOf(' ')));
            if (FRAGILE.contains(registry)) {
                problems.add("registered in " + registry + ": " + line.substring(line.indexOf(' ') + 1));
            }
        }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id.getNamespace().equals(BelgianSnacks.MOD_ID) && type.canSerialize()) {
                problems.add("entity saved with the chunk: " + id);
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals(BelgianSnacks.MOD_ID)) {
                continue;
            }
            for (DataComponentType<?> type : item.components().keySet()) {
                ResourceLocation component = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
                if (component != null && component.getNamespace().equals(BelgianSnacks.MOD_ID)) {
                    problems.add("our component " + component + " on " + id);
                }
            }
        }
        helper.assertTrue(problems.isEmpty(), "fragile data: " + problems);
        helper.succeed();
    }
}
