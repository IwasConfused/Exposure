package io.github.mortuusars.exposure.core.image.color;

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
        assertEquals(color.getRF(), 0.498f, 0.01f);
        assertEquals(color.getGF(), 0.498f, 0.01f);
        assertEquals(color.getBF(), 0.498f, 0.01f);
    }
}
