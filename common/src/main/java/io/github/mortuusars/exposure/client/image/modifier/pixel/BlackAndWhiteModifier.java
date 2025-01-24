package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class BlackAndWhiteModifier implements PixelModifier {
    protected final float rWeight;
    protected final float gWeight;
    protected final float bWeight;
    protected String identifier;

    public BlackAndWhiteModifier(float rWeight, float gWeight, float bWeight) {
        this.rWeight = rWeight;
        this.gWeight = gWeight;
        this.bWeight = bWeight;
        this.identifier = "bw-" + rWeight + "-" + gWeight + "-" + bWeight;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public int modify(int colorARGB) {
        int alpha = FastColor.ARGB32.alpha(colorARGB);
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);

        int value = Mth.clamp((int) (rWeight * red + gWeight * green + bWeight * blue), 0, 255);
        return FastColor.ARGB32.color(alpha, value, value, value);
    }

    @Override
    public String toString() {
        return "BlackAndWhiteProcessor{weights:%s,%s,%s}".formatted(rWeight, gWeight, bWeight);
    }
}
