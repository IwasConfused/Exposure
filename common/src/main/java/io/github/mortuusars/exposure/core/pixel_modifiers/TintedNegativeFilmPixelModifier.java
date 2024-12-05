package io.github.mortuusars.exposure.core.pixel_modifiers;

import io.github.mortuusars.exposure.core.FilmColor;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class TintedNegativeFilmPixelModifier implements PixelModifier {
    private final FilmColor filmColor;

    public TintedNegativeFilmPixelModifier(FilmColor tintColor) {
        this.filmColor = tintColor;
    }

    @Override
    public String getIdSuffix() {
        return "_tinted_negative_film";
    }

    @Override
    public int modifyPixel(int ARGB) {
        int alpha = FastColor.ABGR32.alpha(ARGB);
        int blue = FastColor.ABGR32.blue(ARGB);
        int green = FastColor.ABGR32.green(ARGB);
        int red = FastColor.ABGR32.red(ARGB);

        // Modify opacity to make lighter colors transparent, like in real film.
        int brightness = (blue + green + red) / 3;
        int opacity = (int) Mth.clamp(brightness * 1.5f, 0, 255);
        alpha = (alpha * opacity) / 255;

        // Invert
        blue = 255 - blue;
        green = 255 - green;
        red = 255 - red;

        // Tint
        blue = (int) (blue * filmColor.b() / 255);
        green = (int) (green * filmColor.g() / 255);
        red = (int) (red * filmColor.r() / 255);

        return FastColor.ABGR32.color(alpha, blue, green, red);
    }
}
