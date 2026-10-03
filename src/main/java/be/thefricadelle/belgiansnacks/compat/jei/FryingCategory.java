/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.jei;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.fryer.FryingRecipe;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Create-styled page: item and fat per item on the left, the fryer on its burner, results on the right. */
public class FryingCategory extends CreateRecipeCategory<FryingRecipe> {
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public FryingCategory(Info<FryingRecipe> info) {
        super(info);
    }

    @Override
    protected void setRecipe(IRecipeLayoutBuilder builder, FryingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 14)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(recipe.getIngredients().get(0));
        SizedFluidIngredient fat = recipe.getFat();
        if (fat != null) {
            addFluidSlot(builder, 21, 37, fat)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                    Component.translatable(BelgianSnacks.MOD_ID + ".recipe.frying.fat").withStyle(ChatFormatting.GRAY)));
        }
        List<ProcessingOutput> results = recipe.getRollableResults();
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 141 + 19 * i, 26)
                .setBackground(getRenderedSlot(output), -1, -1)
                .addItemStack(output.getStack())
                .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

    @Override
    protected void draw(FryingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        // The arrow runs above the fryer; the burner fits in the lower half of the page.
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 47, 31);
        int centre = getBackground().getWidth() / 2 + 3;
        HeatCondition heat = recipe.getRequiredHeat();
        if (heat != HeatCondition.NONE) {
            heater.withHeat(heat.visualizeAsBlazeBurner()).draw(graphics, centre, 56);
        }
        drawFryer(graphics, centre, 56);

        int seconds = Math.max(1, recipe.getProcessingDuration() / 20);
        graphics.drawString(Minecraft.getInstance().font, seconds + " s", 143, 47, 0x8B8B8B, false);
    }

    // Same projection as Create's AnimatedBlazeBurner, one block higher so the vat sits on the burner.
    private static void drawFryer(GuiGraphics graphics, int x, int y) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 200);
        pose.mulPose(Axis.XP.rotationDegrees(-15.5f));
        pose.mulPose(Axis.YP.rotationDegrees(22.5f));
        AnimatedKinetics.defaultBlockElement(BSBlocks.FRYER.getDefaultState())
            .atLocal(0, 0.65, 0)
            .scale(23)
            .render(graphics);
        pose.popPose();
    }
}
