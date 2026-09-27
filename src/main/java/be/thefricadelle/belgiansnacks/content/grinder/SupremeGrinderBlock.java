/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.network.GrinderMissingRequestPayload;
import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

public class SupremeGrinderBlock extends KineticBlock implements IBE<SupremeGrinderBlockEntity> {
    /** The gauge on the sides: progress towards the current mode's goal, in quarters. */
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 4);

    public SupremeGrinderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FILL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FILL));
    }

    // The shaft comes in from above.
    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        SupremeGrinderBlockEntity grinder = getBlockEntity(level, pos);
        // Both sides hold the food index: anything else (a block to place, a wrench) goes through untouched.
        if (grinder == null || stack.isEmpty() || !FoodIndex.get(level).contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        // Only the server knows the collection: it decides new or duplicate, and the hand follows.
        if (!level.isClientSide) {
            ItemStack left = grinder.insert(stack.copyWithCount(1), false);
            if (left.isEmpty() && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    // Empty hand: sneaking with goggles opens the list of missing foods, otherwise takes the paste.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        SupremeGrinderBlockEntity grinder = getBlockEntity(level, pos);
        if (grinder == null || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && GogglesItem.isWearingGoggles(player)) {
            if (level.isClientSide) {
                PacketDistributor.sendToServer(new GrinderMissingRequestPayload(pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            ItemStack out = grinder.getOutput().extractItem(0, 64, false);
            if (!out.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, out);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // The gauge as a comparator signal: 0 empty, 15 at the goal.
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        SupremeGrinderBlockEntity grinder = getBlockEntity(level, pos);
        return grinder == null ? 0 : grinder.comparatorSignal();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public Class<SupremeGrinderBlockEntity> getBlockEntityClass() {
        return SupremeGrinderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SupremeGrinderBlockEntity> getBlockEntityType() {
        return BSBlockEntities.SUPREME_GRINDER.get();
    }
}
