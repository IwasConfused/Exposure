package io.github.mortuusars.exposure.client.image.processor;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class ContrastProcessor implements Processor {
    protected final float contrast;

    /**
     * @param contrast 1 means no change.
     */
    public ContrastProcessor(float contrast) {
        Preconditions.checkArgument(contrast > 0f, "Contrast should be larger than 0.");
        this.contrast = contrast;
    }

    public ContrastProcessor() {
        this(1f);
    }

    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "contrast-" + contrast;
    }

    public int modifyPixel(int colorARGB) {
        if (contrast != 1f) {
            int alpha = FastColor.ARGB32.alpha(colorARGB);
            int red = FastColor.ARGB32.red(colorARGB);
            int green = FastColor.ARGB32.green(colorARGB);
            int blue = FastColor.ARGB32.blue(colorARGB);

            int contrastValue = Math.round(127 * contrast);
            red = Mth.clamp((red - 127) * contrastValue / 127 + 127, 0, 255);
            green = Mth.clamp((green - 127) * contrastValue / 127 + 127, 0, 255);
            blue = Mth.clamp((blue - 127) * contrastValue / 127 + 127, 0, 255);

            return FastColor.ARGB32.color(alpha, red, green, blue);
        }

        return colorARGB;
    }

    @Override
    public String toString() {
        return "Contrast{contrast=" + contrast + "}";
    }
}
