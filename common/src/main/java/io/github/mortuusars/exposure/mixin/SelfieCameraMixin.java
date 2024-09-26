package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class SelfieCameraMixin {
    @Inject(method = "getMaxZoom", at = @At(value = "RETURN"), cancellable = true)
    private void getMaxZoom(float maxZoom, CallbackInfoReturnable<Float> cir) {
        if (Viewfinder.isLookingThrough())
            cir.setReturnValue(Math.min(Viewfinder.getSelfieCameraDistance(), cir.getReturnValue()));
    }
}
