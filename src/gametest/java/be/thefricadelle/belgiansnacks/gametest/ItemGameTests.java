/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import be.thefricadelle.belgiansnacks.registry.BSFoods;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Registration, food, tag and creative tab contracts of the base items.
 * Run with {@code ./gradlew runGameTestServer}.
 */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemGameTests {
    private static final String TEMPLATE = "empty";

    // Recipe ids and KubeJS scripts refer to these: renaming or removing one is a breaking change.
    private static final Set<String> EXPECTED_IDS = Set.of(
        "minced_pork", "minced_beef", "minced_chicken", "beef_tallow", "bread_crumbs", "fricadelle_paste",
        "raw_fricadelle", "fricadelle", "belgian_spices", "exceptional_paste", "incomplete_the_fricadelle",
        "raw_the_fricadelle", "the_fricadelle", "absolute_paste", "raw_ultimate_fricadelle", "ultimate_fricadelle",
        "frying_oil_bucket", "melted_beef_tallow_bucket", "mayonnaise_bucket", "curry_ketchup_bucket",
        "fryer", "supreme_grinder");

    private ItemGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void registeredIdsAreExactlyTheExpectedOnes(GameTestHelper helper) {
        Set<String> actual = new TreeSet<>(modItems().stream()
            .map(item -> BuiltInRegistries.ITEM.getKey(item).getPath())
            .toList());
        helper.assertTrue(actual.equals(new TreeSet<>(EXPECTED_IDS)),
            "Registered items " + actual + " differ from " + new TreeSet<>(EXPECTED_IDS));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void onlyTheFricadellesAreFood(GameTestHelper helper) {
        List<Item> fricadelles = List.of(BSItems.FRICADELLE.get(), BSItems.THE_FRICADELLE.get(), BSItems.ULTIMATE_FRICADELLE.get());
        for (Item item : modItems()) {
            boolean food = new ItemStack(item).has(DataComponents.FOOD);
            helper.assertTrue(food == fricadelles.contains(item),
                BuiltInRegistries.ITEM.getKey(item) + (food ? " should not be edible" : " should be edible"));
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void foodValuesComeFromBSFoods(GameTestHelper helper) {
        assertFood(helper, BSItems.FRICADELLE.get(), BSFoods.FRICADELLE);
        assertFood(helper, BSItems.THE_FRICADELLE.get(), BSFoods.THE_FRICADELLE);
        assertFood(helper, BSItems.ULTIMATE_FRICADELLE.get(), BSFoods.ULTIMATE_FRICADELLE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void ultimateFricadelleIsEpicAndShiny(GameTestHelper helper) {
        ItemStack stack = BSItems.ULTIMATE_FRICADELLE.asStack();
        helper.assertTrue(stack.getRarity() == Rarity.EPIC, "THE_FRICADELLE should be epic");
        helper.assertTrue(stack.hasFoil(), "THE_FRICADELLE should have the enchantment glint");
        helper.assertFalse(BSItems.FRICADELLE.asStack().hasFoil(), "Fricadelle should not glint");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void incompleteItemIsASequencedAssemblyItem(GameTestHelper helper) {
        helper.assertTrue(BSItems.INCOMPLETE_THE_FRICADELLE.get() instanceof SequencedAssemblyItem,
            "incomplete_the_fricadelle must be a SequencedAssemblyItem for Create's assembly chain");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void eatingAFricadelleFeedsThePlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(0);
        ItemStack stack = BSItems.FRICADELLE.asStack(2);

        ItemStack left = stack.finishUsingItem(helper.getLevel(), player);

        helper.assertValueEqual(player.getFoodData().getFoodLevel(), BSFoods.FRICADELLE.nutrition(), "food level");
        helper.assertValueEqual(left.getCount(), 1, "stack size after eating");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void onlyTheUltimateCanBeEatenWhenFull(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);

        player.setItemInHand(InteractionHand.MAIN_HAND, BSItems.FRICADELLE.asStack());
        boolean fricadelle = BSItems.FRICADELLE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction();
        player.stopUsingItem();

        player.setItemInHand(InteractionHand.MAIN_HAND, BSItems.ULTIMATE_FRICADELLE.asStack());
        boolean ultimate = BSItems.ULTIMATE_FRICADELLE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction();

        helper.assertFalse(fricadelle, "A full player should not start eating a fricadelle");
        helper.assertTrue(ultimate, "THE_FRICADELLE should be edible even when full");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyFoodOfTheModIsBlacklistedFromTheGrinder(GameTestHelper helper) {
        // Otherwise the Supreme Grinder could eat its own output.
        for (Item item : modItems()) {
            ItemStack stack = new ItemStack(item);
            if (stack.has(DataComponents.FOOD)) {
                helper.assertTrue(stack.is(BSTags.GRINDER_BLACKLIST), BuiltInRegistries.ITEM.getKey(item) + " is food but not blacklisted");
            }
        }
        helper.assertFalse(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE).is(BSTags.GRINDER_BLACKLIST), "the enchanted golden apple counts (D8)");
        helper.assertTrue(new ItemStack(Items.OMINOUS_BOTTLE).is(BSTags.GRINDER_BLACKLIST), "the ominous bottle is blacklisted (D8)");
        helper.assertFalse(new ItemStack(Items.BREAD).is(BSTags.GRINDER_BLACKLIST), "ordinary food must stay accepted");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tagsResolveAtRuntime(GameTestHelper helper) {
        helper.assertTrue(BSItems.MINCED_BEEF.asStack().is(BSTags.MINCED_BEEF), "our minced beef in minced_meats/beef");
        for (var entry : List.of(BSItems.MINCED_PORK, BSItems.MINCED_BEEF, BSItems.MINCED_CHICKEN)) {
            helper.assertTrue(entry.asStack().is(BSTags.MINCED_MEATS), entry.getId() + " in minced_meats");
        }
        helper.assertTrue(new ItemStack(Items.CAKE).is(BSTags.GRINDER_EXTRA_FOODS), "cake in grinder/extra_foods");
        for (var entry : List.of(BSItems.FRICADELLE, BSItems.THE_FRICADELLE, BSItems.ULTIMATE_FRICADELLE)) {
            ItemStack stack = entry.asStack();
            helper.assertTrue(stack.is(Tags.Items.FOODS_COOKED_MEAT), entry.getId() + " in c:foods/cooked_meat");
            // c:foods includes c:foods/cooked_meat: checks the convention nesting still reaches us.
            helper.assertTrue(stack.is(Tags.Items.FOODS), entry.getId() + " in c:foods");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void creativeTabListsEveryItem(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        // The GameTest server never builds tab contents on its own; there is no client here to disturb.
        CreativeModeTabs.tryRebuildTabContents(server.getWorldData().enabledFeatures(), true, server.registryAccess());
        var tab = BSCreativeTabs.MAIN.get();
        for (Item item : modItems()) {
            helper.assertTrue(tab.contains(new ItemStack(item)), BuiltInRegistries.ITEM.getKey(item) + " missing from the creative tab");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyItemHasAnEnglishName(GameTestHelper helper) {
        Language language = Language.getInstance();
        for (Item item : modItems()) {
            helper.assertTrue(language.has(item.getDescriptionId()), "No en_us name for " + item.getDescriptionId());
        }
        helper.assertTrue(language.has(BSCreativeTabs.MAIN_TITLE), "No en_us name for the creative tab");
        helper.succeed();
    }

    private static void assertFood(GameTestHelper helper, Item item, FoodProperties expected) {
        FoodProperties actual = new ItemStack(item).get(DataComponents.FOOD);
        helper.assertValueEqual(actual, expected, BuiltInRegistries.ITEM.getKey(item) + " food");
    }

    private static List<Item> modItems() {
        return BuiltInRegistries.ITEM.stream()
            .filter(item -> {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                return id.getNamespace().equals(BelgianSnacks.MOD_ID);
            })
            .toList();
    }
}
