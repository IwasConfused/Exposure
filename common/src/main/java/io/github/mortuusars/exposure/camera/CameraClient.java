package io.github.mortuusars.exposure.camera;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.*;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.CameraAccessors;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.part.Setting;
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

/*

PhotographerMob
  tick
    if player.isNearby && canPhotograph                             cl&sv
      if !isActive
        activate                                                    cl&sv
      else
        addFrame -> start exposure                                  sv then packet to cl
    else
      attack

CameraStand
  use
    if not active:
    player.setActiveCamera(CameraOnStand(pos))                      cl&sv
    activate(stack)                                                 cl&sv
    -
    if active:
    addFrame -> start exposure                                      sv then packet to cl

  tick
    if stack.active && !player.isCameraActive(stack)                sv
      deactivate(stack)                                             sv


// -----------------------------------------------------------


CameraItem
  use
    if not active:
    player.setActiveCamera(CameraInHand(hand))                      cl&sv
    activate(stack)                                                 cl&sv
    -
    if active:
    addFrame -> start exposure                                      sv then packet to cl


  inventoryTick
    if stack.active && !player.isCameraActive(stack)                sv
      deactivate(stack)                                             sv

ServerPlayer
  tick
    if getActiveCamera.isEmpty || !isActive
      setActiveCamera(null)                                         sv then packet to cl

Player
  drop
    deactivate

Client:
  ESC or INV key
    player.getActiveCamera().deactivate()                           cl&sv by packet

  Viewfinder
    if clientPlayer.hasActiveCamera
      renderViewfinderOverlay(clientPlayer.getActiveCamera)

  Player shutter ticking sound
    if !clientPlayer.getActiveCamera.hasShutterOpen
      mute
    else
      unmute

  Entity shutter ticking sound
    if !entity.getActiveCamera.hasShutterOpen
      mute
    else
      unmute
 */

public class CameraClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private static CameraAccessor<?> activeCameraAccessor;

    public static @Nullable CameraAccessor<?> getActiveCameraAccessor() {
        return activeCameraAccessor;
    }

    public static void setActiveCameraAccessor(@Nullable CameraAccessor<?> cameraAccessor) {
        activeCameraAccessor = cameraAccessor;
    }

    public static Optional<Camera<?>> getActiveCamera() {
        if (activeCameraAccessor == null || Minecraft.getInstance().player == null) {
            return Optional.empty();
        }

        @Nullable Camera<? extends CameraItem> camera = activeCameraAccessor.get(Minecraft.getInstance().player);
        if (camera != null && camera.isActive()) {
            return Optional.of(camera);
        }

        return Optional.empty();
    }

    public static void deactivateCameraAndSendToServer() {
        if (activeCameraAccessor == null || Minecraft.getInstance().player == null) {
            LOGGER.warn("Attempted to close viewfinder without an active camera.");
            return;
        }

        getActiveCamera().ifPresentOrElse(camera -> {
                    camera.deactivate(Minecraft.getInstance().player);
                    Packets.sendToServer(new DeactivateCameraC2SP(activeCameraAccessor));
                },
                () -> LOGGER.warn("Cannot access a camera to deactivate it."));

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

        @Nullable CameraAccessor<?> cameraAccessor = getActiveCameraAccessor();
        if (cameraAccessor != null) {
            cameraAccessor.ifPresent(player, camera -> {
                if (!Viewfinder.isOpen()) {
                    Viewfinder.open();
                } else {
                    Viewfinder.update();
                }
            });
            return;
        }

        setActiveCameraAccessor(null);
        Viewfinder.close();
    }

    public static void handleExposureStart(Player player, CameraAccessor<?> cameraAccessor, ExposureIdentifier identifier, boolean flashHasFired) {
        cameraAccessor.ifPresentOrElse(player, camera -> {
            camera.getItem().exposeFrameClientside(player, camera.getItemStack(), identifier, flashHasFired);
            ExposureFrameClientData clientSideFrameData = camera.getItem().getClientSideFrameData(player, camera.getItemStack());
            Packets.sendToServer(new CameraAddFrameC2SP(cameraAccessor, clientSideFrameData));
        }, () -> LOGGER.error("Cannot start exposure '{}': cannot get a camera with accessor '{}'.", identifier, cameraAccessor));
    }

    public static <T> void setSetting(Setting<T> setting, T value) {
        @Nullable CameraAccessor<?> accessor = getActiveCameraAccessor();
        if (accessor != null) {
            setting.setAndSync(accessor, Minecraft.getInstance().player, value);
        }
    }
}
