package io.github.mortuusars.exposure.client;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.TrichromeImage;
import io.github.mortuusars.exposure.client.capture.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.capture.saving.PalettedExposureUploader;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClientTrichromeFinalizer {
    private static final Queue<TrichromeExposureImagesGetter> processingQueue = new LinkedList<>();
    private static final int MAX_ATTEMPTS = 600;

    //TODO: use SnapShot tasks

    public static void finalizeTrichrome(String id, List<ExposureIdentifier> layers) {
        processingQueue.add(new TrichromeExposureImagesGetter(id, layers, MAX_ATTEMPTS));
    }

    public static void clientTick() {
        @Nullable TrichromeExposureImagesGetter item = processingQueue.peek();
        if (item == null) return;

        item.tick();

//        if (item.isDone()) {
//            if (item.hasAllImages()) {
//                List<Image> images = item.getImages();
//                TrichromeImage trichromeImage = new TrichromeImage(images.get(0), images.get(1), images.get(2));
//                PalettedImage palettedImage = ImagePalettizer.palettizeAndClose(trichromeImage, ColorPalette.MAP_COLORS, true);
//                PalettedExposure exposure = palettedImage.toExposure(new PalettedExposure.Tag(ExposureType.COLOR,
//                        Minecrft.player().getScoreboardName(), UnixTimestamp.Seconds.now(), false, false));
//                PalettedExposureUploader.upload(item.getId(), exposure);
//                palettedImage.close();
//            } else {
//                Exposure.LOGGER.error("Cannot create chromatic image with id {}. Couldn't get all images data in time. {}",
//                        item.getId(), String.join(", ", item.getLayers().stream().map(ExposureIdentifier::toString).toList()));
//            }
//
//            processingQueue.remove();
//        }
    }

    private static class TrichromeExposureImagesGetter {
        private final String id;
        private final List<ExposureIdentifier> layers;
        private final List<Image> images = NonNullList.withSize(3, Image.EMPTY);
        private int attemptsRemaining;

        public TrichromeExposureImagesGetter(String id, List<ExposureIdentifier> layers, int attempts) {
            Preconditions.checkState(layers.size() == 3);
            this.id = id;
            this.layers = layers;
            this.attemptsRemaining = attempts;
        }

        public void tick() {
            for (int i = 0; i < 3; i++) {
//                if (images.get(i).isEmpty()) {
//                    images.set(i, ExposureClient.createExposureImage(layers.get(i)));
//                }
            }

            attemptsRemaining--;
        }

//        public boolean isDone() {
//            return attemptsRemaining <= 0 || hasAllImages();
//        }

//        public boolean hasAllImages() {
//            return images.stream().noneMatch(Image::isEmpty);
//        }

        public List<ExposureIdentifier> getLayers() {
            return layers;
        }

        public List<Image> getImages() {
            return images;
        }

        public String getId() {
            return id;
        }
    }
}
