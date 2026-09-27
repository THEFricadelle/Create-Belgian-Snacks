/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import com.mojang.blaze3d.vertex.PoseStack;

import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

/** THEFricadelle with the account's skin, on the wide or the slim player model the skin asks for. */
public class TheFricadelleNpcRenderer extends MobRenderer<TheFricadelleNpc, PlayerModel<TheFricadelleNpc>> {
    private final ClappingPlayerModel wide;
    private final ClappingPlayerModel slim;

    public TheFricadelleNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new ClappingPlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        wide = (ClappingPlayerModel) model;
        slim = new ClappingPlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(TheFricadelleNpc npc, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        model = NpcSkin.get().model() == PlayerSkin.Model.SLIM ? slim : wide;
        super.render(npc, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(TheFricadelleNpc npc) {
        return NpcSkin.get().texture();
    }

    @Override
    protected void scale(TheFricadelleNpc npc, PoseStack pose, float partialTick) {
        // Player models are drawn at 15/16, as the player renderer does.
        pose.scale(0.9375f, 0.9375f, 0.9375f);
    }
}
