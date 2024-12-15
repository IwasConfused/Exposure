package io.github.mortuusars.exposure.client;

import net.minecraft.client.Minecraft;

public class Client {
    public static void releaseUseButton() {
        Minecraft.getInstance().options.keyUse.setDown(false);
    }
}
