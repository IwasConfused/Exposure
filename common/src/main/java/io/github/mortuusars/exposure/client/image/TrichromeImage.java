package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;
import net.minecraft.util.FastColor;

public class TrichromeImage implements Image {
    private final Image red;
    private final Image green;
    private final Image blue;
    private final int width, height;

    public TrichromeImage(Image red, Image green, Image blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.width = Math.min(red.getWidth(), Math.min(green.getWidth(), blue.getWidth()));
        this.height = Math.min(red.getHeight(), Math.min(green.getHeight(), blue.getHeight()));
        Preconditions.checkArgument(this.width > 0,
                "Cannot create TrichromeImage: " +
                        "smallest image should have width larger than 0. {%s, %s, %s}", red, green, blue);
        Preconditions.checkArgument(this.height > 0,
                "Cannot create TrichromeImage: " +
                        "smallest image should have height larger than 0. {%s, %s, %s}", red, green, blue);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return FastColor.ABGR32.color(
                FastColor.ABGR32.alpha(red.getPixelARGB(x, y)),
                FastColor.ABGR32.red(red.getPixelARGB(x, y)),
                FastColor.ABGR32.green(green.getPixelARGB(x, y)),
                FastColor.ABGR32.blue(blue.getPixelARGB(x, y)));
    }

    public static TrichromeImage withSize(Image red, Image green, Image blue, int width, int height) {
        return new TrichromeImage(new ResizedImage(red, width, height),
                new ResizedImage(green, width, height),
                new ResizedImage(blue, width, height));
    }
}
