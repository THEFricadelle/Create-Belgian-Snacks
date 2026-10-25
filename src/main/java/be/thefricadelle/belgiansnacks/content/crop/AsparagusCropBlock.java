/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.crop;

import java.util.function.Supplier;

import be.thefricadelle.belgiansnacks.config.BSConfig;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Asparagus grows in any light; the light of its last growth step decides the harvest: white
 * asparagus from the dark (the "white gold" of Flemish cooking), green from daylight.
 */
public class AsparagusCropBlock extends ShortCropBlock {
    public static final BooleanProperty BLANCHED = BooleanProperty.create("blanched");

    public AsparagusCropBlock(Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties, seed);
        registerDefaultState(defaultBlockState().setValue(BLANCHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BLANCHED);
    }

    @Override
    protected BlockState grown(BlockState state, int light) {
        return super.grown(state, light).setValue(BLANCHED, light <= BSConfig.cropsDarkMaxLight());
    }
}
