package io.github.mortuusars.exposure.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.render.FovModifier;
import io.github.mortuusars.exposure.event.ClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 500)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;", shift = At.Shift.AFTER))
    void onRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        // Processing viewfinder shader should be done before capturing
        // otherwise Direct capture method will not be affected by it.
        if (CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().shader().process();
        }

        ExposureClient.cycles().tick();
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    void onResize(int width, int height, CallbackInfo ci) {
        if (CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().shader().resize(width, height);
        }
    }

    @ModifyReturnValue(method = "getFov", at = @At(value = "RETURN", ordinal = 1))
    private double modifyFov(double original, @Local(argsOnly = true) boolean useFOVSetting) {
        return useFOVSetting ? FovModifier.modify(original) : original;
    }

    @Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
    void getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (useFOVSetting && FovModifier.shouldOverride()) {
            cir.setReturnValue(FovModifier.modify(cir.getReturnValue()));
        }
    }

    @Inject(method = "resetData", at = @At(value = "RETURN"))
    void onResetData(CallbackInfo ci) {
        ClientEvents.resetRenderData();
    }
}
