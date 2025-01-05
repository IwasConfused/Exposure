package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "getMaxZoom", at = @At(value = "RETURN"), cancellable = true)
    private void getMaxZoom(float maxZoom, CallbackInfoReturnable<Float> cir) {
        if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
            cir.setReturnValue(Math.min(CameraClient.viewfinder().getMaxSelfieCameraDistance(), cir.getReturnValue()));
        }
    }
}
