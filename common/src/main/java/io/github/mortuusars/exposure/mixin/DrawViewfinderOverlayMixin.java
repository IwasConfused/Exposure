package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class DrawViewfinderOverlayMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;", ordinal = 1))
    private void renderViewfinder(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        if (Viewfinder.isLookingThrough())
            ViewfinderOverlay.render();
    }
}
