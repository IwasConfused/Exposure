package io.github.mortuusars.exposure.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.exposure.camera.viewfinder.OldViewfinder;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerFabricMixin {
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
            cancellable = true)
    void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci,
                  @Local(ordinal = 4 /* Magic number that corresponds to yScroll variable*/) double yScroll) {
        if (yScroll != 0 && OldViewfinder.handleMouseScroll(yScroll)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;",
            ordinal = 0), cancellable = true)
    void onScroll(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (io.github.mortuusars.exposure.client.input.MouseHandler.handleMouseButtonPress(button, action, modifiers))
            ci.cancel();
    }
}
