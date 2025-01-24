package io.github.mortuusars.exposure.client.render.photograph;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.modifier.ImageModifier;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record PhotographStyle(ResourceLocation paperTexture,
                              ResourceLocation overlayTexture,
                              ResourceLocation albumPaperTexture,
                              ResourceLocation albumOverlayTexture,
                              ImageModifier modifier) {

    public static final PhotographStyle REGULAR = new PhotographStyle(
            ExposureClient.Textures.Photograph.REGULAR_PAPER,
            ExposureClient.Textures.EMPTY,
            ExposureClient.Textures.Photograph.REGULAR_ALBUM_PAPER,
            ExposureClient.Textures.EMPTY,
            ImageModifier.EMPTY);

    public static PhotographStyle of(ItemStack photographStack) {
        return photographStack.getItem() instanceof PhotographItem photographItem
                ? PhotographStyles.get(photographItem.getType(photographStack))
                : PhotographStyle.REGULAR;
    }

    public boolean hasOverlayTexture() {
        return !overlayTexture().equals(ExposureClient.Textures.EMPTY);
    }

    public boolean hasAlbumOverlayTexture() {
        return !albumOverlayTexture().equals(ExposureClient.Textures.EMPTY);
    }

    public RenderableImage process(RenderableImage image) {
        return !modifier.equals(ImageModifier.EMPTY) ? image.modifyWith(modifier) : image;
    }
}
