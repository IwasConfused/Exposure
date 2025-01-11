package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.render.GammaModifier;
import net.minecraft.client.Minecraft;

public class ModifyGammaAction implements CaptureAction {
    private final float offset;

    public ModifyGammaAction(float brightnessStops, float gammaPerStop) {
        float currentGamma = Minecraft.getInstance().options.gamma().get().floatValue();
        float strength = (1f - currentGamma) * 0.65f + 0.35f;
        this.offset = (gammaPerStop * brightnessStops) * strength;
    }

    public ModifyGammaAction(float brightnessStops) {
        this(brightnessStops, 0.02f);
    }

    @Override
    public void beforeCapture() {
        GammaModifier.apply(offset);
    }

    @Override
    public void afterCapture() {
        GammaModifier.restore();
    }
}
