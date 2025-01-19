package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class BlackAndWhiteProcessor implements Processor {
    private final float rWeight;
    private final float gWeight;
    private final float bWeight;

    public BlackAndWhiteProcessor(float rWeight, float gWeight, float bWeight) {
        this.rWeight = rWeight;
        this.gWeight = gWeight;
        this.bWeight = bWeight;
    }

    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "bw-" + rWeight + "-" + gWeight + "-" + bWeight;
    }

    public int modifyPixel(int colorARGB) {
        int alpha = FastColor.ARGB32.alpha(colorARGB);
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);

        int value = Mth.clamp((int) (rWeight * red + gWeight * green + bWeight * blue), 0, 255);
        return FastColor.ARGB32.color(alpha, value, value, value);
    }

    @Override
    public String toString() {
        return "BlackAndWhiteProcessor{" +
                "rWeight=" + rWeight +
                ", gWeight=" + gWeight +
                ", bWeight=" + bWeight +
                '}';
    }
}
