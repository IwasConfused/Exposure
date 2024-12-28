package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.client.image.PalettedImage;

public interface ImagePalettizer {
    ImagePalettizer NEAREST_MAP_COLORS = new NearestColorPalettizer();
    ImagePalettizer DITHERED_MAP_COLORS = new DitheredPalettizer();

    PalettedImage palettize(Image image, ColorPalette palette);

    static PalettedImage palettizeAndClose(Image image, ColorPalette palette, boolean dither) {
        ImagePalettizer palettizer = dither ? ImagePalettizer.DITHERED_MAP_COLORS : ImagePalettizer.NEAREST_MAP_COLORS;
        PalettedImage palettedImage = palettizer.palettize(image, palette);
        image.close();
        return palettedImage;
    }
}
