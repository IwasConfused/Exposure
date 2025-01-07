package io.github.mortuusars.exposure.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.color.Color;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;

public class ColorPalettes extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation MAP_COLORS_PLUS = Exposure.resource("map_colors_plus");
    public static final ResourceLocation MAP_COLORS = Exposure.resource("map_colors");
    public static final ResourceLocation RESURRECT_64 = Exposure.resource("resurrect_64");
    public static final ResourceLocation SLSO8 = Exposure.resource("slso8");
    public static final ResourceLocation INK = Exposure.resource("ink");

    public static final ResourceLocation DEFAULT = MAP_COLORS_PLUS;

    private static final Logger LOGGER = LogUtils.getLogger();

    protected Map<ResourceLocation, ColorPalette> palettes = ImmutableMap.of();

    public ColorPalettes() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "exposure/color_palette");
    }

    public @NotNull ColorPalette getOrDefault(ResourceLocation paletteId) {
        @Nullable ColorPalette palette = palettes.get(paletteId);
        if (palette == null) {
            LOGGER.warn("Color Palette with id '{}' is not found. Default palette will be used instead.", paletteId);
            return ColorPalette.MAP_COLORS_PLUS;
        }
        return palette;
    }

    public @NotNull ColorPalette getOrElse(ResourceLocation paletteId, @NotNull ColorPalette fallback) {
        @Nullable ColorPalette palette = palettes.get(paletteId);
        if (palette == null) {
            LOGGER.warn("Color Palette with id '{}' is not found. Returning fallback palette.", paletteId);
            return fallback;
        }
        return palette;
    }

    public Map<ResourceLocation, ColorPalette> get() {
        return ImmutableMap.copyOf(palettes);
    }

    public void set(Map<ResourceLocation, ColorPalette> palettes) {
        this.palettes = palettes;
    }

    // --

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, ColorPalette> builder = ImmutableMap.builder();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                JsonArray colorsJsonArray = entry.getValue().getAsJsonObject().getAsJsonArray("colors");

                int[] colors = new int[colorsJsonArray.size()];

                for (int i = 0; i < colors.length; i++) {
                    String hexString = colorsJsonArray.get(i).getAsString();
                    colors[i] = Color.fromHex(hexString).getARGB();
                }

                ColorPalette palette = new ColorPalette(colors);
                builder.put(location, palette);
            } catch (Exception e) {
                LOGGER.error("Error loading exposure color palette '{}'", location, e);
            }
        }

        set(builder.build());

        if (this.palettes.isEmpty())
            LOGGER.info("No exposure color palettes have been loaded.");
        else {
            LOGGER.info("Loaded {} exposure color palettes.", this.palettes.size());
        }
    }
}
