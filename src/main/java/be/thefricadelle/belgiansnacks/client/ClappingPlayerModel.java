/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** A player model that claps: both arms forward, hands meeting and parting. */
public class ClappingPlayerModel extends PlayerModel<TheFricadelleNpc> {
    public ClappingPlayerModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(TheFricadelleNpc npc, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(npc, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float clap = Mth.sin(ageInTicks * 0.9f) * 0.35f;
        rightArm.xRot = -1.25f;
        leftArm.xRot = -1.25f;
        rightArm.yRot = -0.45f + clap;
        leftArm.yRot = 0.45f - clap;
        rightArm.zRot = 0;
        leftArm.zRot = 0;
        rightSleeve.copyFrom(rightArm);
        leftSleeve.copyFrom(leftArm);
    }
}
