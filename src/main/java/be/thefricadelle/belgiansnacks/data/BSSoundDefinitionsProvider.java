package be.thefricadelle.belgiansnacks.data;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/** sounds.json: each event plays our own recording at sounds/<path>.ogg (docs/06). */
public class BSSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public BSSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BelgianSnacks.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(BSSoundEvents.NPC_VOICE, SoundDefinition.definition()
            .subtitle("subtitles." + BelgianSnacks.MOD_ID + ".npc.speaks")
            .with(recording("npc/phrase_" + TheFricadelleNpc.SPOKEN_PHRASE)));
    }

    // Mono Ogg Vorbis, checked by tools/asset_status.py; the helper fails datagen if the file is missing.
    private static SoundDefinition.Sound recording(String path) {
        return SoundDefinition.Sound.sound(BelgianSnacks.asResource(path), SoundDefinition.SoundType.SOUND);
    }
}
