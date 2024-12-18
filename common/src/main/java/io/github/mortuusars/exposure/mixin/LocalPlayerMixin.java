package io.github.mortuusars.exposure.mixin;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.client.CameraClient;
import io.github.mortuusars.exposure.core.camera.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

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
}
