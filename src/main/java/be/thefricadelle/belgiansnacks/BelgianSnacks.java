/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;

import be.thefricadelle.belgiansnacks.command.BSCommands;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.content.food.FoodIndexEvents;
import be.thefricadelle.belgiansnacks.content.fryer.FryerBlockEntity;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlockEntity;
import be.thefricadelle.belgiansnacks.data.BSDatagen;
import be.thefricadelle.belgiansnacks.network.BSNetwork;
import be.thefricadelle.belgiansnacks.registry.BSBlockEntities;
import be.thefricadelle.belgiansnacks.registry.BSBlocks;
import be.thefricadelle.belgiansnacks.registry.BSCreativeTabs;
import be.thefricadelle.belgiansnacks.registry.BSDataComponents;
import be.thefricadelle.belgiansnacks.registry.BSFluids;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import be.thefricadelle.belgiansnacks.registry.BSLang;
import be.thefricadelle.belgiansnacks.registry.BSRecipeTypes;
import be.thefricadelle.belgiansnacks.registry.BSSoundEvents;
import be.thefricadelle.belgiansnacks.registry.BSTags;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(BelgianSnacks.MOD_ID)
public class BelgianSnacks {
    public static final String MOD_ID = "create_belgian_snacks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
        .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));

    public BelgianSnacks(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, BSConfig.SPEC, MOD_ID + "-server.toml");

        // The default tab must be set before any item entry is created.
        BSCreativeTabs.register(modEventBus);
        BSTags.register();
        BSItems.register();
        BSFluids.register();
        BSBlocks.register();
        BSBlockEntities.register();
        BSDataComponents.register(modEventBus);
        BSSoundEvents.register(modEventBus);
        BSRecipeTypes.register(modEventBus);
        BSLang.register();

        modEventBus.addListener(FryerBlockEntity::registerCapabilities);
        modEventBus.addListener(SupremeGrinderBlockEntity::registerCapabilities);
        modEventBus.addListener(BSNetwork::register);
        FoodIndexEvents.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(BSCommands::register);
        modEventBus.addListener(BSDatagen::gatherData);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
