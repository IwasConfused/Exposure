package io.github.mortuusars.exposure.core.pixel_modifiers;

import net.minecraft.util.FastColor;

public class NegativePixelModifier implements PixelModifier {
    @Override
    public String getIdSuffix() {
        return "_negative";
    }

    @Override
    public int modifyPixel(int ARGB) {
        int alpha = FastColor.ABGR32.alpha(ARGB);
        int blue = FastColor.ABGR32.blue(ARGB);
        int green = FastColor.ABGR32.green(ARGB);
        int red = FastColor.ABGR32.red(ARGB);

        // Invert
        blue = 255 - blue;
        green = 255 - green;
        red = 255 - red;

        return FastColor.ABGR32.color(alpha, blue, green, red);
    }
}
