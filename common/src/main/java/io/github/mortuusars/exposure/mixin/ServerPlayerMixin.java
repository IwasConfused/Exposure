package io.github.mortuusars.exposure.mixin;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.RemoveActiveCameraS2CP;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow @Nullable private Entity camera;

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null && !camera.isActive()) {
            removeActiveExposureCamera();
            Packets.sendToClient(RemoveActiveCameraS2CP.INSTANCE, ((ServerPlayer) (Object) this));
        }
    }
}
