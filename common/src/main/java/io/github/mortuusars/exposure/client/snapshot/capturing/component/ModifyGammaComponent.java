package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import io.github.mortuusars.exposure.client.render.GammaModifier;
import net.minecraft.client.Minecraft;

public class ModifyGammaComponent implements CaptureComponent {
    private final float offset;

    public ModifyGammaComponent(float brightnessStops, float gammaPerStop) {
        float currentGamma = Minecraft.getInstance().options.gamma().get().floatValue();
        //TODO: test and define magical numbers
        this.offset = brightnessStops != 0 ? (gammaPerStop * brightnessStops) * ((1f - currentGamma) * 0.65f + 0.35f) : 0;
    }

    public ModifyGammaComponent(float brightnessStops) {
        this(brightnessStops, 0.01f);
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
