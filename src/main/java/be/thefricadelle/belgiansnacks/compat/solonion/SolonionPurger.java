/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, closed-source software. Access to this source is restricted and
 * grants no right to copy, share, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.compat.solonion;

import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.uninstall.ReferencePurger;
import be.thefricadelle.belgiansnacks.content.uninstall.SavedStackLists;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Spice of Life: Onion (mod id {@code solonion}) keeps every food a player ate as a saved item stack.
 * Once our items are gone, those entries load as empty stacks and Solonion fails to save them, which
 * stops the player from joining (bug reported on 20/10/2026). This removes our entries beforehand.
 *
 * No Solonion class is referenced at compile time: the history is its NeoForge attachment
 * {@code solonion:foodlist}, read through NeoForge's {@link INBTSerializable}; the benefit refresh
 * and the client sync are Solonion's public methods, called by reflection.
 */
public final class SolonionPurger implements ReferencePurger {
    public static final String MOD_ID = "solonion";
    static final ResourceLocation FOOD_LIST = ResourceLocation.fromNamespaceAndPath(MOD_ID, "foodlist");

    private boolean refreshResolved;
    @Nullable
    private Method syncFoodList;
    @Nullable
    private Method updatePlayerBenefits;
    @Nullable
    private Object events;

    @Override
    public String name() {
        return MOD_ID;
    }

    @Override
    public int count(ServerPlayer player) {
        INBTSerializable<ListTag> history = history(player);
        return history == null ? 0 : SavedStackLists.count(history, player.registryAccess(), BelgianSnacks.MOD_ID);
    }

    @Override
    public int purge(ServerPlayer player) {
        INBTSerializable<ListTag> history = history(player);
        if (history == null) {
            return 0;
        }
        int removed = SavedStackLists.purge(history, player.registryAccess(), BelgianSnacks.MOD_ID);
        if (removed > 0) {
            refresh(player);
        }
        return removed;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static INBTSerializable<ListTag> history(ServerPlayer player) {
        AttachmentType<?> type = NeoForgeRegistries.ATTACHMENT_TYPES.get(FOOD_LIST);
        if (type == null || !player.hasData(type)) {
            return null;
        }
        Object data = player.getData(type);
        return data instanceof INBTSerializable<?> serializable ? (INBTSerializable<ListTag>) serializable : null;
    }

    // Diversity benefits and the client's copy of the list follow the shorter history. Without the
    // methods (another Solonion version), the purge is still saved and both catch up on the next sync.
    private synchronized void refresh(ServerPlayer player) {
        if (!refreshResolved) {
            refreshResolved = true;
            try {
                syncFoodList = Class.forName("team.creative.solonion.api.SOLOnionAPI").getMethod("syncFoodList", Player.class);
                events = Class.forName("team.creative.solonion.common.SOLOnion").getField("EVENT").get(null);
                updatePlayerBenefits = events.getClass().getMethod("updatePlayerBenefits", Player.class);
            } catch (ReflectiveOperationException | RuntimeException e) {
                BelgianSnacks.LOGGER.warn("Solonion's refresh methods were not found; the purged history syncs on its next update", e);
            }
        }
        try {
            if (updatePlayerBenefits != null) {
                updatePlayerBenefits.invoke(events, player);
            }
            if (syncFoodList != null) {
                syncFoodList.invoke(null, player);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            BelgianSnacks.LOGGER.warn("Could not refresh Solonion for {}", player.getGameProfile().getName(), e);
        }
    }
}
