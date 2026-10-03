/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.food.FoodIndexRules;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The food index against its definition (docs/03): foods + extra tag - blacklists, sorted, cheap,
 * and the two commands that read it. Also run with Farmer's Delight by runGameTestServerCompat.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodIndexGameTests {
    private static final String TEMPLATE = "empty";

    private FoodIndexGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void indexHoldsFoodsAndExtrasButNotTheBlacklist(GameTestHelper helper) {
        FoodIndex.Snapshot index = FoodIndex.server();
        helper.assertTrue(index.size() > 30, "only " + index.size() + " foods");
        for (String food : List.of("minecraft:apple", "minecraft:bread", "minecraft:cooked_beef")) {
            helper.assertTrue(index.contains(id(food)), food + " missing");
        }
        helper.assertTrue(index.contains(id("minecraft:cake")), "cake comes from the extra tag");
        helper.assertTrue(index.sources().get(id("minecraft:cake")) == FoodIndexRules.Source.EXTRA, "cake source should be extra");
        helper.assertTrue(index.sources().get(id("minecraft:apple")) == FoodIndexRules.Source.FOOD, "apple source should be food");
        helper.assertTrue(index.contains(id("minecraft:enchanted_golden_apple")), "the enchanted golden apple counts (D8)");
        for (String excluded : List.of("minecraft:ominous_bottle", "create_belgian_snacks:fricadelle",
            "create_belgian_snacks:the_fricadelle", "create_belgian_snacks:ultimate_fricadelle", "minecraft:stone")) {
            helper.assertFalse(index.contains(id(excluded)), excluded + " must not count");
        }
        helper.assertTrue(index.excluded() >= 4, "blacklist exclusions counted: " + index.excluded());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void indexIsSortedAndStable(GameTestHelper helper) {
        FoodIndex.Snapshot index = FoodIndex.server();
        // Sorted by the full "namespace:path" string, so each mod's foods stay together.
        List<ResourceLocation> sorted = new ArrayList<>(index.ids());
        sorted.sort(java.util.Comparator.comparing(ResourceLocation::toString));
        helper.assertTrue(sorted.equals(index.ids()), "the index is not sorted by full id");
        helper.assertValueEqual(FoodIndex.compute(Set.of(), Set.of()).fingerprint(), index.fingerprint(), "a recompute gives the same list");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void configBlacklistsRemoveModsAndItems(GameTestHelper helper) {
        FoodIndex.Snapshot noVanilla = FoodIndex.compute(Set.of("minecraft"), Set.of());
        helper.assertTrue(noVanilla.ids().stream().noneMatch(i -> i.getNamespace().equals("minecraft")), "a blacklisted mod left foods behind");
        FoodIndex.Snapshot noApple = FoodIndex.compute(Set.of(), Set.of("minecraft:apple"));
        helper.assertFalse(noApple.contains(id("minecraft:apple")), "a blacklisted item stayed");
        helper.assertTrue(noApple.contains(id("minecraft:bread")), "a blacklisted item took others with it");
        helper.assertTrue(FoodIndex.server().contains(id("minecraft:apple")), "computing a variant changed the live index");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void recomputingIsCheap(GameTestHelper helper) {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            best = Math.min(best, FoodIndex.compute(Set.of(), Set.of()).computeNanos());
        }
        helper.assertTrue(best < 50_000_000L, "food index took " + best / 1_000_000 + " ms (budget 50 ms)");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void farmersDelightFoodsCountWhenLoaded(GameTestHelper helper) {
        boolean loaded = ModList.get().isLoaded("farmersdelight");
        boolean present = FoodIndex.server().ids().stream().anyMatch(i -> i.getNamespace().equals("farmersdelight"));
        helper.assertTrue(present == loaded, loaded ? "Farmer's Delight foods missing" : "foods from an absent mod");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void countAndExportCommands(GameTestHelper helper) throws Exception {
        List<String> messages = new ArrayList<>();
        CommandSource recorder = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) {
                messages.add(component.getString());
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
        var server = helper.getLevel().getServer();
        CommandSourceStack source = new CommandSourceStack(recorder, Vec3.ZERO, Vec2.ZERO, helper.getLevel(), 2, "test",
            Component.literal("test"), server, null);

        server.getCommands().performPrefixedCommand(source, "belgiansnacks foods count");
        helper.assertTrue(!messages.isEmpty() && messages.get(0).startsWith(Integer.toString(FoodIndex.server().size())),
            "count answered " + messages);

        Path file = FMLPaths.CONFIGDIR.get().resolve(BelgianSnacks.MOD_ID).resolve("foods_export.csv");
        Files.deleteIfExists(file);
        server.getCommands().performPrefixedCommand(source, "belgiansnacks foods export");
        helper.assertTrue(Files.isRegularFile(file), "export wrote nothing, answered " + messages);
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        helper.assertValueEqual(lines.get(0), "id,modid,nutrition,saturation,source,has_recipe", "CSV header");
        helper.assertValueEqual(lines.size() - 1, FoodIndex.server().size(), "one line per food");
        helper.assertTrue(lines.contains("minecraft:bread,minecraft,5,6.00,food,true"), "bread line (crafted from wheat)");
        helper.assertTrue(lines.stream().anyMatch(l -> l.startsWith("minecraft:cake,minecraft,,,extra,")), "cake line from the extra tag");
        helper.succeed();
    }

    private static ResourceLocation id(String id) {
        return ResourceLocation.parse(id);
    }
}
