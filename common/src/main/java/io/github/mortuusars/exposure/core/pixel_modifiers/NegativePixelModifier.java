package io.github.mortuusars.exposure.core.pixel_modifiers;

import net.minecraft.util.FastColor;

public class NegativePixelModifier implements PixelModifier {
    @Override
    public String getIdSuffix() {
        return "_negative";
    }

    @Override
    public int modifyPixel(int ABGR) {
        int alpha = FastColor.ABGR32.alpha(ABGR);
        int blue = FastColor.ABGR32.blue(ABGR);
        int green = FastColor.ABGR32.green(ABGR);
        int red = FastColor.ABGR32.red(ABGR);

        // Invert
        blue = 255 - blue;
        green = 255 - green;
        red = 255 - red;

        return FastColor.ABGR32.color(alpha, blue, green, red);
    }
}
