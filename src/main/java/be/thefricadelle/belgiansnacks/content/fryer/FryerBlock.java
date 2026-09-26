/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.fryer;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class FryerBlock extends Block implements IBE<FryerBlockEntity>, IWrenchable {
    // An open vat: 2 px floor and walls.
    private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Block.box(2, 2, 2, 14, 16, 14), BooleanOp.ONLY_FIRST);

    public FryerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        FryerBlockEntity fryer = getBlockEntity(level, pos);
        if (fryer == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        // Buckets and other fluid containers fill or empty the fat tank.
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (fryer.getInput().isItemValid(0, stack)) {
            if (!level.isClientSide) {
                ItemStack left = fryer.getInput().insertItem(0, stack.copy(), false);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, left);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // An empty hand takes the fried output first, then the unfried input back.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        FryerBlockEntity fryer = getBlockEntity(level, pos);
        // Vanilla falls through here for any held item the fryer ignored; only an empty hand takes.
        if (fryer == null || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            boolean taken = false;
            for (int slot = 0; slot < fryer.getOutput().getSlots() && !taken; slot++) {
                ItemStack out = fryer.getOutput().extractItem(slot, 64, false);
                if (!out.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, out);
                    taken = true;
                }
            }
            if (!taken) {
                ItemStack in = fryer.getInput().extractItem(0, 64, false);
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
        FryerBlockEntity fryer = getBlockEntity(level, pos);
        return fryer == null ? 0 : fryer.comparatorSignal();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public Class<FryerBlockEntity> getBlockEntityClass() {
        return FryerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FryerBlockEntity> getBlockEntityType() {
        return BSBlockEntities.FRYER.get();
    }
}
