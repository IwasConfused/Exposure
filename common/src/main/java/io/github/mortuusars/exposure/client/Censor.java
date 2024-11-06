package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.render.image.ResourceImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class Censor {
    public static final Image HIDDEN_IMAGE = ResourceImage.getOrCreate(Exposure.resource("textures/exposure/pack.png"));

    public static boolean isAllowedToRender(ExposureFrame frame) {
        @Nullable Player player = Minecraft.getInstance().player;

        if (Config.Client.HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS.get()
                && (player == null || !frame.isTakenBy(player))) {
            return false;
        }

        if (Config.Client.HIDE_LOADED_PHOTOGRAPHS_MADE_BY_OTHERS.get()
                && frame.isFromFile()
                && (player == null || !frame.isTakenBy(player))) {
            return false;
        }

        return true;
    }
}
