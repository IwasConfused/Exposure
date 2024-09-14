package io.github.mortuusars.exposure.neoforge.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ToggleKeyMapping.class)
public abstract class ToggleKeyMappingNeoForgeMixin extends KeyMapping {
    public ToggleKeyMappingNeoForgeMixin(String pName, int pKeyCode, String pCategory) {
        super(pName, pKeyCode, pCategory);
    }

//    /**
//     * Allows moving when ControlsScreen is open.
//     */
//    @Inject(method = "isDown", at = @At(value = "HEAD"), cancellable = true)
//    private void isDown(CallbackInfoReturnable<Boolean> cir) {
//        if (Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)
//            cir.setReturnValue(this.isDown);
//    }
}