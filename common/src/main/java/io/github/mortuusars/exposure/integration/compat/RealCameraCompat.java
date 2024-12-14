package io.github.mortuusars.exposure.integration.compat;

import com.xtracr.realcamera.compat.DisableHelper;
import io.github.mortuusars.exposure.camera.CameraClient;
import net.minecraft.world.entity.player.Player;

public class RealCameraCompat {
    public static void init() {
        DisableHelper.registerOr("renderModel", entity -> entity instanceof Player && CameraClient.getActiveCamera().isPresent());
    }
}
