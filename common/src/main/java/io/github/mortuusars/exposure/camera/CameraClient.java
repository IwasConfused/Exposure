package io.github.mortuusars.exposure.camera;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.MC;
import io.github.mortuusars.exposure.core.*;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.common.DeactivateActiveCameraCommonPacket;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

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

//    @Nullable
//    private static CameraAccessor<?> activeCameraAccessor;

//    public static @Nullable CameraAccessor<?> getActiveCameraAccessor() {
//        return activeCameraAccessor;
//    }

//    public static void setActiveCameraAccessor(@Nullable CameraAccessor<?> cameraAccessor) {
//        activeCameraAccessor = cameraAccessor;
//    }

//    public static Optional<Camera<?>> getActiveCamera() {
//        if (activeCameraAccessor == null || Minecraft.getInstance().player == null) {
//            return Optional.empty();
//        }
//
//        @Nullable Camera<? extends OldCameraItem> camera = activeCameraAccessor.get(Minecraft.getInstance().player);
//        if (camera != null && camera.isActive()) {
//            return Optional.of(camera);
//        }
//
//        return Optional.empty();
//    }

//    public static void onLocalPlayerTick(LocalPlayer player) {
//        // TODO: Needs thorough testing. It's still convoluted as hell.
//
//        for (InteractionHand hand : InteractionHand.values()) {
//            ItemStack itemInHand = player.getItemInHand(hand);
//            if (itemInHand.getItem() instanceof OldCameraItem cameraItem
//                    && cameraItem.isActive(itemInHand)
//                    && activeCameraAccessor == null
//                    && !OldViewfinder.isOpen()) {
//                activeCameraAccessor = CameraAccessors.ofHand(hand);
//            }
//        }
//
//        @Nullable CameraAccessor<?> cameraAccessor = getActiveCameraAccessor();
//        if (cameraAccessor != null) {
//            cameraAccessor.ifPresent(player, camera -> {
//                if (!OldViewfinder.isOpen()) {
//                    OldViewfinder.open();
//                } else {
//                    OldViewfinder.update();
//                }
//            });
//            return;
//        }
//
//        setActiveCameraAccessor(null);
//        OldViewfinder.close();
//    }

    public static void handleExposureStart(Player player, CameraAccessor<?> cameraAccessor, ExposureIdentifier identifier, boolean flashHasFired) {
        cameraAccessor.ifPresentOrElse(player, camera -> {
            camera.getItem().exposeFrameClientside(player, camera.getItemStack(), identifier, flashHasFired);
            ExposureFrameClientData clientSideFrameData = camera.getItem().getClientSideFrameData(player, camera.getItemStack());
            Packets.sendToServer(new CameraAddFrameC2SP(cameraAccessor, clientSideFrameData));
        }, () -> LOGGER.error("Cannot start exposure '{}': cannot get a camera with accessor '{}'.", identifier, cameraAccessor));
    }

//    public static <T> void setSetting(Setting<T> setting, T value) {
//        @Nullable CameraAccessor<?> accessor = getActiveCameraAccessor();
//        if (accessor != null) {
//            setting.setAndSync(accessor, Minecraft.getInstance().player, value);
//        }
//    }

    public static <T> void setSetting(Setting<T> setting, T value) {
        MC.player().getActiveCamera().ifPresent(camera -> {
            setting.setAndSync(MC.player(), value);
        });
    }

    public static void deactivate() {
        MC.player().getActiveCamera().ifPresent(camera -> camera.getItem().deactivate(MC.player(), camera.getItemStack()));
        MC.player().removeActiveCamera();
        Packets.sendToServer(DeactivateActiveCameraCommonPacket.INSTANCE);
    }
}
