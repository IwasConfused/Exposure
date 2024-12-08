package io.github.mortuusars.exposure.core.image.color;

import net.minecraft.util.Mth;

public class Color {
    public static final Color WHITE = new Color(255, 255, 255, 255);

    private final int a, r, g, b;

    private Color(int a, int r, int g, int b) {
        validate(a, r, g, b);
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    private static void validate(int a, int r, int g, int b) {
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
            throw new IllegalArgumentException("Color parameter outside of expected range:" + badComponentString);
        }
    }

    public static Color argb(int a, int r, int g, int b) {
        return new Color(a, r, g, b);
    }

    public static Color argb(int argb) {
        return argb(argb >>> 24, argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF);
    }

    public static Color rgb(int r, int g, int b) {
        return new Color(255, r, g, b);
    }

    public static Color rgb(int rgb) {
        return rgb(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }

    public static Color abgr(int abgr) {
        return argb(abgr >>> 24, abgr & 0xFF, abgr >> 8 & 0xFF, abgr >> 16 & 0xFF);
    }

    public static Color bgr(int bgr) {
        return rgb(bgr & 0xFF, bgr >> 8 & 0xFF, bgr >> 16 & 0xFF);
    }

    public static Color argbF(float a, float r, float g, float b) {
        return argb(clamp((int) (a * 255)), clamp((int) (r * 255)), clamp((int) (g * 255)), clamp((int) (b * 255)));
    }

    public static Color rgbF(float r, float g, float b) {
        return rgb(clamp((int) (r * 255)), clamp((int) (g * 255)), clamp((int) (b * 255)));
    }

    // --

    public int getA() {
        return a;
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

    public float getAF() {
        return a / 255f;
    }

    public float getRF() {
        return r / 255f;
    }

    public float getGF() {
        return g / 255f;
    }

    public float getBF() {
        return b / 255f;
    }

    public int getARGB() {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public int getABGR() {
        return a << 24 | b << 16 | g << 8 | r;
    }

    public int getRGB() {
        return 255 << 24 | r << 16 | g << 8 | b;
    }

    public int getBGR() {
        return 255 << 24 | b << 16 | g << 8 | r;
    }

    public Color withAlpha(int alpha) {
        return new Color(alpha, getR(), getG(), getB());
    }

    public Color withAlphaF(float alpha) {
        return new Color(clamp((int) (alpha / 255)), getR(), getG(), getB());
    }

    public Color add(Color other) {
        return Color.argb(clamp(this.a + other.a),
                clamp(this.r + other.r),
                clamp(this.g + other.g),
                clamp(this.b + other.b));
    }

    public Color.Unbounded addUnbounded(Color other) {
        return new Color.Unbounded(this.r + other.r, this.g + other.g, this.b + other.b);
    }

    public Color.Unbounded addUnbounded(Color.Unbounded other) {
        return new Color.Unbounded(this.r + other.r, this.g + other.g, this.b + other.b);
    }

    public Color subtract(Color other) {
        return Color.argb(clamp(this.a - other.a),
                clamp(this.r - other.r),
                clamp(this.g - other.g),
                clamp(this.b - other.b));
    }

    public Color.Unbounded subtractUnbounded(Color other) {
        return new Color.Unbounded(this.r - other.r, this.g - other.g, this.b - other.b);
    }

    public Color multiply(double scalar) {
        return Color.argb(clamp((int) (this.a * scalar)),
                clamp((int) (this.r * scalar)),
                clamp((int) (this.g * scalar)),
                clamp((int) (this.b * scalar)));
    }

    public int squaredDifferenceTo(Color color) {
        int rDiff = Math.abs(r - color.r);
        int gDiff = Math.abs(g - color.g);
        int bDiff = Math.abs(b - color.b);
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }

    public float differenceTo(Color color) {
        return Mth.sqrt(squaredDifferenceTo(color));
    }

    // --

    public static int alpha(int argb) {
        return argb >>> 24;
    }

    public static int red(int argb) {
        return argb >> 16 & 0xFF;
    }

    public static int green(int argb) {
        return argb >> 8 & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static float alphaF(int argb) {
        return (argb >>> 24) / 255f;
    }

    public static float redF(int argb) {
        return (argb >> 16 & 0xFF) / 255f;
    }

    public static float greenF(int argb) {
        return (argb >> 8 & 0xFF) / 255f;
    }

    public static float blueF(int argb) {
        return (argb & 0xFF) / 255f;
    }

    public static int pack(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int clamp(int channel) {
        return Mth.clamp(channel, 0, 255);
    }

    public static int ABGRtoARGB(int ABGR) {
        int a = (ABGR >> 24) & 0xFF;
        int b = (ABGR >> 16) & 0xFF;
        int g = (ABGR >> 8) & 0xFF;
        int r = ABGR & 0xFF;

        return a << 24 | r << 16 | g << 8 | b;
    }

    /**
     * It's equivalent to BGRtoRGB, but it's there to convey the intent
     */
    public static int ARGBtoABGR(int ARGB) {
        return ABGRtoARGB(ARGB);
    }

    public static Color fromHex(String hexColor) {
        return argb((int) Long.parseLong(hexColor.replace("#", ""), 16));
    }

    public static class HSB {
        public static int HSBtoRGB(float hue, float saturation, float brightness) {
            int r = 0, g = 0, b = 0;
            if (saturation == 0) {
                r = g = b = (int) (brightness * 255.0f + 0.5f);
            } else {
                float h = (hue - (float) Math.floor(hue)) * 6.0f;
                float f = h - (float) Math.floor(h);
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

        public static float[] RGBtoHSB(int r, int g, int b, float[] output) {
            float hue, saturation, brightness;
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
            output[0] = hue;
            output[1] = saturation;
            output[2] = brightness;
            return output;
        }

        public static float[] RGBtoHSB(int r, int g, int b) {
            return RGBtoHSB(r, g, b, new float[3]);
        }
    }

    public record Unbounded(int r, int g, int b) {
        public Color clamp() {
            return Color.rgb(Color.clamp(r), Color.clamp(g), Color.clamp(b));
        }

        public Color.Unbounded multiply(double scalar) {
            return new Color.Unbounded((int) (this.r * scalar), (int) (this.g * scalar), (int) (this.b * scalar));
        }
    }
}