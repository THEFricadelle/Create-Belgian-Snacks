/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.food.FoodIndex;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderMode;
import be.thefricadelle.belgiansnacks.content.grinder.GrinderProgress;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Display only: the grinder's goal is not a recipe (the food list is unknown at datagen), so each
 * mode is shown as one entry whose input cycles through the foods the server sent.
 */
public class GrindingGoalCategory implements IRecipeCategory<GrinderMode> {
    public static final RecipeType<GrinderMode> TYPE = RecipeType.create(BelgianSnacks.MOD_ID, "grinding_goal", GrinderMode.class);
    private static final String KEY = BelgianSnacks.MOD_ID + ".recipe.grinding_goal";
    private static final int WIDTH = 177;
    private static final int HEIGHT = 96;

    private final IDrawable icon;

    public GrindingGoalCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(BSBlocks.SUPREME_GRINDER.asStack());
    }

    @Override
    public RecipeType<GrinderMode> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(KEY);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(GrinderMode mode) {
        return BelgianSnacks.asResource("grinding_goal/" + mode.id());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrinderMode mode, IFocusGroup focuses) {
        // Read when the page is built: the index arrives from the server after JEI has started.
        List<ItemStack> foods = new ArrayList<>();
        for (ResourceLocation id : FoodIndex.client().ids()) {
            foods.add(new ItemStack(BuiltInRegistries.ITEM.get(id)));
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 14)
            .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
            .addItemStacks(foods);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 141, 14)
            .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
            .addItemStack(mode.paste());
    }

    @Override
    public void draw(GrinderMode mode, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 47, 19);
        drawGrinder(graphics, WIDTH / 2 + 3, 58);

        Font font = Minecraft.getInstance().font;
        int total = FoodIndex.client().size();
        double ratio = mode.ratio();
        Component needed = Component.translatable(KEY + ".foods", GrinderProgress.goal(ratio, total), total, Math.round(ratio * 100));
        graphics.drawString(font, needed, (WIDTH - font.width(needed)) / 2, 74, 0x8B8B8B, false);
        Component each = Component.translatable(KEY + ".each");
        graphics.drawString(font, each, (WIDTH - font.width(each)) / 2, 85, 0x8B8B8B, false);
    }

    // Same projection as Create's animated machines.
    private static void drawGrinder(GuiGraphics graphics, int x, int y) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 200);
        pose.mulPose(Axis.XP.rotationDegrees(-15.5f));
        pose.mulPose(Axis.YP.rotationDegrees(22.5f));
        AnimatedKinetics.defaultBlockElement(BSBlocks.SUPREME_GRINDER.getDefaultState())
            .scale(23)
            .render(graphics);
        pose.popPose();
    }
}
