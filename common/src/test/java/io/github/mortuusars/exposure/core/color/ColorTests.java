package io.github.mortuusars.exposure.core.color;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorTests {
    @Test
    void ARGBtoABGR() {
        assertEquals(Color.argb(0x992233AA).getABGR(), 0x99AA3322);
        assertEquals(Color.ARGBtoABGR(0x992233AA), 0x99AA3322);
    }

    @Test
    void floats() {
        Color color = Color.rgb(0xFFFFFF).multiply(0.5);
        assertEquals(0.498f, color.getRF(), 0.01f);
        assertEquals(0.498f, color.getGF(), 0.01f);
        assertEquals(0.498f, color.getBF(), 0.01f);
    }

    @Test
    void hexParsesCorrectly() {
        Color white = Color.fromHex("FFFFFFFF");
        assertEquals(255, white.getA());
        assertEquals(255, white.getR());
        assertEquals(255, white.getG());
        assertEquals(255, white.getB());

        Color black = Color.fromHex("00000000");
        assertEquals(0, black.getA());
        assertEquals(0, black.getR());
        assertEquals(0, black.getG());
        assertEquals(0, black.getB());

        Color random = Color.fromHex("7FC2C351");
        assertEquals(127, random.getA());
        assertEquals(194, random.getR());
        assertEquals(195, random.getG());
        assertEquals(81, random.getB());
    }
}
