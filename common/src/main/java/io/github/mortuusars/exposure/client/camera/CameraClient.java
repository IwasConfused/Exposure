package io.github.mortuusars.exposure.client.camera;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
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
        return Minecrft.player().getActiveExposureCameraOptional();
    }

    public static boolean isActive() {
        return getActive().isPresent();
    }

    public static void deactivate() {
        Minecrft.player().getActiveExposureCameraOptional().ifPresent(camera -> {
            camera.map((item, stack) -> item.deactivate(camera.getHolder().asEntity(), stack));
            Minecrft.player().removeActiveExposureCamera();
        });
        Packets.sendToServer(ActiveCameraDeactivateCommonPacket.INSTANCE);
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
        Camera camera = Minecrft.player().getActiveExposureCamera();
        if (camera != null) {
            boolean inSelfieMode = Minecrft.options().getCameraType() == CameraType.THIRD_PERSON_FRONT;
            CameraSettings.SELFIE_MODE.setAndSync(camera, inSelfieMode);
        }
    }
}
