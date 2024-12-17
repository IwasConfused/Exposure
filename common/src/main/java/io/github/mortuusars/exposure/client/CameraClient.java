package io.github.mortuusars.exposure.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.*;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.NewCamera;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.common.DeactivateActiveCameraCommonPacket;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.world.entity.player.Player;
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

    public static Optional<NewCamera> getActive() {
        return Minecrft.player().getActiveCamera();
    }

    public static boolean isActive() {
        return getActive().isPresent();
    }

    public static void deactivate() {
        Minecrft.player().getActiveCamera().ifPresent(camera -> camera.getItem().deactivate(Minecrft.player(), camera.getItemStack()));
        Minecrft.player().removeActiveCamera();
        Packets.sendToServer(DeactivateActiveCameraCommonPacket.INSTANCE);
    }

    public static void startCapture(Player player, CameraAccessor<?> cameraAccessor, ExposureIdentifier identifier, boolean flashHasFired) {
        cameraAccessor.ifPresentOrElse(player, camera -> {
            camera.getItem().exposeFrameClientside(player, camera.getItemStack(), identifier, flashHasFired);
            ExposureFrameClientData clientSideFrameData = camera.getItem().getClientSideFrameData(player, camera.getItemStack());
            Packets.sendToServer(new CameraAddFrameC2SP(cameraAccessor, clientSideFrameData));
        }, () -> LOGGER.error("Cannot start exposure '{}': cannot get a camera with accessor '{}'.", identifier, cameraAccessor));
    }

    public static <T> void setSetting(Setting<T> setting, T value) {
        Minecrft.player().getActiveCamera().ifPresent(camera -> {
            setting.setAndSync(Minecrft.player(), value);
        });
    }
}
