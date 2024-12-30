package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.render.FovModifier;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", ordinal = 1))
    private void renderViewfinder(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        if (CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().overlay().render();
        }
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    void onResize(int width, int height, CallbackInfo ci) {
        if (CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().shader().resize(width, height);
        }
    }

    @Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
    void getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (useFOVSetting) {
            cir.setReturnValue(FovModifier.modify(cir.getReturnValue()));
        }
    }
}
