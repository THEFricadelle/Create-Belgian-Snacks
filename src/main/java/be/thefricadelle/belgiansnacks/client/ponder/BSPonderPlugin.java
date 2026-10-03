/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSPonderText;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** The mod's Ponder scenes and tag. Texts come from BSPonderText, in both languages. */
public class BSPonderPlugin implements PonderPlugin {
    public static final ResourceLocation TAG = BelgianSnacks.asResource(BSPonderText.TAG);

    @Override
    public String getModId() {
        return BelgianSnacks.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> scenes = helper.withKeyFunction(RegistryEntry::getId);
        scenes.forComponents(BSBlocks.FRYER)
            .addStoryBoard("fryer", FryerScenes::fryer, TAG);
        scenes.forComponents(BSBlocks.SUPREME_GRINDER)
            .addStoryBoard("supreme_grinder", GrinderScenes::collect, TAG)
            .addStoryBoard("supreme_grinder", GrinderScenes::modes);
        scenes.forComponents(BSItems.EXCEPTIONAL_PASTE, BSItems.INCOMPLETE_THE_FRICADELLE, BSItems.RAW_THE_FRICADELLE)
            .addStoryBoard("the_fricadelle_line", AssemblyScenes::theFricadelle, TAG);
        scenes.forComponents(BSItems.ABSOLUTE_PASTE, BSItems.INCOMPLETE_ULTIMATE_FRICADELLE, BSItems.RAW_ULTIMATE_FRICADELLE)
            .addStoryBoard("ultimate_fricadelle_line", AssemblyScenes::ultimateFricadelle, TAG);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(TAG)
            .addToIndex()
            .item(BSItems.FRICADELLE.get(), true, false)
            .title("Create: Belgian Snacks")
            .description("The friterie: its machines and its pastes")
            .register();
        PonderTagRegistrationHelper<ItemProviderEntry<?, ?>> tags = helper.withKeyFunction(RegistryEntry::getId);
        tags.addToTag(TAG)
            .add(BSBlocks.FRYER)
            .add(BSBlocks.SUPREME_GRINDER)
            .add(BSItems.EXCEPTIONAL_PASTE)
            .add(BSItems.ABSOLUTE_PASTE);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        BSPonderText.LINES.forEach((key, line) -> helper.registerSharedText(key, line.english()));
    }
}
