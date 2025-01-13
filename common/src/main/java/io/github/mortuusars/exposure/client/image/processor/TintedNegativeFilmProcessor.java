package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class TintedNegativeFilmProcessor implements Processor {
    private final FilmColor tintColor;

    public TintedNegativeFilmProcessor(FilmColor tintColor) {
        this.tintColor = tintColor;
    }

    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "tinted-negative-film";
    }

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
        red = (int) (red * tintColor.r() / 255);
        green = (int) (green * tintColor.g() / 255);
        blue = (int) (blue * tintColor.b() / 255);

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }
}
