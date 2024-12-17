package io.github.mortuusars.exposure.integration.compat;

import com.xtracr.realcamera.compat.DisableHelper;
import io.github.mortuusars.exposure.client.Minecrft;
import net.minecraft.world.entity.player.Player;

public class RealCameraCompat {
    public static void init() {
        DisableHelper.registerOr("renderModel", entity -> entity instanceof Player && Minecrft.player().getActiveCamera().isPresent());
    }
}
