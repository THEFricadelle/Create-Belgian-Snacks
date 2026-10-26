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

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.decor.WallPanelBlock;
import be.thefricadelle.belgiansnacks.registry.BSDecor;
import be.thefricadelle.belgiansnacks.registry.BSDishes;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSSnacks;
import be.thefricadelle.belgiansnacks.registry.BSSweets;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** v2 decoration and the menu advancements (docs/13, section 7.2 and the M21 milestone). */
@GameTestHolder(BelgianSnacks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecorGameTests {
    private static final String TEMPLATE = "empty";

    private DecorGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void neonsLightTheFriterieAndStoreNothing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        for (var neon : BSDecor.NEONS) {
            helper.setBlock(pos, neon.getDefaultState());
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) == null, neon.getId() + " has a block entity");
            helper.assertValueEqual(neon.getDefaultState().getLightEmission(), 15, neon.getId() + " light");
        }
        helper.runAfterDelay(10, () -> {
            helper.assertValueEqual(helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(pos.above())), 14, "block light above a neon");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void signsAndNeonsFaceThePlayerAndDropThemselves(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(new BlockPos(1, 1, 1));
        for (var panel : List.of(BSDecor.FRITERIE_SIGN, BSDecor.CHIP_SHOP_SIGN, BSDecor.GREEN_NEON)) {
            for (Direction looking : Direction.Plane.HORIZONTAL) {
                player.setYRot(looking.toYRot());
                var state = panel.get().getStateForPlacement(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, panel.asStack(),
                    new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
                helper.assertValueEqual(state.getValue(WallPanelBlock.FACING), looking.getOpposite(), panel.getId() + " looking " + looking);
            }
        }
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, BSDecor.FRITUUR_SIGN.getDefaultState());
        helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
        boolean dropped = helper.findEntities(EntityType.ITEM, 2, 1, 2, 2).stream().map(ItemEntity::getItem)
            .anyMatch(stack -> stack.is(BSDecor.FRITUUR_SIGN.asItem()));
        helper.assertTrue(dropped, "a broken sign drops itself");
        for (var panel : List.of(BSDecor.FRITERIE_SIGN, BSDecor.FRITUUR_SIGN, BSDecor.CHIP_SHOP_SIGN, BSDecor.RED_NEON, BSDecor.YELLOW_NEON,
            BSDecor.BLUE_NEON, BSDecor.GREEN_NEON)) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(BelgianSnacks.asResource("crafting/" + panel.getId().getPath())).isPresent(),
                "crafting/" + panel.getId().getPath() + " not loaded");
        }
        helper.succeed();
    }

    // Tour of Belgium: one food of each family, not before the fifth.
    @GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "tour_de_belgique")
    public static void theTourOfBelgiumNeedsEveryFamily(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.inLevel(helper, new Vec3(2.5, 1, 2.5));
        AdvancementHolder tour = helper.getLevel().getServer().getAdvancements().get(BelgianSnacks.asResource("tour_de_belgique"));
        helper.assertTrue(tour != null, "tour_de_belgique is not loaded");
        List<ItemStack> menu = List.of(BSItems.CONE_OF_FRIES.asStack(), BSSnacks.MITRAILLETTE.asStack(), BSDishes.CARBONNADE.asStack(),
            BSWaffles.LIEGE_WAFFLE.asStack(), BSSweets.SPECULOOS.asStack());
        helper.assertTrue(menu.get(0).is(BSTags.MENU_FRIES) && menu.get(1).is(BSTags.MENU_FRITERIE) && menu.get(2).is(BSTags.MENU_DISHES)
            && menu.get(3).is(BSTags.MENU_WAFFLES) && menu.get(4).is(BSTags.MENU_SWEETS), "one food of each family");
        helper.assertFalse(BSSnacks.RAW_BOULET.asStack().is(BSTags.MENU_FRITERIE), "a raw boulet is not on the menu");
        for (int i = 0; i < menu.size(); i++) {
            player.getFoodData().setFoodLevel(0);
            menu.get(i).finishUsingItem(helper.getLevel(), player);
            boolean done = player.getAdvancements().getOrStartProgress(tour).isDone();
            helper.assertTrue(done == (i == menu.size() - 1), "after " + (i + 1) + " families the advancement is " + (done ? "" : "not ") + "done");
        }
        TestPlayers.remove(helper, player);
        helper.succeed();
    }
}
