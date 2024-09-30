package io.github.mortuusars.exposure.camera.capture.component;

import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.util.ChromaticChannel;
import net.minecraft.util.FastColor;

@SuppressWarnings("ClassCanBeRecord")
public class SelectiveChannelBlackAndWhiteComponent implements ICaptureComponent {
    private final ChromaticChannel channel;

    public SelectiveChannelBlackAndWhiteComponent(ChromaticChannel channel) {
        this.channel = channel;
    }

    public ChromaticChannel getChannel() {
        return channel;
    }

    @Override
    public int modifyPixel(Capture capture, int colorABGR) {
        int alpha = FastColor.ABGR32.alpha(colorABGR);

        if (channel == ChromaticChannel.RED) {
            int red = FastColor.ABGR32.red(colorABGR);
            return FastColor.ABGR32.color(alpha, red, red, red);
        }
        if (channel == ChromaticChannel.GREEN) {
            int green = FastColor.ABGR32.green(colorABGR);
            return FastColor.ABGR32.color(alpha, green, green, green);
        } else {
            int blue = FastColor.ABGR32.blue(colorABGR);
            return FastColor.ABGR32.color(alpha, blue, blue, blue);
        }
    }
}
