package io.github.mortuusars.exposure.client.image.modifier;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ModifiedImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class NegativeFilmModifier implements Modifier {
    @Override
    public Image modify(Image image) {
        return new ModifiedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "negative-film";
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

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }
}
