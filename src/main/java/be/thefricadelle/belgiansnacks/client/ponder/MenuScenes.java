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

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.waffle.WaffleIronBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSWaffles;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * v2 scenes (M21): the fries line, the waffle iron, the used oil and a friterie that sells through
 * Create's shops. The Ponder world runs no server logic: machines are shown working by editing their
 * saved data, as in FryerScenes.
 */
final class MenuScenes {
    private MenuScenes() {
    }

    // Schematic ponder/fries_line.nbt: saw at x = 1, fryers at x = 3 (lit) and 5 (seething), depot and
    // deployer at x = 7, all at z = 2.
    static void friesLine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "fries_line");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(10);
        HolderLookup.Provider registries = scene.world().getHolderLookupProvider();

        BlockPos saw = util.grid().at(1, 1, 2);
        scene.world().showSection(util.select().position(saw), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(saw), 32);
        scene.idle(10);
        var potato = scene.world().createItemEntity(util.vector().topOf(saw).add(0, 0.5, 0), Vec3.ZERO, new ItemStack(Items.POTATO));
        scene.idle(20);
        scene.world().modifyEntity(potato, entity -> entity.discard());
        scene.world().createItemEntity(util.vector().topOf(saw).add(0, 0.3, 0), new Vec3(0.05, 0.1, 0), BSItems.RAW_FRIES.asStack(4));
        say(scene, "fries.saw", 50, util.vector().topOf(saw));

        BlockPos blanch = util.grid().at(3, 2, 2);
        scene.world().showSection(util.select().fromTo(3, 1, 2, 3, 2, 2), Direction.DOWN);
        scene.idle(10);
        fry(scene, util.select().position(blanch), registries, BSItems.RAW_FRIES.asStack(16), "frying/blanched_fries");
        say(scene, "fries.blanch", 60, util.vector().topOf(blanch));
        done(scene, util.select().position(blanch), registries, BSItems.BLANCHED_FRIES.asStack(16));

        BlockPos second = util.grid().at(5, 2, 2);
        scene.world().showSection(util.select().fromTo(5, 1, 2, 5, 2, 2), Direction.DOWN);
        scene.idle(10);
        fry(scene, util.select().position(second), registries, BSItems.BLANCHED_FRIES.asStack(16), "frying/fries");
        say(scene, "fries.second", 60, util.vector().blockSurface(util.grid().at(5, 1, 2), Direction.WEST), PonderPalette.BLUE);
        done(scene, util.select().position(second), registries, BSItems.FRIES.asStack(16));
        say(scene, "fries.tallow", 60, util.vector().topOf(second));

        BlockPos depot = util.grid().at(7, 1, 2);
        BlockPos deployer = util.grid().at(7, 3, 2);
        scene.world().showSection(util.select().position(depot), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(deployer), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(deployer), 32);
        scene.idle(10);
        scene.world().createItemOnBeltLike(depot, Direction.UP, BSItems.FRIES.asStack());
        scene.idle(10);
        scene.world().moveDeployer(deployer, 1, 20);
        scene.idle(20);
        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.UP, BSItems.CONE_OF_FRIES.asStack());
        scene.world().moveDeployer(deployer, -1, 20);
        scene.idle(15);
        say(scene, "fries.cone", 60, util.vector().topOf(depot), PonderPalette.GREEN);
        say(scene, "fries.sauce", 60, util.vector().topOf(depot));
    }

    // Schematic ponder/waffle_iron.nbt: burner (2, 1, 2), waffle iron above it, belt from the west, funnel
    // and chest east.
    static void waffleIron(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "waffle_iron");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        HolderLookup.Provider registries = scene.world().getHolderLookupProvider();

        BlockPos burner = util.grid().at(2, 1, 2);
        BlockPos iron = util.grid().at(2, 2, 2);
        Selection ironSelection = util.select().position(iron);
        scene.world().showSection(util.select().position(burner), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(ironSelection, Direction.DOWN);
        scene.idle(15);
        say(scene, "waffle.heat", 60, util.vector().blockSurface(burner, Direction.WEST));

        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 2, 2).add(util.select().position(0, 2, 3)), Direction.EAST);
        scene.world().setKineticSpeed(util.select().fromTo(0, 2, 2, 1, 2, 3), -32);
        scene.idle(10);
        scene.world().createItemOnBelt(util.grid().at(0, 2, 2), Direction.WEST, BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(4));
        say(scene, "waffle.input", 50, util.vector().topOf(util.grid().at(0, 2, 2)));
        scene.world().removeItemsFromBelt(util.grid().at(1, 2, 2));
        scene.world().removeItemsFromBelt(util.grid().at(0, 2, 2));

        scene.world().modifyBlockEntityNBT(ironSelection, WaffleIronBlockEntity.class, nbt -> {
            nbt.put("Plates", handler(registries, BSWaffles.BRUSSELS_WAFFLE_BATTER.asStack(4)));
            nbt.putString("Recipe", BelgianSnacks.asResource("waffle_baking/brussels_waffle").toString());
            nbt.putInt("Progress", 0);
            nbt.putInt("Duration", 160);
        });
        say(scene, "waffle.bake", 80, util.vector().topOf(iron));

        scene.world().modifyBlockEntityNBT(ironSelection, WaffleIronBlockEntity.class, nbt -> {
            nbt.put("Plates", handler(registries, ItemStack.EMPTY));
            nbt.put("Output", handler(registries, BSWaffles.BRUSSELS_WAFFLE.asStack(4), ItemStack.EMPTY));
            nbt.remove("Recipe");
            nbt.putInt("Duration", 0);
        });
        scene.effects().indicateSuccess(iron);
        BlockPos funnel = util.grid().at(3, 2, 2);
        scene.world().showSection(util.select().fromTo(3, 2, 2, 4, 2, 2), Direction.WEST);
        scene.idle(10);
        scene.world().flapFunnel(funnel, true);
        say(scene, "waffle.output", 60, util.vector().topOf(funnel));

        scene.overlay().showControls(util.vector().topOf(iron), Pointing.DOWN, 60).withItem(BSItems.POWDERED_SUGAR.asStack());
        say(scene, "waffle.toppings", 60, util.vector().topOf(iron), PonderPalette.GREEN);
    }

    // Schematic ponder/used_oil.nbt: fryer (1, 2, 2) on a burner, pump and pipe east to a basin (4, 2, 2)
    // under a mixer, its cogwheel beside it.
    static void usedOil(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "used_oil");
        scene.configureBasePlate(0, 0, 6);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos fryer = util.grid().at(1, 2, 2);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 1, 2, 2), Direction.DOWN);
        scene.idle(15);
        say(scene, "oil.used", 70, util.vector().topOf(fryer));
        say(scene, "oil.full", 60, util.vector().topOf(fryer), PonderPalette.RED);

        scene.world().showSection(util.select().fromTo(2, 2, 2, 3, 2, 2), Direction.WEST);
        scene.world().setKineticSpeed(util.select().position(2, 2, 2), 32);
        scene.idle(10);
        say(scene, "oil.pipe", 60, util.vector().topOf(util.grid().at(2, 2, 2)));

        BlockPos basin = util.grid().at(4, 2, 2);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 4, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(4, 4, 2, 5, 4, 2), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().fromTo(4, 4, 2, 5, 4, 2), 64);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 60).withItem(BSItems.OIL_FILTER.asStack());
        say(scene, "oil.filter", 70, util.vector().topOf(basin));
        say(scene, "oil.clean", 60, util.vector().topOf(basin), PonderPalette.GREEN);
        say(scene, "oil.biodiesel", 60, util.vector().topOf(basin));
    }

    // Schematic ponder/friterie_shop.nbt: back wall at z = 4 with its signs and neon, seat (2, 1, 3) beside
    // the Stock Ticker (3, 1, 3), a counter of table cloths at z = 1, facing the camera.
    static void friterieShop(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "friterie_shop");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(util.select().fromTo(0, 1, 4, 6, 4, 4), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 3, 3, 5, 3, 3), Direction.NORTH);
        scene.idle(15);
        say(scene, "shop.decor", 60, util.vector().blockSurface(util.grid().at(3, 3, 3), Direction.NORTH));

        scene.world().showSection(util.select().fromTo(2, 1, 3, 3, 1, 3), Direction.DOWN);
        scene.idle(10);
        scene.special().createBirb(util.vector().centerOf(util.grid().at(2, 1, 3)), net.createmod.ponder.api.element.ParrotPose.FacePointOfInterestPose::new);
        scene.idle(15);
        say(scene, "shop.keeper", 80, util.vector().topOf(util.grid().at(3, 1, 3)));

        scene.world().showSection(util.select().fromTo(1, 1, 1, 6, 2, 1), Direction.DOWN);
        scene.idle(15);
        say(scene, "shop.cloth", 80, util.vector().topOf(util.grid().at(3, 2, 1)));
        say(scene, "shop.buy", 70, util.vector().topOf(util.grid().at(1, 2, 1)), PonderPalette.GREEN);
    }

    private static void fry(CreateSceneBuilder scene, Selection fryer, HolderLookup.Provider registries, ItemStack batch, String recipe) {
        scene.world().modifyBlockEntityNBT(fryer, FryerBlockEntity.class, nbt -> {
            nbt.put("Basket", handler(registries, batch));
            nbt.putString("Recipe", BelgianSnacks.asResource(recipe).toString());
            nbt.putInt("Progress", 0);
            nbt.putInt("Duration", 100);
        });
    }

    private static void done(CreateSceneBuilder scene, Selection fryer, HolderLookup.Provider registries, ItemStack result) {
        scene.world().modifyBlockEntityNBT(fryer, FryerBlockEntity.class, nbt -> {
            nbt.put("Basket", handler(registries, ItemStack.EMPTY));
            nbt.put("Output", handler(registries, result, ItemStack.EMPTY));
            nbt.remove("Recipe");
            nbt.putInt("Duration", 0);
        });
    }

    private static CompoundTag handler(HolderLookup.Provider registries, ItemStack... stacks) {
        ItemStackHandler handler = new ItemStackHandler(stacks.length);
        for (int i = 0; i < stacks.length; i++) {
            handler.setStackInSlot(i, stacks[i]);
        }
        return handler.serializeNBT(registries);
    }
}
