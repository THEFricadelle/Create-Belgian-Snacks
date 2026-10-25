/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.content.crop;

import java.util.function.Supplier;

import be.thefricadelle.belgiansnacks.config.BSConfig;
import net.minecraft.world.level.ItemLike;

/** The chicon is forced in the dark, as in a Belgian cellar: it grows only where light is low. */
public class ChiconCropBlock extends ShortCropBlock {
    public ChiconCropBlock(Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties, seed);
    }

    @Override
    protected boolean growsAt(int light) {
        return light <= BSConfig.cropsDarkMaxLight();
    }
}
