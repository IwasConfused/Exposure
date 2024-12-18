package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.CameraClient;
import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderCameraControlsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;added()V"))
    void onSetScreen(Screen screen, CallbackInfo ci) {
        if (player != null && CameraClient.isActive() && !(screen instanceof ViewfinderCameraControlsScreen)) {
            CameraClient.deactivate();
        }
    }
}
