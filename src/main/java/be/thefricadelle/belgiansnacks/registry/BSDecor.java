/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import static be.thefricadelle.belgiansnacks.BelgianSnacks.REGISTRATE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.decor.WallPanelBlock;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;

/**
 * v2 decoration (docs/13, section 7.2): friterie signs in three languages (V-D13) and neons in four
 * colours. Boards hung on a wall, no block entity, nothing stored.
 */
public final class BSDecor {
    /** French name of every block of this class, read by the French language provider. */
    public static final Map<BlockEntry<WallPanelBlock>, String> FRENCH = new LinkedHashMap<>();

    public static final BlockEntry<WallPanelBlock>
        FRITERIE_SIGN = sign("friterie_sign", "Friterie Sign", "Enseigne Friterie"),
        FRITUUR_SIGN = sign("frituur_sign", "Frituur Sign", "Enseigne Frituur"),
        CHIP_SHOP_SIGN = sign("chip_shop_sign", "Chip Shop Sign", "Enseigne Chip shop");

    public static final BlockEntry<WallPanelBlock>
        RED_NEON = neon(DyeColor.RED, "Red", "rouge"),
        YELLOW_NEON = neon(DyeColor.YELLOW, "Yellow", "jaune"),
        BLUE_NEON = neon(DyeColor.BLUE, "Blue", "bleu"),
        GREEN_NEON = neon(DyeColor.GREEN, "Green", "vert");

    public static final List<BlockEntry<WallPanelBlock>> SIGNS = List.of(FRITERIE_SIGN, FRITUUR_SIGN, CHIP_SHOP_SIGN);
    public static final List<BlockEntry<WallPanelBlock>> NEONS = List.of(RED_NEON, YELLOW_NEON, BLUE_NEON, GREEN_NEON);

    private BSDecor() {
    }

    // A black board with white letters: planks, black and white dye.
    private static BlockEntry<WallPanelBlock> sign(String name, String english, String french) {
        BlockEntry<WallPanelBlock> entry = REGISTRATE.block(name, WallPanelBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK).strength(1.0f).sound(SoundType.WOOD).noOcclusion())
            .transform(TagGen.axeOnly())
            .lang(english)
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.get(), panel(prov, name, prov.mcLoc("block/dark_oak_planks"))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ctx.get())
                .pattern("PPP")
                .pattern("BPW")
                .pattern("PP ")
                .define('P', ItemTags.PLANKS)
                .define('B', Tags.Items.DYES_BLACK)
                .define('W', Tags.Items.DYES_WHITE)
                .unlockedBy("has_planks", prov.has(ItemTags.PLANKS))
                .save(prov, BelgianSnacks.asResource("crafting/" + name)))
            .item().model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/decor/" + name))).build()
            .register();
        FRENCH.put(entry, french);
        return entry;
    }

    // A cone of fries in neon tubes on a dark panel: full light, drawn lit even in the dark.
    private static BlockEntry<WallPanelBlock> neon(DyeColor colour, String english, String french) {
        String name = colour.getSerializedName() + "_friterie_neon";
        BlockEntry<WallPanelBlock> entry = REGISTRATE.block(name, WallPanelBlock::new)
            .properties(p -> p.mapColor(colour.getMapColor()).strength(0.5f).sound(SoundType.GLASS).noOcclusion()
                .lightLevel(state -> 15).emissiveRendering((state, level, pos) -> true))
            .transform(TagGen.pickaxeOnly())
            .lang(english + " Friterie Neon")
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.get(), panel(prov, name, prov.mcLoc("block/black_concrete"))))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ctx.get())
                .pattern("GDG")
                .pattern("GSG")
                .define('G', Tags.Items.GLASS_PANES)
                .define('D', colour.getTag())
                .define('S', Items.GLOWSTONE_DUST)
                .unlockedBy("has_glowstone_dust", prov.has(Items.GLOWSTONE_DUST))
                .save(prov, BelgianSnacks.asResource("crafting/" + name)))
            .item().model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/decor/" + name))).build()
            .register();
        FRENCH.put(entry, "Néon de friterie " + french);
        return entry;
    }

    // A 16 x 8 x 1 board against the south side of the block, its face looking north (the blockstate turns it).
    private static ModelFile panel(RegistrateBlockstateProvider prov, String name, net.minecraft.resources.ResourceLocation edge) {
        BlockModelBuilder model = prov.models().withExistingParent("block/decor/" + name, "block/block")
            .texture("face", prov.modLoc("block/decor/" + name))
            .texture("edge", edge)
            .texture("particle", edge);
        model.element().from(0, 4, 15).to(16, 12, 16).allFaces((direction, face) -> {
            if (direction == net.minecraft.core.Direction.NORTH) {
                face.texture("#face").uvs(0, 0, 16, 16);
            } else {
                face.texture("#edge");
            }
        }).end();
        return model;
    }

    public static void register() {
        // Loads the class so every entry above is created during mod construction.
    }
}
