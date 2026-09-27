/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.grinder;

import java.util.Locale;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.config.BSConfig;
import be.thefricadelle.belgiansnacks.registry.BSItems;
import net.minecraft.world.item.ItemStack;

/** What the Supreme Grinder is collecting for. Both modes share one collection. */
public enum GrinderMode implements INamedIconOptions {
    THE_(AllIcons.I_PATTERN_CHANCE_25),
    ULTIMATE(AllIcons.I_PATTERN_SOLID);

    private final AllIcons icon;

    GrinderMode(AllIcons icon) {
        this.icon = icon;
    }

    public double ratio() {
        return this == THE_ ? BSConfig.grinderTheFricadelleRatio() : BSConfig.grinderUltimateRatio();
    }

    public ItemStack paste() {
        return this == THE_ ? BSItems.EXCEPTIONAL_PASTE.asStack() : BSItems.ABSOLUTE_PASTE.asStack();
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT).replace("_", "");
    }

    public static GrinderMode byId(String id) {
        for (GrinderMode mode : values()) {
            if (mode.id().equals(id)) {
                return mode;
            }
        }
        return THE_;
    }

    @Override
    public AllIcons getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        return BelgianSnacks.MOD_ID + ".grinder.mode." + id();
    }
}
