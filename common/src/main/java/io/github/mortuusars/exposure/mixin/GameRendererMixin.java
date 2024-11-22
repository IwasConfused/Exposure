package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderShader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    void onRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ViewfinderShader.process();
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    void onResize(int width, int height, CallbackInfo ci) {
        ViewfinderShader.resize(width, height);
    }
}
