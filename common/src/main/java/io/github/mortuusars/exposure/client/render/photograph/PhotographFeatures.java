package io.github.mortuusars.exposure.client.render.photograph;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.PhotographType;
import io.github.mortuusars.exposure.core.pixel_modifiers.PixelModifier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PhotographFeatures {
    public static final PhotographFeatures DEFAULT = new PhotographFeatures(PhotographTextures.REGULAR_PAPER,
            PhotographTextures.EMPTY, PhotographTextures.REGULAR_ALBUM_PAPER, PhotographTextures.EMPTY, PixelModifier.EMPTY);

    private static final Map<PhotographType, PhotographFeatures> REGISTERED_FEATURES = new HashMap<>();

    private final ResourceLocation paperTexture;
    private final ResourceLocation overlayTexture;
    private final ResourceLocation albumPaperTexture;
    private final ResourceLocation albumOverlayTexture;
    private final PixelModifier pixelModifier;

    public PhotographFeatures(ResourceLocation paperTexture, ResourceLocation overlayTexture,
                              ResourceLocation albumPaperTexture, ResourceLocation albumOverlayTexture,
                              PixelModifier pixelModifier) {
        this.paperTexture = paperTexture;
        this.overlayTexture = overlayTexture;
        this.albumPaperTexture = albumPaperTexture;
        this.albumOverlayTexture = albumOverlayTexture;
        this.pixelModifier = pixelModifier;
    }

    static {
        REGISTERED_FEATURES.put(PhotographType.REGULAR, DEFAULT);
        REGISTERED_FEATURES.put(PhotographType.AGED, new PhotographFeatures(
                PhotographTextures.AGED_PAPER,
                PhotographTextures.AGED_OVERLAY,
                PhotographTextures.AGED_ALBUM_PAPER,
                PhotographTextures.AGED_ALBUM_OVERLAY,
                PixelModifier.AGED));
    }

    public static void register(PhotographType photographType, PhotographFeatures features) {
        Preconditions.checkState(!REGISTERED_FEATURES.containsKey(photographType),
                "PhotographFeatures for '%s' are already registered.", photographType);
        REGISTERED_FEATURES.put(photographType, features);
    }

    public static @NotNull PhotographFeatures get(PhotographType photographType) {
        return REGISTERED_FEATURES.getOrDefault(photographType, DEFAULT);
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
}
