package io.github.mortuusars.exposure.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

    public static void sendToPlayersNear(IPacket packet, ServerLevel level, @Nullable ServerPlayer excluded,
                                         Entity entity, double radius) {
        sendToPlayersNear(packet, level, excluded, entity.getX(), entity.getY(), entity.getZ(), radius);
    }
}