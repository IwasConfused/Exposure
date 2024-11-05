package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Config;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PotionContents.class)
public abstract class PotionContentsMixin {
    @Shadow public abstract Optional<Holder<Potion>> potion();

    @Inject(method = "getColor()I", at = @At("RETURN"), cancellable = true)
    private void onGetColor(CallbackInfoReturnable<Integer> cir) {
        if (!Config.Client.DIFFERENT_DEVELOPING_POTION_COLORS.get() || cir.getReturnValue() != 0xFF385DC6) { // Default color
            return;
        }

        potion().ifPresent(potionHolder -> {
            if (potionHolder.value().equals(Potions.MUNDANE.value())) {
                cir.setReturnValue(0xFF424D8F);
                return;
            }

            if (potionHolder.value().equals(Potions.AWKWARD.value())) {
                cir.setReturnValue(0xFF653594);
                return;
            }

            if (potionHolder.value().equals(Potions.THICK.value())) {
                cir.setReturnValue(0xFF3E7782);
                return;
            }
        });
    }
}
