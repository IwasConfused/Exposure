package io.github.mortuusars.exposure.client.snapshot.processing;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class BrightnessProcessor implements Processor {
    public float brightenPerStop = 0.3f;
    public float darkenPerStop = 0.3f;

    private final float brightnessStops;

    public BrightnessProcessor(float brightnessStops) {
        this.brightnessStops = brightnessStops;
    }

    @Override
    public Image apply(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    public int modifyPixel(int colorABGR) {
        float stopsDif = brightnessStops;
        if (stopsDif == 0f)
            return colorABGR;

        int alpha = FastColor.ABGR32.alpha(colorABGR);
        int blue = FastColor.ABGR32.blue(colorABGR);
        int green = FastColor.ABGR32.green(colorABGR);
        int red = FastColor.ABGR32.red(colorABGR);

        float brightness = 1f + (stopsDif * (stopsDif < 0 ? darkenPerStop : brightenPerStop));

        // We simulate bright light by not modifying all pixels equally
        float lightness = (blue + green + red) / 765f; // from 0.0 to 1.0
        float bias;
        if (stopsDif < 0)
            bias = (1f - lightness) * 0.8f + 0.2f;
        else {
            float curve = (float) Math.pow(Math.sin(lightness * Math.PI), 2);
            bias = lightness > 0.5f ? curve * 0.8f + 0.2f : curve * 0.5f + 0.5f;
        }

        float b = Mth.lerp(bias, blue, blue * brightness);
        float g = Mth.lerp(bias, green, green * brightness);
        float r = Mth.lerp(bias, red, red * brightness);

        // Above values are not clamped at 255 purposely.
        // Excess is redistributed to other channels. As a result - color gets less saturated, which gives more natural color.
        int[] rdst = redistribute(r, g, b);

        // BUT, it does not look perfect (idk, maybe because of dithering), so we blend them together.
        // This makes transitions smoother, subtler. Which looks good imo.
        return FastColor.ABGR32.color(alpha,
                Mth.clamp((int) ((b + rdst[2]) / 2), 0, 255),
                Mth.clamp((int) ((g + rdst[1]) / 2), 0, 255),
                Mth.clamp((int) ((r + rdst[0]) / 2), 0, 255));
    }

    /**
     * Redistributes excess (> 255) values to other channels.
     * Adapted from Mark Ransom's answer:
     * <a href="https://stackoverflow.com/a/141943">StackOverflow</a>
     */
    private int[] redistribute(float red, float green, float blue) {
        float threshold = 255.999f;
        float max = Math.max(red, Math.max(green, blue));
        if (max <= threshold) {
            return new int[]{
                    Mth.clamp(Math.round(red), 0, 255),
                    Mth.clamp(Math.round(green), 0, 255),
                    Mth.clamp(Math.round(blue), 0, 255)};
        }

        float total = red + green + blue;

        if (total >= 3 * threshold)
            return new int[]{(int) threshold, (int) threshold, (int) threshold};

        float x = (3f * threshold - total) / (3f * max - total);
        float gray = threshold - x * max;
        return new int[]{
                Mth.clamp(Math.round(gray + x * red), 0, 255),
                Mth.clamp(Math.round(gray + x * green), 0, 255),
                Mth.clamp(Math.round(gray + x * blue), 0, 255)};
    }

    @Override
    public String toString() {
        return "BrightnessProcessor[" +
                "brightnessStops=" + brightnessStops + ']';
    }
}
