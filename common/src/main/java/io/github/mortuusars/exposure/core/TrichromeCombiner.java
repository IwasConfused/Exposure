package io.github.mortuusars.exposure.core;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.capture.converter.ImageConverter;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.warehouse.PalettedImage;
import net.minecraft.util.FastColor;
import org.slf4j.Logger;

public class TrichromeCombiner {
    private static final Logger LOGGER = LogUtils.getLogger();

    //TODO: properties
    public static PalettedImage create(Image red, Image green, Image blue) {
        int width = Math.min(red.getWidth(), Math.min(green.getWidth(), blue.getWidth()));
        int height = Math.min(red.getHeight(), Math.min(green.getHeight(), blue.getHeight()));
        if (width <= 0 || height <= 0) {
            LOGGER.error("Cannot create Chromatic Photograph: Width and Height should be larger than 0. " +
                    "Width '{}', Height: '{}'.", width, height);
            return PalettedImage.EMPTY;
        }

        try (NativeImage image = new NativeImage(width, height, false)) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    image.setPixelRGBA(x, y, FastColor.ABGR32.color(
                            FastColor.ABGR32.alpha(red.getPixelARGB(x, y)),
                            FastColor.ABGR32.blue(blue.getPixelARGB(x, y)),
                            FastColor.ABGR32.green(green.getPixelARGB(x, y)),
                            FastColor.ABGR32.red(red.getPixelARGB(x, y))
                    ));
                }
            }

            return ImageConverter.DITHERED_MAP_COLORS.convert(new WrappedNativeImage(image));
        }
    }
}
