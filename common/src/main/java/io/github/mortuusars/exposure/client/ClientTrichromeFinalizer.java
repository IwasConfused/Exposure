package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.capture.component.ExposureUploaderComponent;
import io.github.mortuusars.exposure.client.render.image.ResourceImage;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.TrichromeCombiner;
import io.github.mortuusars.exposure.core.image.ExposureDataImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.warehouse.PalettedImage;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Primary use is to make trichromes that have one or more texture layers. Textures cannot be used serverside.
 */
public class ClientTrichromeFinalizer {
    private static final Queue<TrichromeExposureImagesGetter> processingQueue = new LinkedList<>();
    private static final int MAX_ATTEMPTS = 200;

    public static void finalizeTrichrome(ExposureIdentifier red, ExposureIdentifier green, ExposureIdentifier blue, String exposureId) {
        processingQueue.add(new TrichromeExposureImagesGetter(red, green, blue, exposureId, MAX_ATTEMPTS));
    }

    public static void clientTick() {
        @Nullable ClientTrichromeFinalizer.TrichromeExposureImagesGetter item = processingQueue.peek();
        if (item == null) return;

        item.tick();

        Image[] images = item.getImages();
        if (images.length >= 3) {
            PalettedImage trichromeData = TrichromeCombiner.create(images[0], images[1], images[2]);
            ExposureUploaderComponent uploaderComponent = new ExposureUploaderComponent(item.exposureId);
            uploaderComponent.save(trichromeData.width(), trichromeData.height(), trichromeData.pixels(), new CompoundTag());

            processingQueue.remove();
        } else if (item.attemptsRemaining < 0) {
            TrichromeExposureImagesGetter removedItem = processingQueue.remove();
            for (@Nullable Image image : removedItem.images) {
                if (image != null) {
                    image.close();
                }
            }
            Exposure.LOGGER.error("Cannot finalize a chromatic {}. Couldn't get all images data in time. {}, {}, {}",
                    removedItem.exposureId, removedItem.red, removedItem.green, removedItem.blue);
        }
    }

    private static class TrichromeExposureImagesGetter {
        private final ExposureIdentifier red, green, blue;
        private final String exposureId;
        private final Image[] images = new Image[3];
        private int attemptsRemaining;

        public TrichromeExposureImagesGetter(ExposureIdentifier red, ExposureIdentifier green,
                                             ExposureIdentifier blue, String exposureId, int attempts) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.exposureId = exposureId;
            this.attemptsRemaining = attempts;
        }

        public void tick() {
            if (images[0] == null) {
                images[0] = tryGetImage(red);
            }

            if (images[1] == null) {
                images[1] = tryGetImage(green);
            }

            if (images[2] == null) {
                images[2] = tryGetImage(blue);
            }

            attemptsRemaining--;
        }

        private @Nullable Image tryGetImage(ExposureIdentifier identifier) {
            return identifier.map(id ->
                            ExposureClient.exposureCache().getOrQuery(id)
                                    .map(data -> new ExposureDataImage(id, data))
                                    .orElse(null),
                    ResourceImage::new);
        }

        public Image[] getImages() {
            return images;
        }
    }
}
