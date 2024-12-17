package io.github.mortuusars.exposure.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import java.util.Objects;

public class MC {
    public static Minecraft get() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return Objects.requireNonNull(Minecraft.getInstance().player, "Player is not available.");
    }

    public static ClientLevel level() {
        return Objects.requireNonNull(Minecraft.getInstance().level, "Level is not available.");
    }

    public static Options options() {
        return Minecraft.getInstance().options;
    }
}
