package io.github.mortuusars.exposure.core.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorPaletteTests {
    @Test
    void smallerPaletteIsCorrectedProperly() {
        ColorPalette palette = new ColorPalette(new int[]{0xFF112233, 0xFF223344});
        assertEquals(256, palette.getColors().length);
        assertEquals(0x00000000, palette.getColors()[255]);
        assertEquals(0xFF000000, palette.getColors()[2]);
        assertEquals(0xFF000000, palette.getColors()[24]);
        assertEquals(0xFF000000, palette.getColors()[254]);
    }

    @Test
    void byIdReturnsCorrectColor() {
        ColorPalette palette = new ColorPalette(new int[]{0xFF112233, 0xFF223344});
        assertEquals(0xFF112233, palette.byId(0));
        assertEquals(0xFF223344, palette.byId(1));
        assertEquals(0x00000000, palette.byId(255));
        assertEquals(0xFF000000, palette.byId(2));
        assertEquals(0xFF000000, palette.byId(24));
        assertEquals(0xFF000000, palette.byId(254));
    }
}
