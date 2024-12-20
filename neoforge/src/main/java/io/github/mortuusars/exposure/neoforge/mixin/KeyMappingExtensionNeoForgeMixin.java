package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IKeyMappingExtension.class)
public interface KeyMappingExtensionNeoForgeMixin {
    @Inject(method = "isConflictContextAndModifierActive", at = @At("HEAD"), cancellable = true)
    private void modify(CallbackInfoReturnable<Boolean> cir) {
        if (CameraClient.viewfinder() != null
                && CameraClient.viewfinder().getControlsScreen().map(screen -> screen == Minecrft.get().screen).orElse(false)) {
            cir.setReturnValue(true);
        }
    }
}