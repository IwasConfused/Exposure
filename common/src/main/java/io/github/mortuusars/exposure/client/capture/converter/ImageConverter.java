package io.github.mortuusars.exposure.client.capture.converter;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.processing.NearestColor;
import io.github.mortuusars.exposure.core.image.processing.Dithering;

@FunctionalInterface
public interface ImageConverter {
    ImageConverter NEAREST_MAP_COLORS = image -> NearestColor.convert(image, ColorPalette.MAP_COLORS);
    ImageConverter DITHERED_MAP_COLORS = image -> Dithering.ditherIndexed(image, ColorPalette.MAP_COLORS);

    byte[] convert(NativeImage image);
}
