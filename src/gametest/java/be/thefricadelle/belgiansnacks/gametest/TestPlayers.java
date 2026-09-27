/*
 * Create: Belgian Snacks - Copyright (C) 2026 THEFricadelle. All rights reserved.
 * SPDX-License-Identifier: LicenseRef-Create-Belgian-Snacks-ARR
 *
 * Proprietary, source-available software. Public visibility of this source
 * grants no right to copy, reuse, redistribute, or create derivative works.
 * See LICENSE and CONTRIBUTING.md at the repository root.
 */

package be.thefricadelle.belgiansnacks.gametest;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * A server player standing in a GameTest level, for what needs one the level lists and real
 * advancements. Neither of the usual ones fits: the mock player logs in over a test connection that
 * Create, Jade and this mod's payloads crash, and NeoForge gives fake players dummy advancements.
 * This one is a plain server player whose connection swallows every packet.
 */
final class TestPlayers {
    private TestPlayers() {
    }

    static ServerPlayer inLevel(GameTestHelper helper, Vec3 relative) {
        var level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "test-player");
        ServerPlayer player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        player.connection = new SilentConnection(player, profile);
        Vec3 at = helper.absoluteVec(relative);
        player.moveTo(at.x, at.y, at.z, 0, 0);
        level.addNewPlayer(player);
        return player;
    }

    static void remove(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }

    private static final class SilentConnection extends ServerGamePacketListenerImpl {
        SilentConnection(ServerPlayer player, GameProfile profile) {
            super(player.getServer(), new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        }

        @Override
        public void send(Packet<?> packet) {
        }

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
        }
    }
}
