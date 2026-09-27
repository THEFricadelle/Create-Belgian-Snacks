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

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * The Fryer (schematic ponder/fryer.nbt): burner below, belt from the west, funnel and chest east.
 * The Ponder world runs no server logic, so frying is shown by editing the fryer's saved data.
 */
final class FryerScenes {
    private FryerScenes() {
    }

    static void fryer(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        PonderTexts.title(scene, "fryer");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos burner = util.grid().at(2, 1, 2);
        BlockPos fryer = util.grid().at(2, 2, 2);
        Selection fryerSelection = util.select().position(fryer);
        scene.world().showSection(util.select().position(burner), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(fryerSelection, Direction.DOWN);
        scene.idle(15);
        say(scene, "fryer.heat", 60, util.vector().blockSurface(burner, Direction.WEST));

        scene.world().modifyBlock(burner, state -> state.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.SEETHING), false);
        say(scene, "fryer.superheat", 60, util.vector().blockSurface(burner, Direction.WEST), PonderPalette.BLUE);
        scene.world().modifyBlock(burner, state -> state.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);

        scene.overlay().showControls(util.vector().topOf(fryer), Pointing.DOWN, 60)
            .withItem(new ItemStack(BSFluids.FRYING_OIL.getBucket().orElseThrow()));
        say(scene, "fryer.fat", 60, util.vector().topOf(fryer));

        Selection belt = util.select().fromTo(0, 1, 2, 1, 2, 2).add(util.select().position(0, 2, 3));
        scene.world().showSection(belt, Direction.EAST);
        scene.world().setKineticSpeed(util.select().fromTo(0, 2, 2, 1, 2, 3), 32);
        scene.idle(10);
        scene.world().createItemOnBelt(util.grid().at(0, 2, 2), Direction.WEST, BSItems.RAW_FRICADELLE.asStack(16));
        say(scene, "fryer.input", 50, util.vector().topOf(util.grid().at(0, 2, 2)));

        HolderLookup.Provider registries = scene.world().getHolderLookupProvider();
        scene.world().removeItemsFromBelt(util.grid().at(1, 2, 2));
        scene.world().removeItemsFromBelt(util.grid().at(0, 2, 2));
        scene.world().modifyBlockEntityNBT(fryerSelection, FryerBlockEntity.class, nbt -> {
            nbt.put("Basket", handler(registries, BSItems.RAW_FRICADELLE.asStack(16)));
            nbt.putString("Recipe", BelgianSnacks.asResource("frying/fricadelle").toString());
            nbt.putInt("Progress", 0);
            nbt.putInt("Duration", 100);
        });
        say(scene, "fryer.batch", 70, util.vector().topOf(fryer));

        scene.world().modifyBlockEntityNBT(fryerSelection, FryerBlockEntity.class, nbt -> {
            nbt.put("Basket", handler(registries, ItemStack.EMPTY));
            nbt.put("Output", handler(registries, BSItems.FRICADELLE.asStack(16), ItemStack.EMPTY));
            nbt.remove("Recipe");
            nbt.putInt("Duration", 0);
        });
        scene.effects().indicateSuccess(fryer);
        BlockPos funnel = util.grid().at(3, 2, 2);
        scene.world().showSection(util.select().fromTo(3, 2, 2, 4, 2, 2), Direction.WEST);
        scene.idle(10);
        scene.world().flapFunnel(funnel, true);
        say(scene, "fryer.output", 60, util.vector().topOf(funnel));

        scene.overlay().showControls(util.vector().topOf(fryer), Pointing.DOWN, 50).withItem(new ItemStack(BSBlocks.FRYER.get()));
        say(scene, "fryer.keep", 50, util.vector().topOf(fryer), PonderPalette.GREEN);
    }

    private static net.minecraft.nbt.CompoundTag handler(HolderLookup.Provider registries, ItemStack... stacks) {
        ItemStackHandler handler = new ItemStackHandler(stacks.length);
        for (int i = 0; i < stacks.length; i++) {
            handler.setStackInSlot(i, stacks[i]);
        }
        return handler.serializeNBT(registries);
    }
}
