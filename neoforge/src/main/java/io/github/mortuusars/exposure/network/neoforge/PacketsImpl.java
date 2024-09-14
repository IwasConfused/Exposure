package io.github.mortuusars.exposure.network.neoforge;


import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    public static void sendToAllClients(IPacket packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}