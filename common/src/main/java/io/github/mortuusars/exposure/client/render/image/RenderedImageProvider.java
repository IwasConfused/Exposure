package io.github.mortuusars.exposure.client.render.image;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.image.EmptyImage;
import io.github.mortuusars.exposure.core.image.ExposureDataImage;
import io.github.mortuusars.exposure.core.image.IImage;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class RenderedImageProvider {
    public static final RenderedImageProvider EMPTY = new RenderedImageProvider(new EmptyImage());
    public static final RenderedImageProvider HIDDEN = new RenderedImageProvider(TextureImage.getTexture(Exposure.resource("textures/exposure/pack.png")));

    protected final IImage image;

    public RenderedImageProvider(IImage image) {
        this.image = image;
    }

    public static RenderedImageProvider fromFrame(ExposureFrame frame) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return EMPTY;
        }

        if (shouldHideImage(frame)) {
            return HIDDEN;
        }

        @Nullable IImage image = frame.identifier().map(
                id -> ExposureClient.exposureCache().getOrQuery(id)
                        .map(data -> new ExposureDataImage(id, data))
                        .orElse(null),
                TextureImage::getTexture);

        if (image != null) {
            return new RenderedImageProvider(image);
        }

        return EMPTY;
    }

    protected static boolean shouldHideImage(ExposureFrame frame) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        if (Config.Client.HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS.get()
                && !frame.photographer().isEmpty()
                && !frame.photographer().matches(player)) {
            return true;
        }

        if (Config.Client.HIDE_LOADED_PHOTOGRAPHS_MADE_BY_OTHERS.get()
                && frame.isFromFile()
                && !frame.photographer().isEmpty()
                && !frame.photographer().matches(player)) {
                return true;
        }

        return false;
    }

    public IImage get() {
        return image;
    }

    public String getInstanceId() {
        return get().getImageId();
    }
}
