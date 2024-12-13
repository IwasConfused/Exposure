package io.github.mortuusars.exposure.integration.compat;

import com.xtracr.realcamera.compat.DisableHelper;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.Camera;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class RealCameraCompat {
    public static void init() {
        DisableHelper.registerOr("renderModel", entity -> {
            if (!(entity instanceof Player player)) {
                return false;
            }

            @Nullable CameraAccessor cameraAccessor = CameraClient.getActiveCameraAccessor();
            if (cameraAccessor == null) {
                return false;
            }

            return cameraAccessor.getCamera(player).map(Camera::isActive).orElse(false);
        });
    }
}
