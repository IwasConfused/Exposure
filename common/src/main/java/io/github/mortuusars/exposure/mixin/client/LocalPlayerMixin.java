package io.github.mortuusars.exposure.mixin.client;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.world.camera.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public void setActiveExposureCamera(Camera camera) {
        super.setActiveExposureCamera(camera);
        CameraClient.setupViewfinder(camera);
    }

    @Override
    public void removeActiveExposureCamera() {
        super.removeActiveExposureCamera();
        CameraClient.removeViewfinder();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        @Nullable Camera camera = activeExposureCamera();

        CameraClient.tick();

        if (camera != null && !camera.isActive()) {
            removeActiveExposureCamera();
        }
    }
}
