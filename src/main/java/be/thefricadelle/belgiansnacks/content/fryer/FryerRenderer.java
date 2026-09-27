/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.fryer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

/** Draws the fat inside the vat and the items floating in it (the basket, or the waiting input). */
@OnlyIn(Dist.CLIENT)
public class FryerRenderer extends SmartBlockEntityRenderer<FryerBlockEntity> {
    public FryerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FryerBlockEntity fryer, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(fryer, partialTicks, ms, buffer, light, overlay);
        float surface = fryer.fatSurface();
        FluidStack fat = fryer.getTank().getPrimaryHandler().getFluid();
        if (!fat.isEmpty()) {
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fat, 2 / 16f, 4 / 16f, 2 / 16f, 14 / 16f, surface, 14 / 16f,
                buffer, ms, light, false, false);
        }

        ItemStack shown = fryer.getBasket().isEmpty() ? fryer.getInput().getStackInSlot(0) : fryer.getBasket();
        if (shown.isEmpty()) {
            return;
        }
        // A few copies bobbing at the surface: enough to read the batch without drawing all 16.
        int copies = Math.min(4, 1 + shown.getCount() / 4);
        float time = (fryer.getLevel().getGameTime() + partialTicks) / 20f;
        for (int i = 0; i < copies; i++) {
            ms.pushPose();
            float angle = i * 90f + time * 20f;
            double bob = Math.sin(time * 2 + i) * 0.015;
            // Just above the surface: drawn under it, the fat would hide them.
            ms.translate(0.5, surface + 0.03 + bob, 0.5);
            ms.mulPose(Axis.YP.rotationDegrees(angle));
            ms.translate(0.2, 0, 0);
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.scale(0.5f, 0.5f, 0.5f);
            Minecraft.getInstance().getItemRenderer()
                .renderStatic(shown, ItemDisplayContext.FIXED, light, overlay, ms, buffer, fryer.getLevel(), 0);
            ms.popPose();
        }
    }
}
