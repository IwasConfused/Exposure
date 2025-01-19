package io.github.mortuusars.exposure.client.image.modifier;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ModifiedImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

public class NoiseModifier implements Modifier {
    protected final float intensity;

    public NoiseModifier(float intensity) {
        Preconditions.checkArgument(intensity >= 0 && intensity <= 1, "intensity should be in 0-1 range.");
        this.intensity = intensity;
    }

    @Override
    public Image modify(Image image) {
        return new ModifiedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "noise-" + intensity;
    }

    public int modifyPixel(int colorARGB) {
        int alpha = FastColor.ARGB32.alpha(colorARGB);
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);

        red = addNoise(red);
        green = addNoise(green);
        blue = addNoise(blue);

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }

    protected int addNoise(int colorValue) {
        // Apply noise based on intensity
        float effect = intensity * (1f - (colorValue / 255f));
        effect = Mth.lerp(0.4f, effect, intensity);
        int noise = (int) ((ThreadLocalRandom.current().nextDouble() * 2 - 1) * 255 * effect);
        return Mth.clamp(colorValue + noise, 0, 255);
    }
}
