package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.camera.viewfinder.OldViewfinder;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MouseHandler.class)
public abstract class ViewfinderSensitivityMixin {
    @ModifyVariable(method = "turnPlayer", at = @At(value = "STORE"), ordinal = 3)
    private double modifySensitivity(double sensitivity) {
        return OldViewfinder.modifyMouseSensitivity(sensitivity);
    }
}
