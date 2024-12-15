package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PacketsImpl {
    @Nullable
    private static MinecraftServer server;

    public static void sendToServer(IPacket packet) {
        FabricS2CPackets.sendToServer(packet);
    }

    public static void sendToClient(IPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToClients(IPacket packet, Predicate<ServerPlayer> filter) {
        if (server == null) {
            Exposure.LOGGER.error("Cannot send a packet to players. Server is not available.");
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (filter.test(player)) {
                sendToClient(packet, player);
            }
        }
    }

    public static void sendToAllClients(IPacket packet) {
        if (server == null) {
            Exposure.LOGGER.error("Cannot send a packet to all players. Server is not available.");
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendToClient(packet, player);
        }
    }

    public static void sendToPlayersNear(IPacket packet, @NotNull ServerLevel level, @Nullable ServerPlayer excludedPlayer,
                                         double x, double y, double z, double radius) {
        sendToClients(packet, player -> {
            if (player != excludedPlayer && player.level().dimension() == level.dimension()) {
                double d0 = x - player.getX();
                double d1 = y - player.getY();
                double d2 = z - player.getZ();
                return d0 * d0 + d1 * d1 + d2 * d2 < radius * radius;
            }

            return false;
        });
    }

    public static void onServerStarting(MinecraftServer server) {
        // Store server to access from static context:
        PacketsImpl.server = server;
    }

    public static void onServerStopped(MinecraftServer server) {
        PacketsImpl.server = null;
    }
}
