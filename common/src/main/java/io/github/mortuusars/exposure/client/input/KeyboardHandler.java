package io.github.mortuusars.exposure.client.input;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;

public class KeyboardHandler {
    public static boolean handleKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        return Minecrft.get().player != null
                && CameraClient.viewfinder() != null
                && CameraClient.viewfinder().keyPressed(key, scanCode, action);
    }

//    private static boolean handleCameraKeyPress(int key, int scanCode, int action) {
//        Minecraft minecraft = Minecraft.getInstance();
//        @Nullable LocalPlayer player = minecraft.player;
//        if (player == null) {
//            return false;
//        }
//
//        if (CameraClient.getActiveCamera().isEmpty()) {
//            return false;
//        }
//
//        if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get()
//                && Minecraft.getInstance().options.keyAttack.matches(key, scanCode)
//                && !(Minecraft.getInstance().screen instanceof CameraControlsScreen)) {
//            return true;
//        }
//
//        if (minecraft.options.keyTogglePerspective.matches(key, scanCode)) {
//            if (action == InputConstants.PRESS)
//                return true;
//
//            CameraType currentCameraType = minecraft.options.getCameraType();
//            CameraType newCameraType = currentCameraType == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_FRONT
//                    : CameraType.FIRST_PERSON;
//
//            minecraft.options.setCameraType(newCameraType);
//            return true;
//        }
//
//        if (key == InputConstants.KEY_ESCAPE || minecraft.options.keyInventory.matches(key, scanCode)) {
//            if (action == InputConstants.PRESS) { // TODO: Check if activating on release is not causing problems
//                if (minecraft.screen instanceof CameraControlsScreen viewfinderControlsScreen) {
//                    viewfinderControlsScreen.onClose();
//                } else if (Minecraft.getInstance().player != null) {
//                    Minecraft.getInstance().player.removeActiveCamera();
//                    Packets.sendToServer(new RemoveActiveCameraS2CP());
//                }
//            }
//            return true;
//        }
//
//        if (!OldViewfinder.isLookingThrough())
//            return false;
//
//        if (!(minecraft.screen instanceof CameraControlsScreen)) {
//            if (ExposureClient.getCameraControlsKey().matches(key, scanCode)) {
//                ClientGUI.openViewfinderControlsScreen();
//                return false;
//            }
//
//            if (action == 1 || action == 2) { // Press or Hold
//                if (key == InputConstants.KEY_ADD || key == InputConstants.KEY_EQUALS) {
//                    OldViewfinder.zoom(ZoomDirection.IN, false);
//                    return true;
//                }
//
//                if (key == 333 /*KEY_SUBTRACT*/ || key == InputConstants.KEY_MINUS) {
//                    OldViewfinder.zoom(ZoomDirection.OUT, false);
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
}
