package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.camera.viewfinder.OldViewfinder;
import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinders;
import io.github.mortuusars.exposure.client.snapshot.SnapShot;
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
        // Processing viewfinder shader should be done before capturing with SnapShot
        // because Direct capture method will not be affected by it otherwise.
        Viewfinders.processShader();
//        OldViewfinderShader.process();
        SnapShot.tick();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", ordinal = 1))
    private void renderViewfinder(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Viewfinders.render();
//        if (OldViewfinder.isLookingThrough())
//            OldViewfinderOverlay.render();
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    void onResize(int width, int height, CallbackInfo ci) {
        Viewfinders.getActive().flatMap(Viewfinder::getShader).ifPresent(shader -> shader.resize(width, height));
//        OldViewfinderShader.resize(width, height);
    }

    @Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
    void getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (!useFOVSetting)
            return;

        double prevFov = cir.getReturnValue();
        double modifiedFov = OldViewfinder.modifyFov(prevFov);
        if (prevFov != modifiedFov)
            cir.setReturnValue(modifiedFov);
    }
}
