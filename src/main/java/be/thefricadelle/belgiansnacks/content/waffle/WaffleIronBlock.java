/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.waffle;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Two hinged iron plates on a low base. FACING is the side of the handle, turned towards the player who
 * placed it; the hinge is on the opposite side.
 */
public class WaffleIronBlock extends Block implements IBE<WaffleIronBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    // The plates shut; the open lid is drawn, not collided with.
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 9, 15);

    public WaffleIronBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        WaffleIronBlockEntity iron = getBlockEntity(level, pos);
        if (iron == null || !iron.getInput().isItemValid(0, stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            ItemStack left = iron.getInput().insertItem(0, stack.copy(), false);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, left);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    // An empty hand takes the waffles first, then the unbaked batter back.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        WaffleIronBlockEntity iron = getBlockEntity(level, pos);
        if (iron == null || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            boolean taken = false;
            for (int slot = 0; slot < iron.getOutput().getSlots() && !taken; slot++) {
                ItemStack out = iron.getOutput().extractItem(slot, 64, false);
                if (!out.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, out);
                    taken = true;
                }
            }
            if (!taken) {
                ItemStack in = iron.getInput().extractItem(0, 64, false);
                if (!in.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, in);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        WaffleIronBlockEntity iron = getBlockEntity(level, pos);
        return iron == null ? 0 : iron.comparatorSignal();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public Class<WaffleIronBlockEntity> getBlockEntityClass() {
        return WaffleIronBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WaffleIronBlockEntity> getBlockEntityType() {
        return BSBlockEntities.WAFFLE_IRON.get();
    }
}
