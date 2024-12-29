package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import net.minecraft.util.FastColor;

public class NegativeProcessor implements Processor {
    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
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
