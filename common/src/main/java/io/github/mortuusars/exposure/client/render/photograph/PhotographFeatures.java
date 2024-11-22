package io.github.mortuusars.exposure.client.render.photograph;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.PhotographType;
import io.github.mortuusars.exposure.core.pixel_modifiers.PixelModifier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PhotographFeatures {
    public static final PhotographFeatures REGULAR = new PhotographFeatures("", ExposureClient.Textures.Photograph.REGULAR_PAPER,
            ExposureClient.Textures.EMPTY, ExposureClient.Textures.Photograph.REGULAR_ALBUM_PAPER, ExposureClient.Textures.EMPTY, PixelModifier.EMPTY);

    private static final Map<PhotographType, PhotographFeatures> REGISTERED_FEATURES = new HashMap<>();

    private final String name;
    private final ResourceLocation paperTexture;
    private final ResourceLocation overlayTexture;
    private final ResourceLocation albumPaperTexture;
    private final ResourceLocation albumOverlayTexture;
    private final PixelModifier pixelModifier;

    public PhotographFeatures(String name, ResourceLocation paperTexture, ResourceLocation overlayTexture,
                              ResourceLocation albumPaperTexture, ResourceLocation albumOverlayTexture,
                              PixelModifier pixelModifier) {
        this.name = name;
        this.paperTexture = paperTexture;
        this.overlayTexture = overlayTexture;
        this.albumPaperTexture = albumPaperTexture;
        this.albumOverlayTexture = albumOverlayTexture;
        this.pixelModifier = pixelModifier;
    }

    static {
        REGISTERED_FEATURES.put(PhotographType.REGULAR, REGULAR);
        REGISTERED_FEATURES.put(PhotographType.AGED, new PhotographFeatures(
                "aged",
                ExposureClient.Textures.Photograph.AGED_PAPER,
                ExposureClient.Textures.Photograph.AGED_OVERLAY,
                ExposureClient.Textures.Photograph.AGED_ALBUM_PAPER,
                ExposureClient.Textures.Photograph.AGED_ALBUM_OVERLAY,
                PixelModifier.AGED));
    }

    public static void register(PhotographType photographType, PhotographFeatures features) {
        Preconditions.checkState(!REGISTERED_FEATURES.containsKey(photographType),
                "PhotographFeatures for '%s' are already registered.", photographType);
        REGISTERED_FEATURES.put(photographType, features);
    }

    public static @NotNull PhotographFeatures get(PhotographType photographType) {
        return REGISTERED_FEATURES.getOrDefault(photographType, REGULAR);
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getPaperTexture() {
        return paperTexture;
    }

    public ResourceLocation getOverlayTexture() {
        return overlayTexture;
    }

    public ResourceLocation getAlbumPaperTexture() {
        return albumPaperTexture;
    }

    public ResourceLocation getAlbumOverlayTexture() {
        return albumOverlayTexture;
    }

    public PixelModifier getPixelModifier() {
        return pixelModifier;
    }

    public boolean hasOverlayTexture() {
        return getOverlayTexture() != ExposureClient.Textures.EMPTY;
    }

    public boolean hasAlbumOverlayTexture() {
        return getAlbumOverlayTexture() != ExposureClient.Textures.EMPTY;
    }
}
