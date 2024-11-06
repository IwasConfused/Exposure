package io.github.mortuusars.exposure.core.pixel_modifiers;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class NegativeFilmPixelModifier implements PixelModifier {
    @Override
    public String getIdSuffix() {
        return "_negative_film";
    }

    @Override
    public int modifyPixel(int ABGR) {
        int alpha = FastColor.ABGR32.alpha(ABGR);
        int blue = FastColor.ABGR32.blue(ABGR);
        int green = FastColor.ABGR32.green(ABGR);
        int red = FastColor.ABGR32.red(ABGR);

        // Modify opacity to make lighter colors transparent, like in real film.
        int brightness = (blue + green + red) / 3;
        int opacity = (int) Mth.clamp(brightness * 1.5f, 0, 255);
        alpha = (alpha * opacity) / 255;

        // Invert
        blue = 255 - blue;
        green = 255 - green;
        red = 255 - red;

        return FastColor.ABGR32.color(alpha, blue, green, red);
    }
}
