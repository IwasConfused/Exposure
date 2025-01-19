package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class SingleChannelBlackAndWhiteProcessor implements Processor {
    private final ColorChannel channel;

    public SingleChannelBlackAndWhiteProcessor(ColorChannel channel) {
        this.channel = channel;
    }

    @Override
    public Image process(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    @Override
    public String getIdentifier() {
        return "bw-" + channel.getSerializedName();
    }

    public int modifyPixel(int colorARGB) {
        int r = FastColor.ARGB32.red(colorARGB);
        int g = FastColor.ARGB32.green(colorARGB);
        int b = FastColor.ARGB32.blue(colorARGB);

        int color = switch (channel) {
            case RED -> FastColor.ARGB32.red(colorARGB);
            case GREEN -> FastColor.ARGB32.green(colorARGB);
            case BLUE -> FastColor.ARGB32.blue(colorARGB);
        };

        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorARGB),
                Mth.lerpInt(1f, r, color),
                Mth.lerpInt(1f, g, color),
                Mth.lerpInt(1f, b, color));
    }

    @Override
    public String toString() {
        return "SingleChannelBlackAndWhiteProcessor{channel=" + channel + '}';
    }
}
