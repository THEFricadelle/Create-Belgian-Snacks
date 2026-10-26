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

import be.thefricadelle.belgiansnacks.content.waffle.WaffleBakingRecipe;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Create-styled page: the batter on the left, the waffle iron on its burner, the waffles on the right. */
public class WaffleBakingCategory extends CreateRecipeCategory<WaffleBakingRecipe> {
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public WaffleBakingCategory(Info<WaffleBakingRecipe> info) {
        super(info);
    }

    @Override
    protected void setRecipe(IRecipeLayoutBuilder builder, WaffleBakingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 26)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(recipe.getIngredients().get(0));
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
    protected void draw(WaffleBakingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 47, 31);
        int centre = getBackground().getWidth() / 2 + 3;
        HeatCondition heat = recipe.getRequiredHeat();
        if (heat != HeatCondition.NONE) {
            heater.withHeat(heat.visualizeAsBlazeBurner()).draw(graphics, centre, 56);
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(centre, 56, 200);
        pose.mulPose(Axis.XP.rotationDegrees(-15.5f));
        pose.mulPose(Axis.YP.rotationDegrees(22.5f));
        AnimatedKinetics.defaultBlockElement(BSBlocks.WAFFLE_IRON.getDefaultState())
            .atLocal(0, 0.65, 0)
            .scale(23)
            .render(graphics);
        pose.popPose();
        int seconds = Math.max(1, recipe.getProcessingDuration() / 20);
        graphics.drawString(Minecraft.getInstance().font, seconds + " s", 143, 47, 0x8B8B8B, false);
    }
}
