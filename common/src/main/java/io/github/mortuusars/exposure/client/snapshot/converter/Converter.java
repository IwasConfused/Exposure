package io.github.mortuusars.exposure.client.snapshot.converter;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.processing.Dithering;
import io.github.mortuusars.exposure.core.image.processing.NearestColor;
import io.github.mortuusars.exposure.warehouse.PalettedImage;

@FunctionalInterface
public interface Converter {
    Converter NEAREST_MAP_COLORS = image -> NearestColor.convert(image, ColorPalette.MAP_COLORS);
    Converter DITHERED_MAP_COLORS = image -> Dithering.ditherIndexed(image, ColorPalette.MAP_COLORS);

    PalettedImage convert(Image image);

    static NativeImage convertBack(PalettedImage palettedImage) {
        NativeImage image = new NativeImage(palettedImage.width(), palettedImage.height(), false);

        for (int y = 0; y < palettedImage.height(); y++) {
            for (int x = 0; x < palettedImage.width(); x++) {
                int pixelColor = palettedImage.getPixelARGB(x, y).getRGB();
                image.setPixelRGBA(x, y, pixelColor);
            }
        }

        return image;
    }
}
