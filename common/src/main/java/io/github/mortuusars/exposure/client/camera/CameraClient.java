package io.github.mortuusars.exposure.client.camera;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.CameraItem;
import io.github.mortuusars.exposure.world.item.part.CameraSetting;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.common.DeactivateActiveCameraCommonPacket;
import net.minecraft.client.CameraType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CameraClient {
    private static @Nullable Viewfinder activeViewfinder;

    public static void tick() {
        if (activeViewfinder != null) activeViewfinder.tick();
        updateSelfieMode();
    }

    public static Optional<Camera> getActive() {
        return Minecrft.player().getActiveExposureCamera();
    }

    public static boolean isActive() {
        return getActive().isPresent();
    }

    public static void deactivate() {
        Minecrft.player().ifActiveExposureCameraPresent((item, stack) -> item.deactivate(Minecrft.player(), stack));
        Packets.sendToServer(DeactivateActiveCameraCommonPacket.INSTANCE);
    }

    public static <T> void setSetting(CameraSetting<T> setting, T value) {
        Minecrft.player().getActiveExposureCamera().ifPresent(camera -> {
            setting.setAndSync(Minecrft.player(), value);
        });
    }

    // --

    public static @Nullable Viewfinder viewfinder() {
        return activeViewfinder;
    }

    public static void setupViewfinder(@NotNull Camera camera) {
        removeViewfinder();

        if (camera.getItemStack().getItem() instanceof CameraItem item) {
            activeViewfinder = ViewfinderRegistry.getOrThrow(item).apply(camera);
        }
    }

    public static void removeViewfinder() {
        if (activeViewfinder != null) {
            activeViewfinder.close();
            activeViewfinder = null;
        }
    }

    // --

    public static void updateSelfieMode() {
        @Nullable Camera camera = Minecrft.player().activeExposureCamera();
        if (camera != null) {
            boolean inSelfieMode = Minecrft.options().getCameraType() == CameraType.THIRD_PERSON_FRONT;
            if (CameraSetting.SELFIE_MODE.getOrDefault(camera.getItemStack()) != inSelfieMode) {
                CameraClient.setSetting(CameraSetting.SELFIE_MODE, inSelfieMode);
            }
        }
    }
}
