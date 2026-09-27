/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.data;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/** sounds.json: our events point at vanilla sounds until real recordings exist (docs/06). */
public class BSSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public BSSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BelgianSnacks.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(BSSoundEvents.FRYER_SIZZLE, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".fryer.sizzle")
            .with(vanilla("liquid/lavapop"), vanilla("block/campfire/crackle1"), vanilla("block/campfire/crackle3")));
        add(BSSoundEvents.FRICADELLE_BURP, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".fricadelle.burp")
            .with(vanilla("random/burp")));
        add(BSSoundEvents.GRINDER_GRIND, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".grinder.grind")
            .with(vanilla("block/grindstone/grindstone1"), vanilla("block/grindstone/grindstone2"), vanilla("block/grindstone/grindstone3")));
        add(BSSoundEvents.GRINDER_COMPLETE, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".grinder.complete")
            .with(vanilla("random/levelup")));
    }

    private static SoundDefinition.Sound vanilla(String path) {
        return SoundDefinition.Sound.sound(ResourceLocation.withDefaultNamespace(path), SoundDefinition.SoundType.SOUND);
    }
}
