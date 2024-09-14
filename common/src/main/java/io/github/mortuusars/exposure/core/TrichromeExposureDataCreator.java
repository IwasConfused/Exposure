package io.github.mortuusars.exposure.core;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringColorConverter;
import io.github.mortuusars.exposure.core.image.IImage;
import io.github.mortuusars.exposure.warehouse.ImageData;
import net.minecraft.util.FastColor;
import org.slf4j.Logger;

public class TrichromeExposureDataCreator {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ImageData create(IImage red, IImage green, IImage blue, String creator) {
        int width = Math.min(red.getWidth(), Math.min(green.getWidth(), blue.getWidth()));
        int height = Math.min(red.getHeight(), Math.min(green.getHeight(), blue.getHeight()));
        if (width <= 0 ||height <= 0) {
            LOGGER.error("Cannot create Chromatic Photograph: Width and Height should be larger than 0. " +
                    "Width '{}', Height: '{}'.", width, height);
            return ImageData.EMPTY;
        }

        int[][] pixels = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = FastColor.ABGR32.alpha(red.getPixelABGR(x, y));
                int b = FastColor.ABGR32.blue(blue.getPixelABGR(x, y));
                int g = FastColor.ABGR32.green(green.getPixelABGR(x, y));
                int r = FastColor.ABGR32.red(red.getPixelABGR(x, y));

                //TODO: test abgr insanity
                int abgr = FastColor.ARGB32.color(a, r, g, b);

                pixels[y][x] = abgr;
            }
        }

        byte[] mapColorPixels = new DitheringColorConverter().convert(pixels);

        return new ImageData(width, height, mapColorPixels);
    }
}
