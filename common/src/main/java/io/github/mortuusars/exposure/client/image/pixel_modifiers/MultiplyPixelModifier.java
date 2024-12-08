package io.github.mortuusars.exposure.client.image.pixel_modifiers;

import net.minecraft.util.FastColor;

public class MultiplyPixelModifier implements PixelModifier {
    public final int multiplyColor;

    public MultiplyPixelModifier(int multiplyColor) {
        this.multiplyColor = multiplyColor;
    }

    @Override
    public int modifyPixel(int ARGB) {
        if (multiplyColor == 0)
            return ARGB;

        int alpha = FastColor.ABGR32.alpha(ARGB);
        int blue = FastColor.ABGR32.blue(ARGB);
        int green = FastColor.ABGR32.green(ARGB);
        int red = FastColor.ABGR32.red(ARGB);

        int tintAlpha = FastColor.ARGB32.alpha(ARGB);
        int tintBlue = FastColor.ARGB32.blue(ARGB);
        int tintGreen = FastColor.ARGB32.green(ARGB);
        int tintRed = FastColor.ARGB32.red(ARGB);

        alpha = Math.min(255, (alpha * tintAlpha) / 255);
        blue = Math.min(255, (blue * tintBlue) / 255);
        green = Math.min(255, (green * tintGreen) / 255);
        red = Math.min(255, (red * tintRed) / 255);

        return FastColor.ABGR32.color(alpha, blue, green, red);
    }

    @Override
    public String getIdentifier() {
        return multiplyColor != 0 ? "_tint" + Integer.toHexString(multiplyColor) : "";
    }
}
