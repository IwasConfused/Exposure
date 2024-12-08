package io.github.mortuusars.exposure.core.image.color;


import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.CameraAccessors;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ColorPalette {
    private static final Map<ResourceLocation, ColorPalette> PALETTES = new HashMap<>();

    public static final Codec<ColorPalette> CODEC = ResourceLocation.CODEC.xmap(ColorPalette::byId, ColorPalette::idOf);

    public static final StreamCodec<ByteBuf, ColorPalette> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ColorPalette::idOf,
            ColorPalette::byId
    );

    public static ColorPalette register(ResourceLocation id, ColorPalette palette) {
        Preconditions.checkState(!PALETTES.containsKey(id), "Palette with id '%s' is already registered.", id);
        PALETTES.put(id, palette);
        return palette;
    }

    public static @Nullable ColorPalette byId(ResourceLocation id) {
        return PALETTES.get(id);
    }

    public static ResourceLocation idOf(ColorPalette palette) {
        for (Map.Entry<ResourceLocation, ColorPalette> entry : PALETTES.entrySet()) {
            if (entry.getValue() == palette) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Color palette is not registered.");
    }

    public static final ColorPalette MAP_COLORS = register(Exposure.resource("map_colors"), new ColorPalette(new int[]{
            0x000000, 0x000000, 0x000000, 0x000000,
            0x597D27, 0x6D9930, 0x7FB238, 0x435E1D,
            0xAEA473, 0xD5C98C, 0xF7E9A3, 0x827B56,
            0x8C8C8C, 0xABABAB, 0xC7C7C7, 0x696969,
            0xB40000, 0xDC0000, 0xFF0000, 0x870000,
            0x7070B4, 0x8A8ADC, 0xA0A0FF, 0x545487,
            0x757575, 0x909090, 0xA7A7A7, 0x585858,
            0x005700, 0x006A00, 0x007C00, 0x004100,
            0xB4B4B4, 0xDCDCDC, 0xFFFFFF, 0x878787,
            0x737681, 0x8D909E, 0xA4A8B8, 0x565861,
            0x6A4C36, 0x825E42, 0x976D4D, 0x4F3928,
            0x4F4F4F, 0x606060, 0x707070, 0x3B3B3B,
            0x2D2DB4, 0x3737DC, 0x4040FF, 0x212187,
            0x645432, 0x7B663E, 0x8F7748, 0x4B3F26,
            0xB4B1AC, 0xDCD9D3, 0xFFFCF5, 0x878581,
            0x985924, 0xBA6D2C, 0xD87F33, 0x72431B,
            0x7D3598, 0x9941BA, 0xB24CD8, 0x5E2872,
            0x486C98, 0x5884BA, 0x6699D8, 0x365172,
            0xA1A124, 0xC5C52C, 0xE5E533, 0x79791B,
            0x599011, 0x6DB015, 0x7FCC19, 0x436C0D,
            0xAA5974, 0xD06D8E, 0xF27FA5, 0x804357,
            0x353535, 0x414141, 0x4C4C4C, 0x282828,
            0x6C6C6C, 0x848484, 0x999999, 0x515151,
            0x35596C, 0x416D84, 0x4C7F99, 0x284351,
            0x592C7D, 0x6D3699, 0x7F3FB2, 0x43215E,
            0x24357D, 0x2C4199, 0x334CB2, 0x1B285E,
            0x483524, 0x58412C, 0x664C33, 0x36281B,
            0x485924, 0x586D2C, 0x667F33, 0x36431B,
            0x6C2424, 0x842C2C, 0x993333, 0x511B1B,
            0x111111, 0x151515, 0x191919, 0x0D0D0D,
            0xB0A836, 0xD7CD42, 0xFAEE4D, 0x847E28,
            0x409A96, 0x4FBCB7, 0x5CDBD5, 0x307370,
            0x345AB4, 0x3F6EDC, 0x4A80FF, 0x274387,
            0x009928, 0x00BB32, 0x00D93A, 0x00721E,
            0x5B3C22, 0x6F4A2A, 0x815631, 0x442D19,
            0x4F0100, 0x600100, 0x700200, 0x3B0100,
            0x937C71, 0xB4988A, 0xD1B1A1, 0x6E5D55,
            0x703919, 0x89461F, 0x9F5224, 0x542B13,
            0x693D4C, 0x804B5D, 0x95576C, 0x4E2E39,
            0x4F4C61, 0x605D77, 0x706C8A, 0x3B3949,
            0x835D19, 0xA0721F, 0xBA8524, 0x624613,
            0x485225, 0x58642D, 0x677535, 0x363D1C,
            0x703637, 0x8A4243, 0xA04D4E, 0x542829,
            0x281C18, 0x31231E, 0x392923, 0x1E1512,
            0x5F4B45, 0x745C54, 0x876B62, 0x473833,
            0x3D4040, 0x4B4F4F, 0x575C5C, 0x2E3030,
            0x56333E, 0x693E4B, 0x7A4958, 0x40262E,
            0x352B40, 0x41354F, 0x4C3E5C, 0x282030,
            0x352318, 0x412B1E, 0x4C3223, 0x281A12,
            0x35391D, 0x414624, 0x4C522A, 0x282B16,
            0x642A20, 0x7A3327, 0x8E3C2E, 0x4B1F18,
            0x1A0F0B, 0x1F120D, 0x251610, 0x130B08,
            0x852122, 0xA3292A, 0xBD3031, 0x641919,
            0x682C44, 0x7F3653, 0x943F61, 0x4E2133,
            0x401114, 0x4F1519, 0x5C191D, 0x300D0F,
            0x0F585E, 0x126C73, 0x167E86, 0x0B4246,
            0x286462, 0x327A78, 0x3A8E8C, 0x1E4B4A,
            0x3C1F2B, 0x4A2535, 0x562C3E, 0x2D1720,
            0x0E7F5D, 0x119B72, 0x14B485, 0x0A5F46,
            0x464646, 0x565656, 0x646464, 0x343434,
            0x987B67, 0xBA967E, 0xD8AF93, 0x725C4D,
            0x597569, 0x6D9081, 0x7FA796, 0x43584F,
            0x000000, 0x000000, 0x000000, 0x000000,
            0x000000, 0x000000, 0x000000, 0x000000

    }));

//    0x0C150B

    private final Color[] colors;

    public ColorPalette(Color[] colors) {
        Preconditions.checkState(colors.length <= 256, "Palette size can be up to or exactly 256 colors.");
        this.colors = colors;
    }

    public ColorPalette(int[] colors) {
        this(Arrays.stream(colors).mapToObj(Color::new).toArray(Color[]::new));
    }

    public Color[] getColors() {
        return colors;
    }

    public Color byIndex(int index) {
        return colors[index & 0xFF];
    }

    public int closestColorIndexTo(Color color) {
        int closest = 0;
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < colors.length; i++) {
            int distance = colors[i].squaredDifferenceTo(color);
            if (distance < closestDistance) {
                closest = i;
                closestDistance = distance;
            }
        }

        return closest;
    }

    public int closestColorIndexTo(NeatColor color) {
        int closest = 0;
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < colors.length; i++) {
            int distance = colors[i].toNeatColor().squaredDifferenceTo(color);
            if (distance < closestDistance) {
                closest = i;
                closestDistance = distance;
            }
        }

        return closest;
    }
}