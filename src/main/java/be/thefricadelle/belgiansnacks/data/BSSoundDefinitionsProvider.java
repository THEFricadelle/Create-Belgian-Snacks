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
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/** sounds.json: each event plays our recording once it exists, vanilla sounds until then (docs/06). */
public class BSSoundDefinitionsProvider extends SoundDefinitionsProvider {
    private final ExistingFileHelper helper;

    public BSSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BelgianSnacks.MOD_ID, helper);
        this.helper = helper;
    }

    @Override
    public void registerSounds() {
        add(BSSoundEvents.FRYER_SIZZLE, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".fryer.sizzle")
            .with(recordingOr("fryer/sizzle", vanilla("liquid/lavapop"), vanilla("block/campfire/crackle1"), vanilla("block/campfire/crackle3"))));
        add(BSSoundEvents.GRINDER_GRIND, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".grinder.grind")
            .with(recordingOr("grinder/grind", vanilla("block/grindstone/grindstone1"), vanilla("block/grindstone/grindstone2"),
                vanilla("block/grindstone/grindstone3"))));
        add(BSSoundEvents.GRINDER_COMPLETE, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".grinder.complete")
            .with(recordingOr("grinder/complete", vanilla("random/levelup"))));
        add(BSSoundEvents.GRINDER_RUNNING, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".grinder.running")
            .with(recordingOr("grinder/running", vanilla("minecart/base"))));
        // THEFricadelle's voice: a villager's grunt until the author records the line.
        for (int i = 0; i < BSSoundEvents.NPC_PHRASES.size(); i++) {
            add(BSSoundEvents.NPC_PHRASES.get(i), SoundDefinition.definition()
                .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".npc.speaks")
                .with(recordingOr("npc/phrase_" + i, vanilla("mob/villager/yes" + (i % 3 + 1)))));
        }
    }

    // A recording of ours at sounds/<path>.ogg (mono Ogg Vorbis, tools/asset_status.py checks it)
    // replaces the vanilla stand-ins as soon as it exists.
    private SoundDefinition.Sound[] recordingOr(String path, SoundDefinition.Sound... standIns) {
        ResourceLocation recording = BelgianSnacks.asResource(path);
        return helper.exists(recording, PackType.CLIENT_RESOURCES, ".ogg", "sounds")
            ? new SoundDefinition.Sound[] {SoundDefinition.Sound.sound(recording, SoundDefinition.SoundType.SOUND)}
            : standIns;
    }

    private static SoundDefinition.Sound vanilla(String path) {
        return SoundDefinition.Sound.sound(ResourceLocation.withDefaultNamespace(path), SoundDefinition.SoundType.SOUND);
    }
}
