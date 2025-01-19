package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionMode;

import java.lang.ref.Cleaner;
import java.util.function.Function;

public interface Palettizer {
    Palettizer NEAREST = new NearestColorPalettizer();
    Palettizer DITHERED = new DitheredPalettizer();

    PalettedImage palettize(Image image, ColorPalette palette);

    default PalettedImage palettizeAndClose(Image image, ColorPalette palette) {
        PalettedImage palettedImage = palettize(image, palette);
        image.close();
        return palettedImage;
    }

    default Function<Image, PalettedImage> palettizeAndClose(ColorPalette palette) {
        return image -> palettizeAndClose(image, palette);
    }

    static Function<Image, PalettedImage> palettizeAndClose(Palettizer palettizer, ColorPalette palette) {
        return image -> palettizer.palettizeAndClose(image, palette);
    }

    static Palettizer fromProjectionMode(ProjectionMode mode) {
        return mode == ProjectionMode.DITHERED
                ? DITHERED
                : NEAREST;
    }
}
