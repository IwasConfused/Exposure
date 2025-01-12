package io.github.mortuusars.exposure.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;

import java.util.Objects;

public class Minecrft {
    public static Minecraft get() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return Objects.requireNonNull(Minecraft.getInstance().player, "Player is not available.");
    }

    public static ClientLevel level() {
        return Objects.requireNonNull(Minecraft.getInstance().level, "Level is not available.");
    }

    public static RegistryAccess registryAccess() {
        return level().registryAccess();
    }

    public static Options options() {
        return Minecraft.getInstance().options;
    }

    public static void execute(Runnable runnable) {
        get().execute(runnable);
    }

    public static void releaseUseButton() {
        Minecraft.getInstance().options.keyUse.setDown(false);
    }

//    public int getTrueGuiScale() {
//
//    }
}
