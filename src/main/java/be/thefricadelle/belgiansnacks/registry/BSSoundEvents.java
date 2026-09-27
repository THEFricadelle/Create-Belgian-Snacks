/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.registry;

import java.util.List;
import java.util.stream.IntStream;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sound events; sounds.json (datagen) maps them to vanilla sounds until real ones exist. */
public final class BSSoundEvents {
    private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(Registries.SOUND_EVENT, BelgianSnacks.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FRYER_SIZZLE = sound("fryer.sizzle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRINDER_GRIND = sound("grinder.grind");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRINDER_COMPLETE = sound("grinder.complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRINDER_RUNNING = sound("grinder.running");
    /** THEFricadelle's voice, one event per line (npc.phrase.N), so a recording can match its text. */
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> NPC_PHRASES = IntStream.range(0, TheFricadelleNpc.PHRASES)
        .mapToObj(i -> sound("npc.phrase." + i))
        .toList();

    private BSSoundEvents() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(BelgianSnacks.asResource(name)));
    }

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
