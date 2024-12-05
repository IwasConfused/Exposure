package io.github.mortuusars.exposure.core.image.color;

import net.minecraft.util.Mth;

public class Color {
    public static final Color WHITE = new Color(255, 255, 255, 255);

    protected final int r, g, b, a;
    protected final int value;

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.value = ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                ((b & 0xFF));
    }

    public Color(int rgb) {
        this(rgb, false);
    }

    public Color(int rgba, boolean hasAlpha) {
        if (hasAlpha) {
            this.value = rgba;
        } else {
            this.value = 0xff000000 | rgba;
        }

        this.a = hasAlpha ? (rgba >> 24) & 0xFF : 255;
        this.r = (rgba >> 16) & 0xFF;
        this.g = (rgba >> 8) & 0xFF;
        this.b = rgba & 0xFF;
    }

    public Color withAlpha(int alpha) {
        return new Color(this.r, this.g, this.b, alpha);
    }

    public void validate(int r, int g, int b, int a) {
        boolean rangeError = false;
        String badComponentString = "";

        if (a < 0 || a > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Alpha";
        }
        if (r < 0 || r > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Red";
        }
        if (g < 0 || g > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Green";
        }
        if (b < 0 || b > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Blue";
        }
        if (rangeError) {
            throw new IllegalArgumentException("Color parameter outside of expected range:"
                    + badComponentString);
        }
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getA() {
        return a;
    }

    public int getRGB() {
        return value;
    }

    public Color subtract(Color other) {
        return new Color(this.r - other.r, this.g - other.g, this.b - other.b);
    }

    public Color add(Color other) {
        return new Color(this.r + other.r, this.g + other.g, this.b + other.b);
    }

    public int squaredDifferenceTo(Color color) {
        int rDiff = color.r - r;
        int gDiff = color.g - g;
        int bDiff = color.b - b;
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }

    public Color scalarMultiply(double scalar) {
        return new Color((int) (this.r * scalar), (int) (this.g * scalar), (int) (this.b * scalar));
    }

    public Color clamp(int minimum, int maximum) {
        return new Color(Mth.clamp(this.r, minimum, maximum), Mth.clamp(this.g, minimum, maximum), Mth.clamp(this.b, minimum, maximum));
    }

    public int hashCode() {
        return value;
    }

    public boolean equals(Object obj) {
        return obj instanceof Color && ((Color) obj).getRGB() == this.getRGB();
    }

    public String toString() {
        return getClass().getName() + "[r=" + getR() + ",g=" + getG() + ",b=" + getB() + "]";
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b);
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        int cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    public static int BGRtoRGB(int ABGR) {
        int a = (ABGR >> 24) & 0xFF;
        int b = (ABGR >> 16) & 0xFF;
        int g = (ABGR >> 8) & 0xFF;
        int r = ABGR & 0xFF;

        return a << 24 | r << 16 | g << 8 | b;
    }

    /**
     * It's equivalent to BGRtoRGB, but it's there to make code more readable.
     */
    public static int RGBtoBGR(int ARGB) {
        return BGRtoRGB(ARGB);
    }

    public static int RGBFromHex(String hexColor) {
        return new Color((int) Long.parseLong(hexColor.replace("#", ""), 16), true).getRGB();
    }
}
