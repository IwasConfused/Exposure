package io.github.mortuusars.exposure.network;


import com.google.common.base.Preconditions;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class Packets {
    @ExpectPlatform
    public static void sendToServer(IPacket packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToClient(IPacket packet, ServerPlayer player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToClients(IPacket packet, Predicate<ServerPlayer> filter) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToAllClients(IPacket packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToPlayersNear(IPacket packet, ServerLevel level, @Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        throw new AssertionError();
    }

//    public static void sendToClients(IPacket packet, ServerPlayer origin, Predicate<ServerPlayer> filter) {
//        Preconditions.checkState(origin.getServer() != null, "Server cannot be null");
//        for (ServerPlayer player : origin.getServer().getPlayerList().getPlayers()) {
//            if (filter.test(player))
//                sendToClient(packet, player);
//        }
//    }

//    public static void sendToOtherClients(IPacket packet, @Nullable Player excludedPlayer) {
//        sendToClients(packet, serverPlayer -> !serverPlayer.equals(excludedPlayer));
//    }
//
//    public static void sendToOtherClients(IPacket packet, ServerPlayer excludedPlayer, Predicate<ServerPlayer> filter) {
//        sendToClients(packet, excludedPlayer, serverPlayer -> !serverPlayer.equals(excludedPlayer) && filter.test(serverPlayer));
//    }

//    public static void sendToPlayersInRange(IPacket packet, ServerLevel level, Vec3 position, float range) {
//        sendToPlayersInRange(packet, null, level, position, range);
//    }

//    public static void sendToPlayersInRange(IPacket packet, @Nullable Player excludedPlayer, ServerLevel level,
//                                            Vec3 position, float range) {
//        sendToClients(packet, player -> {
//            if (player.equals(excludedPlayer)) {
//                return false;
//            }
//
//            double distance = Math.sqrt(player.distanceToSqr(position));
//            if (distance > range) {
//
//            }
//        });
//        for (ServerPlayer player : level.players()) {
//            if (player.equals(excludedPlayer)) {
//                continue;
//            }
//
//            double distance = Math.sqrt(player.distanceToSqr(position));
//            if (distance > range) {
//                sendToClient(packet, player);
//            }
//        }
//    }
}