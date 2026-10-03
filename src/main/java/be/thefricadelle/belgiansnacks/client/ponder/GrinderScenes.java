/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client.ponder;

import static be.thefricadelle.belgiansnacks.client.ponder.PonderTexts.say;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlock;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The Supreme Grinder (schematic ponder/supreme_grinder.nbt): hopper and chest below, shaft and
 * motor above, belt from the west. Its gauge is a block state, so the scenes set it directly.
 */
final class GrinderScenes {
    private GrinderScenes() {
    }

    static void collect(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "supreme_grinder");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos grinder = util.grid().at(2, 2, 2);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 1, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(grinder), Direction.DOWN);
        scene.idle(10);
        Selection drive = util.select().fromTo(2, 2, 2, 2, 4, 2);
        scene.world().showSection(util.select().fromTo(2, 3, 2, 2, 4, 2), Direction.DOWN);
        scene.world().setKineticSpeed(drive, 64);
        scene.idle(10);
        say(scene, "grinder.rotation", 60, util.vector().topOf(util.grid().at(2, 3, 2)));

        showBelt(scene, util);
        feed(scene, util, new ItemStack(Items.APPLE));
        scene.world().modifyBlock(grinder, state -> state.setValue(SupremeGrinderBlock.FILL, 1), false);
        say(scene, "grinder.once", 60, util.vector().blockSurface(grinder, Direction.WEST));

        scene.world().createItemOnBelt(util.grid().at(0, 2, 2), Direction.WEST, new ItemStack(Items.APPLE));
        scene.idle(25);
        say(scene, "grinder.refuse", 60, util.vector().topOf(util.grid().at(1, 2, 2)), PonderPalette.RED);
        scene.world().removeItemsFromBelt(util.grid().at(1, 2, 2));

        for (int fill = 2; fill <= 4; fill++) {
            int level = fill;
            scene.world().modifyBlock(grinder, state -> state.setValue(SupremeGrinderBlock.FILL, level), false);
            scene.idle(10);
        }
        say(scene, "grinder.gauge", 60, util.vector().blockSurface(grinder, Direction.NORTH));
    }

    static void modes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "supreme_grinder_modes");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        BlockPos grinder = util.grid().at(2, 2, 2);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 1, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(2, 2, 2, 2, 4, 2), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().fromTo(2, 2, 2, 2, 4, 2), 64);
        scene.idle(15);

        scene.overlay().showCenteredScrollInput(grinder, Direction.NORTH, 60);
        say(scene, "grinder.modes", 60, util.vector().blockSurface(grinder, Direction.NORTH));
        scene.world().modifyBlock(grinder, state -> state.setValue(SupremeGrinderBlock.FILL, 2), false);
        say(scene, "grinder.the", 60, util.vector().blockSurface(grinder, Direction.NORTH), PonderPalette.GREEN);
        say(scene, "grinder.ultimate", 60, util.vector().blockSurface(grinder, Direction.NORTH), PonderPalette.BLUE);

        scene.world().modifyBlock(grinder, state -> state.setValue(SupremeGrinderBlock.FILL, 4), false);
        scene.idle(10);
        scene.effects().indicateSuccess(grinder);
        scene.world().modifyBlock(grinder, state -> state.setValue(SupremeGrinderBlock.FILL, 0), false);
        BlockPos hopper = util.grid().at(2, 1, 2);
        scene.overlay().showControls(util.vector().blockSurface(hopper, Direction.WEST), Pointing.LEFT, 60)
            .withItem(BSItems.EXCEPTIONAL_PASTE.asStack());
        say(scene, "grinder.paste", 60, util.vector().blockSurface(hopper, Direction.WEST));

        scene.overlay().showControls(util.vector().topOf(grinder), Pointing.DOWN, 60).withItem(AllItems.GOGGLES.asStack());
        say(scene, "grinder.goggles", 70, util.vector().blockSurface(grinder, Direction.NORTH));
    }

    private static void showBelt(CreateSceneBuilder scene, SceneBuildingUtil util) {
        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 2, 2).add(util.select().position(0, 2, 3)), Direction.EAST);
        // Negative: on a belt along x, that runs east, towards the grinder.
        scene.world().setKineticSpeed(util.select().fromTo(0, 2, 2, 1, 2, 3), -32);
        scene.idle(10);
    }

    // An item rides the belt into the grinder, which takes it.
    private static void feed(CreateSceneBuilder scene, SceneBuildingUtil util, ItemStack food) {
        scene.world().createItemOnBelt(util.grid().at(0, 2, 2), Direction.WEST, food);
        scene.idle(25);
        scene.world().removeItemsFromBelt(util.grid().at(1, 2, 2));
        scene.world().removeItemsFromBelt(util.grid().at(0, 2, 2));
    }
}
