package io.github.mortuusars.exposure.network.neoforge;


import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class PacketsImpl {
    public static void handle(IPacket packet, IPayloadContext context) {
        packet.handle(context.flow(), context.player());
    }

    public static void sendToServer(IPacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToClient(IPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToClients(IPacket packet, Predicate<ServerPlayer> filter) {
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(),
                "Cannot send clientbound payloads on the client");

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (filter.test(player)) {
                sendToClient(packet, player);
            }
        }
    }

    public static void sendToAllClients(IPacket packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendToPlayersNear(IPacket packet, @NotNull ServerLevel level, @Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        PacketDistributor.sendToPlayersNear(level, excluded, x, y, z, radius, packet);
    }
}