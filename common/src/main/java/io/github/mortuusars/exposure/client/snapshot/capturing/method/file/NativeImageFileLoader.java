package io.github.mortuusars.exposure.client.snapshot.capturing.method.file;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.FileCaptureMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class NativeImageFileLoader implements ImageFileLoader {
    @Override
    public CaptureResult load(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return CaptureResult.success(new WrappedNativeImage(NativeImage.read(NativeImage.Format.RGBA, inputStream)));
        } catch (IOException e) {
            Exposure.LOGGER.error("Loading image from file path '{}' failed:", file, e);
            return CaptureResult.error(FileCaptureMethod.ERROR_CANNOT_READ);
        }
    }
}
