package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PacketsImpl {
    @Nullable
    private static MinecraftServer server;

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(AlbumSignC2SP.TYPE, AlbumSignC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(AlbumSyncNoteC2SP.TYPE, AlbumSyncNoteC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CameraAddFrameC2SP.TYPE, CameraAddFrameC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CameraSetCompositionGuideC2SP.TYPE, CameraSetCompositionGuideC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CameraSetFlashModeC2SP.TYPE, CameraSetFlashModeC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CameraSetSelfieModeC2SP.TYPE, CameraSetSelfieModeC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CameraSetShutterSpeedC2SP.TYPE, CameraSetShutterSpeedC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CameraSetZoomC2SP.TYPE, CameraSetZoomC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DeactivateCameraC2SP.TYPE, DeactivateCameraC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExposureDataPartC2SP.TYPE, ExposureDataPartC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(OpenCameraAttachmentsInCreativePacketC2SP.TYPE, OpenCameraAttachmentsInCreativePacketC2SP.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QueryExposureDataC2SP.TYPE, QueryExposureDataC2SP.STREAM_CODEC);

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
