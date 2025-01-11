package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ColorPalettes {
    public static final ResourceKey<ColorPalette> MAP_COLORS_PLUS = createKey("map_colors_plus");
    public static final ResourceKey<ColorPalette> MAP_COLORS = createKey("map_colors");
    public static final ResourceKey<ColorPalette> RESURRECT_64 = createKey("resurrect_64");
    public static final ResourceKey<ColorPalette> SLSO8 = createKey("slso8");
    public static final ResourceKey<ColorPalette> INK = createKey("ink");
    public static final ResourceKey<ColorPalette> DEFAULT = MAP_COLORS_PLUS;

    private static ResourceKey<ColorPalette> createKey(String name) {
        return ResourceKey.create(Exposure.Registries.COLOR_PALETTE, Exposure.resource(name));
    }

    public static Holder<ColorPalette> get(RegistryAccess registryAccess, ResourceKey<ColorPalette> key) {
        Registry<ColorPalette> registry = registryAccess.registryOrThrow(Exposure.Registries.COLOR_PALETTE);
        return registry.getHolder(key).or(() -> registry.getHolder(DEFAULT)).or(registry::getAny).orElseThrow();
    }

    public static Holder<ColorPalette> get(RegistryAccess registryAccess, ResourceLocation key) {
        Registry<ColorPalette> registry = registryAccess.registryOrThrow(Exposure.Registries.COLOR_PALETTE);
        return registry.getHolder(key).or(() -> registry.getHolder(DEFAULT)).or(registry::getAny).orElseThrow();
    }
}
