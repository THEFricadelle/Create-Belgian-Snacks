/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import java.util.function.Supplier;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BSDataComponents {
    private static final DeferredRegister.DataComponents REGISTER =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BelgianSnacks.MOD_ID);

    // The fat a broken fryer carries in its item, restored when it is placed again.
    public static final Supplier<DataComponentType<SimpleFluidContent>> FRYER_FLUID = REGISTER.registerComponentType("fryer_fluid",
        builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    private BSDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
