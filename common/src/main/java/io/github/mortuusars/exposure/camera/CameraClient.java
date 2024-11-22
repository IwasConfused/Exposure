package io.github.mortuusars.exposure.camera;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.Camera;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.CameraAccessors;
import io.github.mortuusars.exposure.core.ExposureFrameDataFromClient;
import io.github.mortuusars.exposure.core.camera.CompositionGuide;
import io.github.mortuusars.exposure.core.camera.FlashMode;
import io.github.mortuusars.exposure.core.camera.ShutterSpeed;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class CameraClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void handleExposureStart(Player player, CameraAccessor cameraAccessor, String exposureId, boolean flashHasFired) {
        cameraAccessor.getCamera(player).ifPresentOrElse(camera -> {
                    camera.getItem().exposeFrameClientside(player, camera, exposureId, flashHasFired);
                    ExposureFrameDataFromClient clientSideFrameData = camera.getItem().getClientSideFrameData(player, camera.getItemStack());
                    Packets.sendToServer(new CameraAddFrameC2SP(cameraAccessor, clientSideFrameData));
                },
                () -> LOGGER.error("Cannot start exposure '{}': failed to get a camera.", exposureId));
    }

    @Nullable
    private static CameraAccessor activeCameraAccessor;

    public static @Nullable CameraAccessor getActiveCameraAccessor() {
        return activeCameraAccessor;
    }

    public static void setActiveCameraAccessor(@Nullable CameraAccessor cameraAccessor) {
        activeCameraAccessor = cameraAccessor;
    }

    public static Optional<Camera> getActiveCamera() {
        if (activeCameraAccessor == null || Minecraft.getInstance().player == null) {
            return Optional.empty();
        }

        return activeCameraAccessor.getCamera(Minecraft.getInstance().player);
    }

    public static void deactivateCameraAndSendToServer() {
        if (activeCameraAccessor == null || Minecraft.getInstance().player == null) {
            LOGGER.warn("Attempted to close viewfinder without an active camera.");
            return;
        }

        activeCameraAccessor.getCamera(Minecraft.getInstance().player).ifPresentOrElse(camera -> {
                    camera.getItem().deactivate(Minecraft.getInstance().player, camera.getItemStack());
                    Packets.sendToServer(new DeactivateCameraC2SP(activeCameraAccessor));
                },
                () -> {
                    LOGGER.warn("Cannot access a camera to deactivate it.");
                });

        activeCameraAccessor = null;
    }

    public static void onLocalPlayerTick(LocalPlayer player) {
        // TODO: Needs thorough testing. It's still convoluted as hell.

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem cameraItem
                    && cameraItem.isActive(itemInHand)
                    && activeCameraAccessor == null
                    && !Viewfinder.isOpen()) {
                activeCameraAccessor = CameraAccessors.ofHand(hand);
            }
        }

        @Nullable CameraAccessor cameraAccessor = getActiveCameraAccessor();
        if (cameraAccessor != null) {
            Optional<Camera> cameraOpt = cameraAccessor.getCamera(player);
            if (cameraOpt.isPresent()) {
                if (cameraOpt.get().isActive()) {
                    if (!Viewfinder.isOpen()) {
                        Viewfinder.open();
                    } else {
                        Viewfinder.update();
                    }
                    return;
                }
            }
        }

        setActiveCameraAccessor(null);
        Viewfinder.close();
    }

    public static void setZoom(double zoom) {
        if (getActiveCameraAccessor() == null) {
            return;
        }

        getActiveCamera().ifPresent(camera -> {
            camera.getItem().setZoom(camera.getItemStack(), zoom);
            Packets.sendToServer(new CameraSetZoomC2SP(getActiveCameraAccessor(), zoom));
        });
    }

    public static void setShutterSpeed(ShutterSpeed shutterSpeed) {
        if (getActiveCameraAccessor() == null) {
            return;
        }

        getActiveCamera().ifPresent(camera -> {
            camera.getItem().setShutterSpeed(camera.getItemStack(), shutterSpeed);
            Packets.sendToServer(new CameraSetShutterSpeedC2SP(getActiveCameraAccessor(), shutterSpeed));
        });
    }

    public static void setFlashMode(FlashMode flashMode) {
        if (getActiveCameraAccessor() == null) {
            return;
        }

        getActiveCamera().ifPresent(camera -> {
            camera.getItem().setFlashMode(camera.getItemStack(), flashMode);
            Packets.sendToServer(new CameraSetFlashModeC2SP(getActiveCameraAccessor(), flashMode));
        });
    }

    public static void setCompositionGuide(CompositionGuide guide) {
        if (getActiveCameraAccessor() == null) {
            return;
        }

        getActiveCamera().ifPresent(camera -> {
            camera.getItem().setCompositionGuide(camera.getItemStack(), guide);
            Packets.sendToServer(new CameraSetCompositionGuideC2SP(getActiveCameraAccessor(), guide));
        });
    }

    public static void setSelfieMode(boolean inSelfieMode) {
        if (getActiveCameraAccessor() == null) {
            return;
        }

        getActiveCamera().ifPresent(camera -> {
            camera.getItem().setSelfieModeWithEffects(Minecraft.getInstance().player, camera.getItemStack(), inSelfieMode);
            Packets.sendToServer(new CameraSetSelfieModeC2SP(getActiveCameraAccessor(), inSelfieMode));
        });
    }
}
