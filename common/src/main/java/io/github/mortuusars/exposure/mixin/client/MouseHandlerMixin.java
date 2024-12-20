package io.github.mortuusars.exposure.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.exposure.camera.viewfinder.OldViewfinder;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
            cancellable = true)
    private void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci,
                  @Local(ordinal = 4 /* Magic number that corresponds to yScroll variable*/) double yScroll) {
        if (yScroll != 0 && MouseHandler.scrolled(yScroll)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;",
            ordinal = 0), cancellable = true)
    private void onScroll(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (MouseHandler.buttonPressed(button, action, modifiers))
            ci.cancel();
    }

    @ModifyVariable(method = "turnPlayer", at = @At(value = "STORE"), ordinal = 3)
    private double modifySensitivity(double sensitivity) {
        return MouseHandler.modifySensitivity(sensitivity);
    }
}
