package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class NegativeFilmModifier implements PixelModifier {
    @Override
    public String getIdentifier() {
        return "negative-film";
    }

    public int modify(int ARGB) {
        int alpha = FastColor.ARGB32.alpha(ARGB);
        int red = FastColor.ARGB32.red(ARGB);
        int green = FastColor.ARGB32.green(ARGB);
        int blue = FastColor.ARGB32.blue(ARGB);

        // Modify opacity to make lighter colors transparent, like in real film.
        int lightness = (red + green + blue) / 3;
        int opacity = (int) Mth.clamp(lightness * 1.5f, 0, 255);
        alpha = (alpha * opacity) / 255;

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }
}
