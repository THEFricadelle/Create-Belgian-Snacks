/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.waffle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import be.thefricadelle.belgiansnacks.client.BSPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * The lid swings on its hinge at the back, shut while a batch bakes; the batter or the waffles lie flat on
 * the bottom plate. The lid model faces north (handle at z = 0, hinge at z = 16), like the base.
 */
@OnlyIn(Dist.CLIENT)
public class WaffleIronRenderer extends SmartBlockEntityRenderer<WaffleIronBlockEntity> {
    private static final float OPEN_DEGREES = 70;
    // The hinge line, in the north-facing model.
    private static final float HINGE_Y = 6 / 16f;
    private static final float HINGE_Z = 15 / 16f;
    // Top of the bottom plate.
    private static final float PLATE_Y = 6 / 16f;

    public WaffleIronRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(WaffleIronBlockEntity iron, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(iron, partialTicks, ms, buffer, light, overlay);
        BlockState state = iron.getBlockState();
        Direction facing = state.getValue(WaffleIronBlock.FACING);

        ms.pushPose();
        // The same turn the blockstate gives the base: north is the model, the others turn clockwise.
        ms.translate(0.5, 0, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(-((facing.toYRot() + 180) % 360)));
        ms.translate(-0.5, 0, -0.5);

        ItemStack shown = iron.getPlates().isEmpty() ? firstOutput(iron) : iron.getPlates();
        if (shown.isEmpty()) {
            shown = iron.getInput().getStackInSlot(0);
        }
        if (!shown.isEmpty()) {
            ms.pushPose();
            ms.translate(0.5, PLATE_Y + 0.01, 0.5);
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.scale(0.6f, 0.6f, 0.6f);
            Minecraft.getInstance().getItemRenderer()
                .renderStatic(shown, ItemDisplayContext.FIXED, light, overlay, ms, buffer, iron.getLevel(), 0);
            ms.popPose();
        }

        // Lifting the handle side: a positive turn about x around the hinge raises z < hinge.
        ms.translate(0, HINGE_Y, HINGE_Z);
        ms.mulPose(Axis.XP.rotationDegrees(OPEN_DEGREES * iron.lidOpen(partialTicks)));
        ms.translate(0, -HINGE_Y, -HINGE_Z);
        CachedBuffers.partial(BSPartialModels.WAFFLE_IRON_LID, state)
            .light(light)
            .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();
    }

    private static ItemStack firstOutput(WaffleIronBlockEntity iron) {
        for (int slot = 0; slot < iron.getOutput().getSlots(); slot++) {
            if (!iron.getOutput().getStackInSlot(slot).isEmpty()) {
                return iron.getOutput().getStackInSlot(slot);
            }
        }
        return ItemStack.EMPTY;
    }
}
