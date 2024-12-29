package io.github.mortuusars.exposure.client.image.processor;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class BlackAndWhiteProcessor implements Processor {
    protected final float contrast;

    /**
     * @param contrast 1 means no change.
     */
    public BlackAndWhiteProcessor(float contrast) {
        Preconditions.checkArgument(contrast > 0f, "Contrast should be larger than 0.");
        this.contrast = contrast;
    }

    public BlackAndWhiteProcessor() {
        this(1f);
    }

    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "bw-" + contrast;
    }

    public int modifyPixel(int colorABGR) {
        int alpha = FastColor.ABGR32.alpha(colorABGR);
        int blue = FastColor.ABGR32.blue(colorABGR);
        int green = FastColor.ABGR32.green(colorABGR);
        int red = FastColor.ABGR32.red(colorABGR);

        // Weights adding up to more than 1 - to make the image slightly brighter and better emulate the look of BW photos
        int luma = Mth.clamp((int) (0.299 * red + 0.587 * green + 0.114 * blue), 0, 255);

        if (contrast != 1f) {
            int contrast = Math.round(127 * this.contrast);
            luma = Mth.clamp((luma - 127) * contrast / 127 + 127, 0, 255);
        }

        return FastColor.ABGR32.color(alpha, luma, luma, luma);
    }

    @Override
    public String toString() {
        return contrast == 1f ? "BlackAndWhiteProcessor{}" : "BlackAndWhiteProcessor{contrast=" + contrast + "}";
    }
}
