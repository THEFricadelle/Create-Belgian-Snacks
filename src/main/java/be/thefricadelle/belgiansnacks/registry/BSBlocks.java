/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;

import be.thefricadelle.belgiansnacks.content.fryer.FryerBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public final class BSBlocks {
    public static final BlockEntry<FryerBlock> FRYER = REGISTRATE.block("fryer", FryerBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .properties(p -> p.mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).noOcclusion())
        .transform(TagGen.pickaxeOnly())
        .lang("Fryer")
        .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), vatModel(prov)))
        // The fat stays in the dropped item; the items inside drop on their own.
        .loot((tables, block) -> tables.add(block, tables.createSingleItemTable(block)
            .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(BSDataComponents.FRYER_FLUID.get()))))
        .item()
        .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/fryer")))
        .build()
        .register();

    private BSBlocks() {
    }

    // Placeholder vat until a Blockbench model exists: a floor and four walls, open at the top.
    private static ModelFile vatModel(RegistrateBlockstateProvider prov) {
        BlockModelBuilder model = prov.models().withExistingParent("block/fryer", "block/block")
            .texture("side", prov.modLoc("block/fryer/side"))
            .texture("top", prov.modLoc("block/fryer/top"))
            .texture("bottom", prov.modLoc("block/fryer/bottom"))
            .texture("inner", prov.modLoc("block/fryer/inner"))
            .texture("particle", prov.modLoc("block/fryer/side"));
        box(model, 0, 0, 0, 16, 2, 16);
        box(model, 0, 2, 0, 16, 16, 2);
        box(model, 0, 2, 14, 16, 16, 16);
        box(model, 0, 2, 2, 2, 16, 14);
        box(model, 14, 2, 2, 16, 16, 14);
        return model;
    }

    private static void box(BlockModelBuilder model, float x1, float y1, float z1, float x2, float y2, float z2) {
        model.element().from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
            String texture = switch (direction) {
                case DOWN -> "#bottom";
                case UP -> y1 == 0 ? "#inner" : "#top";
                default -> isInward(direction, x1, z1, x2, z2) ? "#inner" : "#side";
            };
            face.texture(texture).cullface(isOuter(direction, x1, y1, z1, x2, z2) ? direction : null);
        }).end();
    }

    // A face looking into the vat rather than out of the block.
    private static boolean isInward(Direction direction, float x1, float z1, float x2, float z2) {
        return switch (direction) {
            case NORTH -> z1 > 0;
            case SOUTH -> z2 < 16;
            case WEST -> x1 > 0;
            case EAST -> x2 < 16;
            default -> false;
        };
    }

    private static boolean isOuter(Direction direction, float x1, float y1, float z1, float x2, float z2) {
        return switch (direction) {
            case DOWN -> y1 == 0;
            case NORTH -> z1 == 0;
            case SOUTH -> z2 == 16;
            case WEST -> x1 == 0;
            case EAST -> x2 == 16;
            default -> false;
        };
    }

    public static void register() {
        // Loads the class so every entry above is created during mod construction.
    }
}
