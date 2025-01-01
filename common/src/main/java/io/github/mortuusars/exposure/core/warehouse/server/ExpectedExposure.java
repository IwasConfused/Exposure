package io.github.mortuusars.exposure.core.warehouse.server;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public record ExpectedExposure(String id, long timeoutAt, BiConsumer<ServerPlayer, String> onReceived) {
    public boolean isTimedOut(long currentUnixTime) {
        return currentUnixTime > timeoutAt;
    }
}
