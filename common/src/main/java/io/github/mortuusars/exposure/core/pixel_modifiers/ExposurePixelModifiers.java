package io.github.mortuusars.exposure.core.pixel_modifiers;

import io.github.mortuusars.exposure.core.ExposureType;

public class ExposurePixelModifiers {
    public static final IPixelModifier EMPTY = new IPixelModifier() {
        @Override
        public String getIdSuffix() {
            return "";
        }
    };

    public static final IPixelModifier NEGATIVE = new NegativePixelModifier();
    public static final IPixelModifier NEGATIVE_FILM = new NegativeFilmPixelModifier();
    public static final IPixelModifier TINTED_NEGATIVE_COLOR_FILM = new TintedNegativeFilmPixelModifier(ExposureType.COLOR.getFilmColor());
    public static final IPixelModifier TINTED_NEGATIVE_BW_FILM = new TintedNegativeFilmPixelModifier(ExposureType.BLACK_AND_WHITE.getFilmColor());

    // HSB is faster while giving only slightly worse result. HSLUV is slower and creates noticeable freezes when exposure is loaded.
    public static final IPixelModifier AGED = new AgedHSBPixelModifier(0xD9A863, 0.65f, 40, 255);
}
