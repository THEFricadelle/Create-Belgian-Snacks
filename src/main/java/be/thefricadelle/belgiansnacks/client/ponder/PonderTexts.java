/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client.ponder;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.registry.BSPonderText;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.minecraft.world.phys.Vec3;

/** The few ways our scenes put text on screen, all through BSPonderText's stable keys. */
final class PonderTexts {
    private PonderTexts() {
    }

    static void title(SceneBuilder scene, String id) {
        scene.title(id, BSPonderText.TITLES.get(id).english());
    }

    /** A key-framed line pointing at a spot, shown for {@code ticks}; the scene then waits as long, plus 10. */
    static void say(SceneBuilder scene, String key, int ticks, Vec3 at) {
        say(scene, key, ticks, at, PonderPalette.WHITE);
    }

    static void say(SceneBuilder scene, String key, int ticks, Vec3 at, PonderPalette colour) {
        if (!BSPonderText.LINES.containsKey(key)) {
            throw new IllegalArgumentException("no Ponder line " + key);
        }
        scene.overlay().showText(ticks)
            .attachKeyFrame()
            .colored(colour)
            .sharedText(BelgianSnacks.asResource(key))
            .pointAt(at)
            .placeNearTarget();
        scene.idle(ticks + 10);
    }
}
