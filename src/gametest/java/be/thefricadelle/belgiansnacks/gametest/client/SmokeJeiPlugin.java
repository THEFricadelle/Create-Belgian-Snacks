/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest.client;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * Hands the live JEI runtime to {@link ClientSmokeTest}. Lives in the gametest source set, so it never
 * ships; it only records the runtime and registers nothing.
 */
@JeiPlugin
public class SmokeJeiPlugin implements IModPlugin {
    static volatile IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return BelgianSnacks.asResource("client_smoke");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
