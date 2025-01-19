package io.github.mortuusars.exposure.client.image.modifier;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ModifiedImage;
import net.minecraft.util.FastColor;

public class NegativeModifier implements Modifier {
    @Override
    public Image modify(Image image) {
        return new ModifiedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "negative";
    }

    public int modifyPixel(int ARGB) {
        int alpha = FastColor.ARGB32.alpha(ARGB);
        int red = FastColor.ARGB32.red(ARGB);
        int green = FastColor.ARGB32.green(ARGB);
        int blue = FastColor.ARGB32.blue(ARGB);

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }
}
