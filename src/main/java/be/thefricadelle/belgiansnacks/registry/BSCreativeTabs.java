/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import com.simibubi.create.AllCreativeModeTabs;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BSCreativeTabs {
    public static final String MAIN_TITLE = "itemGroup." + BelgianSnacks.MOD_ID + ".main";

    private static final DeferredRegister<CreativeModeTab> REGISTER =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BelgianSnacks.MOD_ID);

    // Items are added by Registrate through the default tab, not by displayItems.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable(MAIN_TITLE))
            .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
            .icon(BSItems.FRICADELLE::asStack)
            .build());

    private BSCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
        BelgianSnacks.REGISTRATE.defaultCreativeTab(MAIN.getKey());
        // No-op outside datagen; must run before GatherDataEvent since it registers a Registrate provider.
        BelgianSnacks.REGISTRATE.addRawLang(MAIN_TITLE, "Create: Belgian Snacks");
    }
}
