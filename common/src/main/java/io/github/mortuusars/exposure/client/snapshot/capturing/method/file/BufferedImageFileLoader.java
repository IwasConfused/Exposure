package io.github.mortuusars.exposure.client.snapshot.capturing.method.file;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.FileCaptureMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BufferedImageFileLoader implements ImageFileLoader {
    @Override
    public CaptureResult load(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            return CaptureResult.success(new WrappedBufferedImage(image));
        } catch (IOException e) {
            Exposure.LOGGER.error("Loading image from file path '{}' failed:", file, e);
            return CaptureResult.error(FileCaptureMethod.ERROR_CANNOT_READ);
        }
    }
}
