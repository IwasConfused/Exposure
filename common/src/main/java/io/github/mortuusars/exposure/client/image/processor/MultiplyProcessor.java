package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import net.minecraft.util.FastColor;

public class MultiplyProcessor implements Processor {
    protected final int multiplyColor;

    public MultiplyProcessor(int multiplyColor) {
        this.multiplyColor = multiplyColor;
    }

    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return multiplyColor != 0 ? "tint-" + Integer.toHexString(multiplyColor) : "";
    }

    public int modifyPixel(int colorARGB) {
        if (multiplyColor == 0)
            return colorARGB;

        int alpha = FastColor.ARGB32.alpha(colorARGB);
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);

        int tintAlpha = FastColor.ARGB32.alpha(colorARGB);
        int tintRed = FastColor.ARGB32.red(colorARGB);
        int tintGreen = FastColor.ARGB32.green(colorARGB);
        int tintBlue = FastColor.ARGB32.blue(colorARGB);

        alpha = Math.min(255, (alpha * tintAlpha) / 255);
        red = Math.min(255, (red * tintRed) / 255);
        green = Math.min(255, (green * tintGreen) / 255);
        blue = Math.min(255, (blue * tintBlue) / 255);

        return FastColor.ARGB32.color(alpha, green, red, blue);
    }
}
