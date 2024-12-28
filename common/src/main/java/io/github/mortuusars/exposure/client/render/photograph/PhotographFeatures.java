package io.github.mortuusars.exposure.client.render.photograph;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.RenderableImage;
import io.github.mortuusars.exposure.core.PhotographType;
import io.github.mortuusars.exposure.client.image.pixel_modifiers.PixelModifier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record PhotographFeatures(String name,
                                 ResourceLocation paperTexture,
                                 ResourceLocation overlayTexture,
                                 ResourceLocation albumPaperTexture,
                                 ResourceLocation albumOverlayTexture,
                                 PixelModifier pixelModifier) {
    public static final PhotographFeatures REGULAR = new PhotographFeatures("",
            ExposureClient.Textures.Photograph.REGULAR_PAPER,
            ExposureClient.Textures.EMPTY,
            ExposureClient.Textures.Photograph.REGULAR_ALBUM_PAPER,
            ExposureClient.Textures.EMPTY,
            PixelModifier.EMPTY);

    private static final Map<PhotographType, PhotographFeatures> REGISTERED_FEATURES = new HashMap<>();

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

    public boolean hasOverlayTexture() {
        return overlayTexture() != ExposureClient.Textures.EMPTY;
    }

    public boolean hasAlbumOverlayTexture() {
        return albumOverlayTexture() != ExposureClient.Textures.EMPTY;
    }

    public RenderableImage process(RenderableImage image) {
        return pixelModifier != null ? image.processWith(pixelModifier) : image;
    }
}
