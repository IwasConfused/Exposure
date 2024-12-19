package io.github.mortuusars.exposure.mixin;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow @Nullable private Entity camera;

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "drop(Z)Z", at = @At(value = "HEAD"))
    void onDrop(boolean dropStack, CallbackInfoReturnable<Boolean> cir) {
        Inventory inventory = this.getInventory();
        ItemStack droppedItem = inventory.getSelected();

        if (droppedItem.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(droppedItem)) {
            @Nullable Camera activeCamera = activeExposureCamera();
            if (activeCamera == null) {
                cameraItem.setActive(droppedItem, false);
            } else if (activeCamera.getItemStack().equals(droppedItem)) {
                cameraItem.deactivate(this, droppedItem);
            }
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null && !camera.isActive()) {
            for (ItemStack stack : getInventory().items) {
                if (stack.getItem() instanceof CameraItem cameraItem && camera.idMatches(cameraItem.getOrCreateID(stack))) {
                    cameraItem.deactivate(camera.getPhotographer(), stack);
                }
            }
            removeActiveExposureCamera();
        }
    }
}
