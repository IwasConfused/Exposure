package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class ServerEvents {
    public static void onServerSave() {

    }

    public static void serverStarted(MinecraftServer server) {
        Exposure.initServer(server);
    }

    public static void serverStopped(MinecraftServer server) {

    }

    public static void syncDatapack(Stream<ServerPlayer> relevantPlayers) {

    }

    public static void playerTick(ServerPlayer player) {
        if (player.getActiveExposureCamera() instanceof CameraInHand cameraInHand) {
            if (!cameraInHand.getId().matches(player.getItemInHand(cameraInHand.getHand()))) {
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() instanceof CameraItem cameraItem && !cameraInHand.getId().matches(stack)) {
                        cameraItem.deactivate(player, stack);
                    }
                }
                player.removeActiveExposureCamera();
            }
        }
    }

    public static void itemDrop(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        ItemStack droppedItem = inventory.getSelected();

        if (droppedItem.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(droppedItem)) {
            player.getActiveExposureCameraOptional().ifPresentOrElse(
                    camera -> {
                        player.removeActiveExposureCamera();
                        cameraItem.deactivate(player, droppedItem);
                    },
                    () -> cameraItem.setActive(droppedItem, false));
        }
    }
}
