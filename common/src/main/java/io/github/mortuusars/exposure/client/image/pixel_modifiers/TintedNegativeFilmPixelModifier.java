package io.github.mortuusars.exposure.client.image.pixel_modifiers;

import io.github.mortuusars.exposure.core.FilmColor;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class TintedNegativeFilmPixelModifier implements PixelModifier {
    private final FilmColor filmColor;

    public TintedNegativeFilmPixelModifier(FilmColor tintColor) {
        this.filmColor = tintColor;
    }

    @Override
    public String getIdentifier() {
        return "tinted_negative_film";
    }

    @Override
    public int modifyPixel(int ARGB) {
        int alpha = FastColor.ARGB32.alpha(ARGB);
        int red = FastColor.ARGB32.red(ARGB);
        int green = FastColor.ARGB32.green(ARGB);
        int blue = FastColor.ARGB32.blue(ARGB);

        // Modify opacity to make lighter colors transparent, like in real film.
        int brightness = (red + green + blue) / 3;
        int opacity = (int) Mth.clamp(brightness * 1.5f, 0, 255);
        alpha = (alpha * opacity) / 255;

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        // Tint
        red = (int) (red * filmColor.r() / 255);
        green = (int) (green * filmColor.g() / 255);
        blue = (int) (blue * filmColor.b() / 255);

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }
}
