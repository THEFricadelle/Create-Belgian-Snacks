/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
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
    // Between two floating items, across the vat.
    private static final double SPACING = 0.15;

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
        // A few copies lying side by side at the surface, enough to read the batch without drawing
        // all 16; while frying they tremble in the bubbling fat.
        int copies = Math.min(4, 1 + shown.getCount() / 4);
        boolean frying = fryer.status() == FryerBlockEntity.Status.FRYING;
        float time = fryer.getLevel().getGameTime() + partialTicks;
        for (int i = 0; i < copies; i++) {
            ms.pushPose();
            double jitterX = frying ? Math.sin(time * 2.1 + i * 1.7) * 0.006 : 0;
            double jitterY = frying ? Math.sin(time * 3.3 + i * 2.3) * 0.004 : 0;
            double jitterZ = frying ? Math.cos(time * 2.7 + i * 1.1) * 0.006 : 0;
            float wobble = frying ? (float) Math.sin(time * 2.9 + i) * 3f : 0;
            // Spread across the vat, centred on it.
            double offset = (i - (copies - 1) / 2.0) * SPACING;
            // Just above the surface: drawn under it, the fat would hide them.
            ms.translate(0.5 + jitterX, surface + 0.03 + jitterY, 0.5 + offset + jitterZ);
            // The sprite's sausage is diagonal: a -45 degree turn lays it along x, the copies side by side along z.
            ms.mulPose(Axis.YP.rotationDegrees(-45 + wobble));
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.scale(0.5f, 0.5f, 0.5f);
            Minecraft.getInstance().getItemRenderer()
                .renderStatic(shown, ItemDisplayContext.FIXED, light, overlay, ms, buffer, fryer.getLevel(), 0);
            ms.popPose();
        }
    }
}
