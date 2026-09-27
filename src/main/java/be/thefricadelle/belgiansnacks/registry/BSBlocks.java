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

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;

import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlock;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlock;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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

    // D11: 8 SU/rpm (a crushing wheel's), read from the config so a server can change it.
    public static final BlockEntry<SupremeGrinderBlock> SUPREME_GRINDER = REGISTRATE.block("supreme_grinder", SupremeGrinderBlock::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.NETHERITE_BLOCK).noOcclusion())
        .transform(TagGen.pickaxeOnly())
        .lang("Supreme Grinder")
        .onRegister(block -> BlockStressValues.IMPACTS.register(block, BSConfig::grinderStressImpact))
        .blockstate((ctx, prov) -> {
            grinderBlades(prov);
            prov.getVariantBuilder(ctx.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(grinderModel(prov, state.getValue(SupremeGrinderBlock.FILL))).build());
        })
        // The collection stays in the dropped item (D10); the paste inside drops on its own.
        .loot((tables, block) -> tables.add(block, tables.createSingleItemTable(block)
            .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(BSDataComponents.GRINDER_CONTENTS.get()))))
        .item()
        .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/supreme_grinder/fill_0")))
        .build()
        .register();

    /**
     * Hand-made models (Blockbench exports, docs/06) live in src/main/resources under this folder.
     * When one exists, the generated model of the same block becomes its child, so blockstates,
     * item models and the grinder's gauge keep working unchanged.
     */
    public static final String HANDMADE = "block/custom/";

    private BSBlocks() {
    }

    private static boolean handMade(RegistrateBlockstateProvider prov, String name) {
        return prov.models().existingFileHelper.exists(prov.modLoc(HANDMADE + name), PackType.CLIENT_RESOURCES, ".json", "models");
    }

    // Placeholder housing until a Blockbench model exists: a brass body with the gauge on its sides,
    // and a rim around the blade pit on top. The blades are a separate, rotating partial model.
    private static ModelFile grinderModel(RegistrateBlockstateProvider prov, int fill) {
        String dir = "block/supreme_grinder/";
        // A hand-made housing takes over; each gauge level only swaps its #side texture.
        if (handMade(prov, "supreme_grinder")) {
            return prov.models().withExistingParent(dir + "fill_" + fill, prov.modLoc(HANDMADE + "supreme_grinder"))
                .texture("side", prov.modLoc(dir + "side_" + fill));
        }
        BlockModelBuilder model = prov.models().withExistingParent(dir + "fill_" + fill, "block/block")
            .texture("side", prov.modLoc(dir + "side_" + fill))
            .texture("top", prov.modLoc(dir + "top"))
            .texture("bottom", prov.modLoc(dir + "bottom"))
            .texture("particle", prov.modLoc(dir + "side_0"));
        model.element().from(0, 0, 0).to(16, 12, 16).allFaces((direction, face) -> face
            .texture(direction == Direction.DOWN ? "#bottom" : direction == Direction.UP ? "#top" : "#side")
            .cullface(direction == Direction.UP ? null : direction)).end();
        rim(model, 0, 0, 16, 2);
        rim(model, 0, 14, 16, 16);
        rim(model, 0, 2, 2, 14);
        rim(model, 14, 2, 16, 14);
        return model;
    }

    private static void rim(BlockModelBuilder model, float x1, float z1, float x2, float z2) {
        model.element().from(x1, 12, z1).to(x2, 16, z2).allFaces((direction, face) -> face
            .texture(direction.getAxis().isHorizontal() ? "#bottom" : "#top")
            .cullface(isOuterRim(direction, x1, z1, x2, z2) ? direction : null)).end();
    }

    private static boolean isOuterRim(Direction direction, float x1, float z1, float x2, float z2) {
        return switch (direction) {
            case UP -> true;
            case NORTH -> z1 == 0;
            case SOUTH -> z2 == 16;
            case WEST -> x1 == 0;
            case EAST -> x2 == 16;
            default -> false;
        };
    }

    // Two crossed blades and the shaft stub that meets the shaft above.
    private static void grinderBlades(RegistrateBlockstateProvider prov) {
        // Same path either way: the partial model and its Flywheel visual keep pointing at it.
        if (handMade(prov, "supreme_grinder_blades")) {
            prov.models().withExistingParent("block/supreme_grinder/blades", prov.modLoc(HANDMADE + "supreme_grinder_blades"));
            return;
        }
        BlockModelBuilder model = prov.models().withExistingParent("block/supreme_grinder/blades", "block/block")
            .texture("blade", prov.modLoc("block/supreme_grinder/blade"))
            .texture("particle", prov.modLoc("block/supreme_grinder/blade"));
        model.element().from(2, 12.5f, 7).to(14, 14, 9).allFaces((direction, face) -> face.texture("#blade")).end();
        model.element().from(7, 12.5f, 2).to(9, 14, 14).allFaces((direction, face) -> face.texture("#blade")).end();
        model.element().from(6, 12, 6).to(10, 16, 10).allFaces((direction, face) -> face.texture("#blade")).end();
    }

    // Placeholder vat until a Blockbench model exists: a floor and four walls, open at the top.
    private static ModelFile vatModel(RegistrateBlockstateProvider prov) {
        if (handMade(prov, "fryer")) {
            return prov.models().withExistingParent("block/fryer", prov.modLoc(HANDMADE + "fryer"));
        }
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
