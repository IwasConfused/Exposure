package io.github.mortuusars.exposure.client.capture.saving;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.color.Color;

import java.io.File;

public class NativeImageFileSaver {
    private final File file;

    public NativeImageFileSaver(File file) {
        this.file = file;
    }

    public NativeImageFileSaver(String filePath) {
        this.file = new File(filePath);
    }

    public void save(Image image) {
        //TODO: support different image formats like in loader (buffered and native) because NativeImage does not exist on server

        try (NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false)) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixelColor = image.getPixelARGB(x, y);
                    nativeImage.setPixelRGBA(x, y, Color.ARGBtoABGR(pixelColor));
                }
            }

            boolean ignored = file.getParentFile().mkdirs();
            nativeImage.writeToFile(file);
            Exposure.LOGGER.info("Saved image: {}", file);
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Failed to save image to file: {}", e.toString());
        }
    }
}
