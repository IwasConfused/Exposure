package io.github.mortuusars.exposure.event_hub;

import io.github.mortuusars.exposure.Exposure;
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

    }
}
