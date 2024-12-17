package io.github.mortuusars.exposure.mixin;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinders;
import io.github.mortuusars.exposure.core.camera.NewCamera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public void setActiveCamera(@Nullable NewCamera camera) {
        super.setActiveCamera(camera);
        Viewfinders.setActiveCamera(camera);
    }

    @Override
    public void removeActiveCamera() {
        super.removeActiveCamera();
        Viewfinders.setActiveCamera(null);
    }
}
