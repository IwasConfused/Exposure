package io.github.mortuusars.exposure.core.image.color;


import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ColorPalette {
    private static final Map<ResourceLocation, ColorPalette> PALETTES = new HashMap<>();

    public static ColorPalette register(ResourceLocation id, ColorPalette palette) {
        Preconditions.checkState(!PALETTES.containsKey(id), "Palette with id '%S' is already registered.", id);
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
            0x277D59, 0x30996D, 0x38B27F, 0x1D5E43,
            0x73A4AE, 0x8CC9D5, 0xA3E9F7, 0x567B82,
            0x8C8C8C, 0xABABAB, 0xC7C7C7, 0x696969,
            0x0000B4, 0x0000DC, 0x0000FF, 0x000087,
            0xB47070, 0xDC8A8A, 0xFFA0A0, 0x875454,
            0x757575, 0x909090, 0xA7A7A7, 0x585858,
            0x005700, 0x006A00, 0x007C00, 0x004100,
            0xB4B4B4, 0xDCDCDC, 0xFFFFFF, 0x878787,
            0x817673, 0x9E908D, 0xB8A8A4, 0x615856,
            0x364C6A, 0x425E82, 0x4D6D97, 0x28394F,
            0x4F4F4F, 0x606060, 0x707070, 0x3B3B3B,
            0xB42D2D, 0xDC3737, 0xFF4040, 0x872121,
            0x325464, 0x3E667B, 0x48778F, 0x263F4B,
            0xACB1B4, 0xD3D9DC, 0xF5FCFF, 0x818587,
            0x245998, 0x2C6DBA, 0x337FD8, 0x1B4372,
            0x98357D, 0xBA4199, 0xD84CB2, 0x72285E,
            0x986C48, 0xBA8458, 0xD89966, 0x725136,
            0x24A1A1, 0x2CC5C5, 0x33E5E5, 0x1B7979,
            0x119059, 0x15B06D, 0x19CC7F, 0x0D6C43,
            0x7459AA, 0x8E6DD0, 0xA57FF2, 0x574380,
            0x353535, 0x414141, 0x4C4C4C, 0x282828,
            0x6C6C6C, 0x848484, 0x999999, 0x515151,
            0x6C5935, 0x846D41, 0x997F4C, 0x514328,
            0x7D2C59, 0x99366D, 0xB23F7F, 0x5E2143,
            0x7D3524, 0x99412C, 0xB24C33, 0x5E281B,
            0x243548, 0x2C4158, 0x334C66, 0x1B2836,
            0x245948, 0x2C6D58, 0x337F66, 0x1B4336,
            0x24246C, 0x2C2C84, 0x333399, 0x1B1B51,
            0x111111, 0x151515, 0x191919, 0x0D0D0D,
            0x36A8B0, 0x42CDD7, 0x4DEEFA, 0x287E84,
            0x969A40, 0xB7BC4F, 0xD5DB5C, 0x707330,
            0xB45A34, 0xDC6E3F, 0xFF804A, 0x874327,
            0x289900, 0x32BB00, 0x3AD900, 0x1E7200,
            0x223C5B, 0x2A4A6F, 0x315681, 0x192D44,
            0x00014F, 0x000160, 0x000270, 0x00013B,
            0x717C93, 0x8A98B4, 0xA1B1D1, 0x555D6E,
            0x193970, 0x1F4689, 0x24529F, 0x132B54,
            0x4C3D69, 0x5D4B80, 0x6C5795, 0x392E4E,
            0x614C4F, 0x775D60, 0x8A6C70, 0x49393B,
            0x195D83, 0x1F72A0, 0x2485BA, 0x134662,
            0x255248, 0x2D6458, 0x357567, 0x1C3D36,
            0x373670, 0x43428A, 0x4E4DA0, 0x292854,
            0x181C28, 0x1E2331, 0x232939, 0x12151E,
            0x454B5F, 0x545C74, 0x626B87, 0x333847,
            0x40403D, 0x4F4F4B, 0x5C5C57, 0x30302E,
            0x3E3356, 0x4B3E69, 0x58497A, 0x2E2640,
            0x402B35, 0x4F3541, 0x5C3E4C, 0x302028,
            0x182335, 0x1E2B41, 0x23324C, 0x121A28,
            0x1D3935, 0x244641, 0x2A524C, 0x162B28,
            0x202A64, 0x27337A, 0x2E3C8E, 0x181F4B,
            0x0B0F1A, 0x0D121F, 0x101625, 0x080B13,
            0x222185, 0x2A29A3, 0x3130BD, 0x191964,
            0x442C68, 0x53367F, 0x613F94, 0x33214E,
            0x141140, 0x19154F, 0x1D195C, 0x0F0D30,
            0x5E580F, 0x736C12, 0x867E16, 0x46420B,
            0x626428, 0x787A32, 0x8C8E3A, 0x4A4B1E,
            0x2B1F3C, 0x35254A, 0x3E2C56, 0x20172D,
            0x5D7F0E, 0x729B11, 0x85B414, 0x465F0A,
            0x464646, 0x565656, 0x646464, 0x343434,
            0x677B98, 0x7E96BA, 0x93AFD8, 0x4D5C72,
            0x697559, 0x81906D, 0x96A77F, 0x4F5843,
            0x000000, 0x000000, 0x000000, 0x000000,
            0x000000, 0x000000, 0x000000, 0x000000
    }));

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

        for (int i = 0; i < colors.length; i++) {
            if (colors[i].squaredDifferenceTo(color) < colors[closest].squaredDifferenceTo(color)) {
                closest = i;
            }
        }

        return closest;
    }
}