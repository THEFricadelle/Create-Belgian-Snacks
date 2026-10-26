/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSFoods;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSDishes;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import be.thefricadelle.belgiansnacks.registry.BSSweets;
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
    // The list is the minecraft:item part of frozen-ids.txt (spec v2, rule R1).
    private static final Set<String> EXPECTED_IDS = FrozenIds.paths("minecraft:item");

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

    // The three fricadelles, and the finished snacks of the v2 menu; nothing in between is edible.
    private static List<Item> fricadelles() {
        return List.of(BSItems.FRICADELLE.get(), BSItems.THE_FRICADELLE.get(), BSItems.ULTIMATE_FRICADELLE.get());
    }

    private static List<Item> snacks() {
        List<Item> snacks = new java.util.ArrayList<>(List.of(BSItems.FRIES.get(), BSItems.FRIES_BEEF_TALLOW.get(),
            BSItems.CONE_OF_FRIES.get(), BSItems.CONE_OF_FRIES_BEEF_TALLOW.get()));
        BSItems.SAUCED_CONES.values().forEach(cone -> snacks.add(cone.get()));
        BSItems.SAUCED_TALLOW_CONES.values().forEach(cone -> snacks.add(cone.get()));
        return snacks;
    }

    // M16 friterie snacks, written out.
    private static List<Item> friterieSnacks() {
        return List.of(BSSnacks.MITRAILLETTE.get(), BSSnacks.BOULET.get(), BSSnacks.BOULETS_LIEGEOIS.get(), BSSnacks.SHRIMP_CROQUETTE.get(), BSSnacks.CHEESE_CROQUETTE.get(), BSSnacks.CHICKEN_CROQUETTE.get(), BSSnacks.LUCIFER.get(), BSSnacks.POTATO_CROQUETTE.get(), BSSnacks.CHICKEN_FINGERS.get(), BSSnacks.FISH_STICKS.get(), BSSnacks.CHEESE_STICKS.get(), BSSnacks.CERVELAS.get(), BSSnacks.MEXICANO.get(), BSSnacks.VIANDELLE.get(), BSSnacks.LOEMPIA.get(), BSSnacks.FILET_AMERICAIN.get(), BSSnacks.PISTOLET_AMERICAIN.get(), BSSnacks.AMERICAIN_FRITES.get(), BSSnacks.PATTY.get(), BSSnacks.FRITERIE_BURGER.get(), BSSnacks.FRITERIE_CHEESEBURGER.get(), BSSnacks.HOT_DOG.get(), BSSnacks.GYROS.get(), BSSnacks.PITA.get(), BSSnacks.BROCHETTE.get(), BSSnacks.BAMI_DISC.get(), BSSnacks.NASI_DISC.get(), BSSnacks.SQUID_RINGS.get(), BSSnacks.CHICKEN_NUGGETS.get(), BSSnacks.CHICKEN_CRUNCH.get(), BSSnacks.FRIED_CHICKEN_WINGS.get(), BSSnacks.SPICY_CHICKEN_WINGS.get(), BSSnacks.ONION_RINGS.get());
    }

    // M17 Belgian dishes, written out.
    private static List<Item> belgianDishes() {
        return List.of(BSDishes.MOULES_MARINIERES.get(), BSDishes.MOULES_A_LA_BIERE.get(), BSDishes.MOULES_FRITES.get(), BSDishes.CARBONNADE.get(), BSDishes.CARBONNADE_FRITES.get(), BSDishes.STOEMP.get(), BSDishes.STOEMP_SAUCISSE.get(), BSDishes.WATERZOOI.get(), BSDishes.LAPIN_A_LA_GUEUZE.get(), BSDishes.BRUSSELS_SPROUTS_BACON.get(), BSDishes.ASPARAGUS_FLEMISH.get(), BSDishes.HOP_SHOOTS_CREAM.get(), BSDishes.SALADE_LIEGEOISE.get(), BSDishes.HOCHEPOT.get(), BSDishes.ESCAVECHE.get(), BSDishes.STEAK_FRITES.get(), BSDishes.CHICONS_AU_GRATIN.get(), BSDishes.VOL_AU_VENT.get(), BSDishes.BOUDIN.get(), BSDishes.BOUDIN_COMPOTE.get(), BSDishes.FLAMICHE.get(), BSDishes.CROQUE_MONSIEUR.get());
    }

    // M19 sweets, written out.
    private static List<Item> sweets() {
        return List.of(BSSweets.SPECULOOS.get(), BSSweets.PISTOLET_SPECULOOS.get(), BSSweets.BRUSSELS_WAFFLE_SPECULOOS.get(), BSSweets.CUBERDON.get(), BSSweets.PRALINE.get(), BSSweets.PRALINE_KRIEK.get(), BSSweets.BALLOTIN.get(), BSSweets.BABELUTTE.get(), BSSweets.CROUSTILLONS.get(), BSSweets.CROUSTILLONS_SUGARED.get(), BSSweets.BOUQUETTE.get(), BSSweets.BOUQUETTE_CASSONADE.get(), BSSweets.COUQUE_DE_DINANT.get(), BSSweets.TARTE_AU_RIZ.get(), BSSweets.TARTE_AU_SUCRE.get(), BSSweets.MATTENTAART.get(), BSSweets.CRAQUELIN.get(), BSSweets.CRAMIQUE.get(), BSSweets.PAIN_A_LA_GRECQUE.get(), BSSweets.MERVEILLEUX.get(), BSSweets.DAME_BLANCHE.get(), BSSweets.HOT_CHOCOLATE_BOTTLE.get());
    }

    // M18 waffles, written out.
    private static List<Item> waffles() {
        return List.of(BSWaffles.LIEGE_WAFFLE.get(), BSWaffles.BRUSSELS_WAFFLE.get(), BSWaffles.BRUSSELS_WAFFLE_SUGARED.get(),
            BSWaffles.BRUSSELS_WAFFLE_CHOCOLATE.get(), BSWaffles.LACQUEMANT.get());
    }

    // v2 base ingredients that can be eaten as they are (M13).
    private static List<Item> edibleIngredients() {
        return List.of(BSItems.HERVE_CHEESE.get(), BSItems.PISTOLET.get(), BSItems.ARDENNES_HAM.get(), BSItems.MERINGUE.get(), BSItems.RAISINS.get(), BSItems.APPLE_COMPOTE.get(), BSItems.VANILLA_ICE_CREAM.get());
    }

    @GameTest(template = TEMPLATE)
    public static void onlyFricadellesAndFinishedSnacksAreFood(GameTestHelper helper) {
        for (Item item : modItems()) {
            boolean food = new ItemStack(item).has(DataComponents.FOOD);
            boolean expected = fricadelles().contains(item) || snacks().contains(item) || friterieSnacks().contains(item) || belgianDishes().contains(item) || waffles().contains(item) || sweets().contains(item)
                || edibleIngredients().contains(item);
            helper.assertTrue(food == expected,
                BuiltInRegistries.ITEM.getKey(item) + (food ? " should not be edible" : " should be edible"));
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void foodValuesComeFromBSFoods(GameTestHelper helper) {
        assertFood(helper, BSItems.FRICADELLE.get(), BSFoods.FRICADELLE);
        assertFood(helper, BSItems.THE_FRICADELLE.get(), BSFoods.THE_FRICADELLE);
        assertFood(helper, BSItems.ULTIMATE_FRICADELLE.get(), BSFoods.ULTIMATE_FRICADELLE);
        assertFood(helper, BSItems.FRIES.get(), BSFoods.FRIES);
        assertFood(helper, BSItems.FRIES_BEEF_TALLOW.get(), BSFoods.FRIES_BEEF_TALLOW);
        assertFood(helper, BSItems.CONE_OF_FRIES.get(), BSFoods.CONE_OF_FRIES);
        assertFood(helper, BSItems.CONE_OF_FRIES_BEEF_TALLOW.get(), BSFoods.CONE_OF_FRIES_BEEF_TALLOW);
        BSItems.SAUCED_CONES.values().forEach(cone -> assertFood(helper, cone.get(), BSFoods.CONE_OF_FRIES_SAUCED));
        BSItems.SAUCED_TALLOW_CONES.values().forEach(cone -> assertFood(helper, cone.get(), BSFoods.CONE_OF_FRIES_BEEF_TALLOW_SAUCED));
        assertFood(helper, BSItems.HERVE_CHEESE.get(), BSFoods.HERVE_CHEESE);
        assertFood(helper, BSItems.PISTOLET.get(), BSFoods.PISTOLET);
        assertFood(helper, BSItems.ARDENNES_HAM.get(), BSFoods.ARDENNES_HAM);
        assertFood(helper, BSItems.MERINGUE.get(), BSFoods.MERINGUE);
        assertFood(helper, BSItems.RAISINS.get(), BSFoods.RAISINS);
        assertFood(helper, BSItems.APPLE_COMPOTE.get(), BSFoods.APPLE_COMPOTE);
        assertFood(helper, BSItems.VANILLA_ICE_CREAM.get(), BSFoods.VANILLA_ICE_CREAM);
        assertFood(helper, BSSnacks.MITRAILLETTE.get(), BSFoods.MITRAILLETTE);
        assertFood(helper, BSDishes.STEAK_FRITES.get(), BSFoods.DISH_WITH_FRIES);
        assertFood(helper, BSWaffles.LIEGE_WAFFLE.get(), BSFoods.LIEGE_WAFFLE);
        assertFood(helper, BSSweets.BALLOTIN.get(), BSFoods.BALLOTIN);
        assertFood(helper, BSSweets.TARTE_AU_SUCRE.get(), BSFoods.TART);
        assertFood(helper, BSWaffles.BRUSSELS_WAFFLE_CHOCOLATE.get(), BSFoods.BRUSSELS_WAFFLE_CHOCOLATE);
        assertFood(helper, BSDishes.FLAMICHE.get(), BSFoods.SIDE_DISH_LARGE);
        for (var bowl : List.of(BSDishes.MOULES_FRITES, BSDishes.CARBONNADE, BSDishes.WATERZOOI, BSDishes.BOUDIN_COMPOTE)) {
            helper.assertTrue(bowl.asStack().get(net.minecraft.core.component.DataComponents.FOOD).usingConvertsTo()
                .map(stack -> stack.is(Items.BOWL)).orElse(false), bowl.getId() + " gives its bowl back");
        }
        helper.assertFalse(BSDishes.STEAK_FRITES.asStack().get(net.minecraft.core.component.DataComponents.FOOD).usingConvertsTo().isPresent(),
            "steak-frites is served on no bowl");
        assertFood(helper, BSSnacks.BOULETS_LIEGEOIS.get(), BSFoods.BOULETS_LIEGEOIS);
        assertFood(helper, BSSnacks.FRITERIE_CHEESEBURGER.get(), BSFoods.CHEESEBURGER);
        assertFood(helper, BSSnacks.AMERICAIN_FRITES.get(), BSFoods.AMERICAIN_FRITES);
        helper.assertTrue(BSSnacks.BOULETS_LIEGEOIS.asStack().get(net.minecraft.core.component.DataComponents.FOOD).usingConvertsTo()
            .map(stack -> stack.is(Items.BOWL)).orElse(false), "boulets liegeois give their bowl back");
        // Every sauce a cone carries has its fluid, and the other way round.
        helper.assertValueEqual(BSItems.CONE_SAUCES.keySet(), BSFluids.CONE_SAUCES.keySet(), "cone sauces");
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

    // The fricadelles stay out, or the Supreme Grinder could eat its own output; the finished snacks
    // of the v2 menu count, so THE_FRICADELLE asks for them too (V-D1).
    @GameTest(template = TEMPLATE)
    public static void fricadellesAreBlacklistedAndSnacksCount(GameTestHelper helper) {
        for (Item item : fricadelles()) {
            helper.assertTrue(new ItemStack(item).is(BSTags.GRINDER_BLACKLIST), BuiltInRegistries.ITEM.getKey(item) + " is not blacklisted");
        }
        for (Item item : snacks()) {
            helper.assertFalse(new ItemStack(item).is(BSTags.GRINDER_BLACKLIST), BuiltInRegistries.ITEM.getKey(item) + " is blacklisted");
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
        for (Item item : snacks()) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(stack.is(BSTags.C_FOODS_FRIES), BuiltInRegistries.ITEM.getKey(item) + " in c:foods/fries");
            helper.assertTrue(stack.is(Tags.Items.FOODS), BuiltInRegistries.ITEM.getKey(item) + " in c:foods");
            helper.assertFalse(stack.is(Tags.Items.FOODS_COOKED_MEAT), BuiltInRegistries.ITEM.getKey(item) + " in c:foods/cooked_meat");
        }
        for (Item item : sweets()) {
            helper.assertTrue(new ItemStack(item).is(Tags.Items.FOODS), BuiltInRegistries.ITEM.getKey(item) + " in c:foods");
        }
        helper.assertTrue(BSSweets.PRALINE_KRIEK.asStack().is(BSTags.PRALINES), "kriek praline in #pralines");
        helper.assertTrue(new ItemStack(Items.COCOA_BEANS).is(BSTags.PRALINE_FILLINGS), "cocoa beans fill a praline");
        for (Item item : waffles()) {
            helper.assertTrue(new ItemStack(item).is(Tags.Items.FOODS), BuiltInRegistries.ITEM.getKey(item) + " in c:foods");
        }
        for (Item item : belgianDishes()) {
            helper.assertTrue(new ItemStack(item).is(Tags.Items.FOODS), BuiltInRegistries.ITEM.getKey(item) + " in c:foods");
        }
        helper.assertTrue(be.thefricadelle.belgiansnacks.registry.BSCrops.LEEK.asStack().is(BSTags.ONIONS), "the leek stands in for an onion");
        for (Item item : friterieSnacks()) {
            helper.assertTrue(new ItemStack(item).is(Tags.Items.FOODS), BuiltInRegistries.ITEM.getKey(item) + " in c:foods");
            helper.assertFalse(new ItemStack(item).is(BSTags.C_FOODS_FRIES), BuiltInRegistries.ITEM.getKey(item) + " in c:foods/fries");
        }
        helper.assertTrue(BSSnacks.BOULET.asStack().is(Tags.Items.FOODS_COOKED_MEAT), "boulet in c:foods/cooked_meat");
        helper.assertTrue(BSSnacks.FISH_STICKS.asStack().is(Tags.Items.FOODS_COOKED_FISH), "fish sticks in c:foods/cooked_fish");
        for (var meat : List.of(BSItems.FRICADELLE, BSSnacks.BOULET, BSSnacks.CERVELAS, BSSnacks.MEXICANO, BSSnacks.VIANDELLE,
            BSSnacks.FILET_AMERICAIN, BSSnacks.CHICKEN_FINGERS, BSSnacks.BROCHETTE)) {
            helper.assertTrue(meat.asStack().is(BSTags.MITRAILLETTE_MEATS), meat.getId() + " in mitraillette_meats");
        }
        helper.assertTrue(BSItems.FRIES.asStack().is(BSTags.FRIES), "fries in #fries");
        helper.assertFalse(BSItems.FRIES_BEEF_TALLOW.asStack().is(BSTags.FRIES), "tallow fries in #fries: a plain cone would take them");
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
