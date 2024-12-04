package io.github.mortuusars.exposure.client.snapshot.capturing.method.file;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.exposure.util.task.Result;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.FileCaptureMethod;
import io.github.mortuusars.exposure.core.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BufferedImageFileLoader implements ImageFileLoader {
    @Override
    public Result<Image> load(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            return Result.success(new WrappedBufferedImage(image));
        } catch (IOException e) {
            Exposure.LOGGER.error("Loading image from file path '{}' failed:", file, e);
            return Result.error(FileCaptureMethod.ERROR_CANNOT_READ);
        }
    }
}
