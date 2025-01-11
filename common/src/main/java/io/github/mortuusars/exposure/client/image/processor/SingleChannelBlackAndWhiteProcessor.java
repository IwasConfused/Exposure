package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import io.github.mortuusars.exposure.core.color.ChromaChannel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class SingleChannelBlackAndWhiteProcessor implements Processor {
    private final ChromaChannel channel;

    public SingleChannelBlackAndWhiteProcessor(ChromaChannel channel) {
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

        int avg = (r + g + b) / 3;

        int val = Mth.lerpInt(0.9f, avg, color);

        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorARGB), val, val, val);

//        int color = switch (channel) {
//            case RED -> FastColor.ARGB32.red(colorARGB);
//            case GREEN -> FastColor.ARGB32.green(colorARGB);
//            case BLUE -> FastColor.ARGB32.blue(colorARGB);
//        };
//
//        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorARGB), color, color, color);
    }

    @Override
    public String toString() {
        return "SingleChannelBlackAndWhiteProcessor{channel=" + channel + '}';
    }
}
