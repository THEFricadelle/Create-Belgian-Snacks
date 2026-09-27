/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client.ponder;

import static be.thefricadelle.belgiansnacks.client.ponder.PonderTexts.say;

import java.util.List;

import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;

import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Tiers 2 and 3 on a belt (schematics ponder/the_fricadelle_line.nbt and ultimate_fricadelle_line.nbt):
 * the belt runs along x at z = 3, the stations stand two blocks above it from x = 1. The item is
 * shown under each station in turn while that machine works, as Create's own scenes do.
 */
final class AssemblyScenes {
    private enum Station { SPOUT, DEPLOYER, PRESS }

    private static final int BLOCK_TICKS = 15;
    private static final int PRESS_STROKE_TICKS = 62;

    private record Step(Station station, String line) {
    }

    private AssemblyScenes() {
    }

    static void theFricadelle(SceneBuilder builder, SceneBuildingUtil util) {
        line(builder, util, "the_fricadelle_line", 7, "line2.intro", BSItems.EXCEPTIONAL_PASTE.get(),
            BSItems.INCOMPLETE_THE_FRICADELLE.get(), BSItems.RAW_THE_FRICADELLE.get(),
            List.of(new Step(Station.DEPLOYER, "line2.spices"), new Step(Station.SPOUT, "line2.mayonnaise"),
                new Step(Station.SPOUT, "line2.curry"), new Step(Station.DEPLOYER, "line2.onion"), new Step(Station.PRESS, "line2.press")),
            "line2.loops");
    }

    static void ultimateFricadelle(SceneBuilder builder, SceneBuildingUtil util) {
        line(builder, util, "ultimate_fricadelle_line", 8, "line3.intro", BSItems.ABSOLUTE_PASTE.get(),
            BSItems.INCOMPLETE_ULTIMATE_FRICADELLE.get(), BSItems.RAW_ULTIMATE_FRICADELLE.get(),
            List.of(new Step(Station.SPOUT, "line3.tallow"), new Step(Station.DEPLOYER, "line3.spices"),
                new Step(Station.SPOUT, "line3.mayonnaise"), new Step(Station.SPOUT, "line3.curry"),
                new Step(Station.DEPLOYER, "line3.onion"), new Step(Station.PRESS, "line3.press")),
            "line3.loops");
    }

    private static void line(SceneBuilder builder, SceneBuildingUtil util, String id, int plate, String intro, Item paste, Item unfinished,
                             Item raw, List<Step> steps, String loops) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, id);
        scene.configureBasePlate(0, 0, plate);
        scene.showBasePlate();
        scene.idle(10);
        int last = steps.size() + 1;
        scene.world().showSection(util.select().fromTo(0, 1, 3, last, 1, 3).add(util.select().position(0, 1, 4)), Direction.DOWN);
        scene.idle(10);
        for (int x = 1; x <= steps.size(); x++) {
            scene.world().showSection(util.select().position(x, 3, 3), Direction.DOWN);
            scene.idle(4);
        }
        scene.world().setKineticSpeed(util.select().everywhere(), 32);
        // Negative on the belt and its shaft: along x, that runs east, down the line.
        scene.world().setKineticSpeed(util.select().fromTo(0, 1, 3, last, 1, 3).add(util.select().position(0, 1, 4)), -32);
        scene.idle(10);

        // One item rides the whole line: it moves one block, stops under the next station while the
        // machine works, then moves on, as Create's own belt scenes do.
        BlockPos start = util.grid().at(0, 1, 3);
        // Dropped from above, the item starts at the centre of its segment: each block it moves then
        // stops it right under the next station.
        ElementLink<BeltItemElement> item = scene.world().createItemOnBelt(start, Direction.UP, new ItemStack(paste));
        scene.world().stallBeltItem(item, true);
        say(scene, intro, 50, util.vector().topOf(start));

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            advance(scene, item);
            BlockPos machine = util.grid().at(i + 1, 3, 3);
            work(scene, util, step.station(), machine);
            scene.world().changeBeltItemTo(item, new ItemStack(unfinished));
            say(scene, step.line(), 50, util.vector().blockSurface(machine, Direction.NORTH));
        }

        BlockPos end = util.grid().at(last, 1, 3);
        advance(scene, item);
        scene.world().changeBeltItemTo(item, new ItemStack(raw));
        scene.effects().indicateSuccess(end);
        say(scene, loops, 70, util.vector().topOf(end), PonderPalette.GREEN);
    }

    // One block down the belt at 32 RPM (a belt moves speed / 480 blocks per tick), then stop.
    private static void advance(CreateSceneBuilder scene, ElementLink<BeltItemElement> item) {
        scene.world().stallBeltItem(item, false);
        scene.idle(BLOCK_TICKS);
        scene.world().stallBeltItem(item, true);
    }

    private static void work(CreateSceneBuilder scene, SceneBuildingUtil util, Station station, BlockPos machine) {
        switch (station) {
            case DEPLOYER -> {
                scene.world().moveDeployer(machine, 1, 20);
                scene.idle(20);
                scene.world().moveDeployer(machine, -1, 20);
                scene.idle(15);
            }
            case SPOUT -> {
                scene.world().modifyBlockEntityNBT(util.select().position(machine), SpoutBlockEntity.class,
                    nbt -> nbt.putInt("ProcessingTicks", 20));
                scene.idle(25);
            }
            case PRESS -> {
                scene.world().modifyBlockEntity(machine, MechanicalPressBlockEntity.class, press -> press.getPressingBehaviour().start(Mode.BELT));
                // The whole stroke, down and back up: a cycle at 32 RPM (Create's PressingBehaviour.CYCLE of 240
                // at 4 per tick).
                scene.idle(PRESS_STROKE_TICKS);
                // Only a server ends a pressing cycle; in the Ponder world the press would stay running and
                // hold the item under it for good.
                scene.world().modifyBlockEntity(machine, MechanicalPressBlockEntity.class, press -> {
                    press.getPressingBehaviour().running = false;
                    press.getPressingBehaviour().runningTicks = 0;
                });
            }
        }
    }
}
