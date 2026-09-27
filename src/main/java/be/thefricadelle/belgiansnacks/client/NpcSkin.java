/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.client;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import be.thefricadelle.belgiansnacks.BelgianSnacks;
import be.thefricadelle.belgiansnacks.content.npc.TheFricadelleNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

/**
 * The skin of the THE_Fricadelle account, looked up once through Mojang's services as a player head
 * does. Until it arrives, or without a connection, the default skin for that name.
 */
public final class NpcSkin {
    @Nullable
    private static volatile PlayerSkin loaded;
    @Nullable
    private static volatile String account;
    private static boolean requested;

    private NpcSkin() {
    }

    public static PlayerSkin get() {
        if (!requested) {
            requested = true;
            SkullBlockEntity.fetchGameProfile(TheFricadelleNpc.SKIN_ACCOUNT)
                .thenCompose(profile -> profile.map(p -> {
                    account = p.getName();
                    return Minecraft.getInstance().getSkinManager().getOrLoad(p);
                }).orElseGet(() -> CompletableFuture.completedFuture(null)))
                .whenComplete((skin, error) -> {
                    if (skin != null) {
                        loaded = skin;
                    } else if (error != null) {
                        BelgianSnacks.LOGGER.debug("No online skin for {}, the default one stays", TheFricadelleNpc.SKIN_ACCOUNT, error);
                    }
                });
        }
        PlayerSkin skin = loaded;
        return skin != null ? skin : DefaultPlayerSkin.get(UUIDUtil.createOfflinePlayerUUID(TheFricadelleNpc.SKIN_ACCOUNT));
    }

    /** True once the account's own skin replaced the default one. */
    public static boolean isOnline() {
        return loaded != null;
    }

    /** The account name Mojang's services answered with, once they did. */
    @Nullable
    public static String account() {
        return account;
    }
}
