package io.github.mortuusars.exposure.client.snapshot.capture.task.file;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.snapshot.capture.task.FileCaptureTask;
import io.github.mortuusars.exposure.util.task.Result;
import io.github.mortuusars.exposure.client.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class NativeImageFileLoader implements ImageFileLoader {
    @Override
    public Result<Image> load(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return Result.success(new WrappedNativeImage(NativeImage.read(NativeImage.Format.RGBA, inputStream)));
        } catch (IOException e) {
            Exposure.LOGGER.error("Loading image from file path '{}' failed:", file, e);
            return Result.error(FileCaptureTask.ERROR_CANNOT_READ);
        }
    }
}
