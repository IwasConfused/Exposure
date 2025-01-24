package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void move(float zoom, float dy, float dx);

    @Inject(method = "getMaxZoom", at = @At(value = "RETURN"), cancellable = true)
    private void getMaxZoom(float maxZoom, CallbackInfoReturnable<Float> cir) {
        if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
            cir.setReturnValue(Math.min(CameraClient.viewfinder().getMaxSelfieCameraDistance(), cir.getReturnValue()));
        }
    }

    @Inject(method = "setup", at = @At(value = "RETURN"))
    private void onSetup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (!detached && CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
            move(0, CameraClient.viewfinder().getCameraYOffset(), 0);
        }
    }
}
