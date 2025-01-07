package io.github.mortuusars.exposure.event_hub;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.SyncColorPalettesS2CP;
import io.github.mortuusars.exposure.network.packet.client.SyncLensesDataS2CP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Stream;

public class ServerEvents {
    public static void onServerSave() {

    }

    public static void serverStarted(MinecraftServer server) {
        Exposure.initServer(server);
    }

    public static void serverStopped(MinecraftServer server) {

    }

    public static void syncDatapack(Stream<ServerPlayer> relevantPlayers) {
        relevantPlayers.forEach(player -> {
            Packets.sendToClient(new SyncColorPalettesS2CP(ExposureServer.colorPalettes().get()), player);
            Packets.sendToClient(new SyncLensesDataS2CP(ExposureServer.lenses().get()), player);
        });
    }
}
