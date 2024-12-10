package io.github.mortuusars.exposure.client.snapshot.processing;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import io.github.mortuusars.exposure.core.ChromaChannel;
import net.minecraft.util.FastColor;

public class SingleChannelBlackAndWhiteProcessor implements Processor {
    private final ChromaChannel channel;

    public SingleChannelBlackAndWhiteProcessor(ChromaChannel channel) {
        this.channel = channel;
    }

    @Override
    public Image apply(Image image) {
        return new ProcessedImage(image, this::modifyPixel);
    }

    public int modifyPixel(int colorARGB) {
        int color = switch (channel) {
            case RED -> FastColor.ARGB32.red(colorARGB);
            case GREEN -> FastColor.ARGB32.green(colorARGB);
            case BLUE -> FastColor.ARGB32.blue(colorARGB);
        };

        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorARGB), color, color, color);
    }

    @Override
    public String toString() {
        return "SingleChannelBlackAndWhiteProcessor{" +
                "channel=" + channel +
                '}';
    }
}
