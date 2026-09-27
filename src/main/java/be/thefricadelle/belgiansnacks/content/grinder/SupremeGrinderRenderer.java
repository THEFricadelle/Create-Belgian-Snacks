/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.client.BSPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws the mince rising in the pit, and spins the blades when Flywheel is off (with Flywheel on,
 * SingleAxisRotatingVisual does it).
 */
public class SupremeGrinderRenderer extends KineticBlockEntityRenderer<SupremeGrinderBlockEntity> {
    private static final ResourceLocation MINCE = BelgianSnacks.asResource("block/supreme_grinder/mince");
    // The pit floor, and the highest surface: just under the blades' hub.
    private static final float FLOOR = 2 / 16f;
    private static final float TOP = 11.5f / 16f;
    private static final float EDGE = 2 / 16f;

    public SupremeGrinderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SupremeGrinderBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        float level = be.minceLevel();
        if (level <= 0) {
            return;
        }
        float y = FLOOR + (TOP - FLOOR) * level;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MINCE);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose pose = ms.last();
        float min = EDGE;
        float max = 1 - EDGE;
        // The surface only: the pit's walls hide the sides.
        vertex(consumer, pose, min, y, min, sprite.getU(min), sprite.getV(min), light, overlay);
        vertex(consumer, pose, min, y, max, sprite.getU(min), sprite.getV(max), light, overlay);
        vertex(consumer, pose, max, y, max, sprite.getU(max), sprite.getV(max), light, overlay);
        vertex(consumer, pose, max, y, min, sprite.getU(max), sprite.getV(min), light, overlay);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light, int overlay) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SupremeGrinderBlockEntity be, BlockState state) {
        return CachedBuffers.partial(BSPartialModels.GRINDER_BLADES, state);
    }
}
