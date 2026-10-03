/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.jade;

import be.thefricadelle.belgiansnacks.content.fryer.FryerBlock;
import be.thefricadelle.belgiansnacks.content.grinder.SupremeGrinderBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Found by Jade's annotation scan, so it is only ever loaded when Jade is installed. Jade already
 * shows the fat tank and the item slots from the capabilities; this adds the machines' status lines.
 */
@WailaPlugin
public class BSJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FryerStatusProvider.INSTANCE, FryerBlock.class);
        registration.registerBlockComponent(GrinderStatusProvider.INSTANCE, SupremeGrinderBlock.class);
    }
}
