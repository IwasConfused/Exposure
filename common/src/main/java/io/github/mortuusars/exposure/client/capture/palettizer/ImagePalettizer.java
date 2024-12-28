package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.client.image.PalettizedImage;

public interface ImagePalettizer {
    ImagePalettizer NEAREST_MAP_COLORS = new NearestColorPalettizer();
    ImagePalettizer DITHERED_MAP_COLORS = new DitheredPalettizer();

    PalettizedImage palettize(Image image, ColorPalette palette);
}
