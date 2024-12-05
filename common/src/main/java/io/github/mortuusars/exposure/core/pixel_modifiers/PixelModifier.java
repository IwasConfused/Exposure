package io.github.mortuusars.exposure.core.pixel_modifiers;

import io.github.mortuusars.exposure.core.ExposureType;

public interface PixelModifier {
    PixelModifier EMPTY = new PixelModifier() {
        @Override
        public String getIdSuffix() {
            return "";
        }
    };

    PixelModifier NEGATIVE = new NegativePixelModifier();
    PixelModifier NEGATIVE_FILM = new NegativeFilmPixelModifier();
    PixelModifier TINTED_NEGATIVE_COLOR_FILM = new TintedNegativeFilmPixelModifier(ExposureType.COLOR.getFilmColor());
    PixelModifier TINTED_NEGATIVE_BW_FILM = new TintedNegativeFilmPixelModifier(ExposureType.BLACK_AND_WHITE.getFilmColor());

    // HSB is faster while giving only slightly worse result. HSLUV is slower and creates noticeable freezes when exposure is loaded.
    PixelModifier AGED = new AgedHSBPixelModifier(0xD9A863, 0.65f, 40, 255);
    PixelModifier AGED_HSLUV = new AgedHSLUVPixelModifier(0xD9A863, 0.65f, 40, 255);

    /**
     * Suffix is used to differentiate between cached rendered exposures.
     */
    String getIdSuffix();
    default int modifyPixel(int ARGB) {
        return ARGB;
    }
}
