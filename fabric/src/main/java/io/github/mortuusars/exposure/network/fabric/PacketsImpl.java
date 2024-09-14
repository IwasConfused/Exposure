package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class PacketsImpl {
    @Nullable
    private static MinecraftServer server;

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(AlbumSignC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(AlbumSyncNoteC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(CameraAddFrameC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(CameraSetCompositionGuideC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(CameraSetFlashModeC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(CameraSetSelfieModeC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(CameraSetShutterSpeedC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(CameraSetZoomC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(DeactivateCameraC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(ExposureDataPartC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(OpenCameraAttachmentsInCreativePacketC2SP.TYPE, PacketsImpl::handleServerboundPacket);
        ServerPlayNetworking.registerGlobalReceiver(QueryExposureDataC2SP.TYPE, PacketsImpl::handleServerboundPacket);
    }

    private static <T extends IPacket> void handleServerboundPacket(T payload, ServerPlayNetworking.Context context) {
        payload.handle(PacketFlow.SERVERBOUND, context.player());
    }

    public static void registerS2CPackets() {
        ClientPackets.registerS2CPackets();
    }

    public static void sendToServer(IPacket packet) {
        ClientPackets.sendToServer(packet);
    }

    public static void sendToClient(IPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToAllClients(IPacket packet) {
        if (server == null) {
            Exposure.LOGGER.error("Cannot send a packet to all players. Server is not present.");
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    public static void onServerStarting(MinecraftServer server) {
        // Store server to access from static context:
        PacketsImpl.server = server;
    }

    public static void onServerStopped(MinecraftServer server) {
        PacketsImpl.server = null;
    }
}
