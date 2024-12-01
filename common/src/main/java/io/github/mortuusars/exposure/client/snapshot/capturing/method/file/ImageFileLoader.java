package io.github.mortuusars.exposure.client.snapshot.capturing.method.file;

import com.google.common.io.Files;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;

import java.io.File;

public interface ImageFileLoader {
    CaptureResult load(File file);

    static ImageFileLoader chooseFitting(File file) {
        return Files.getFileExtension(file.toString()).equalsIgnoreCase("png")
                ? fallback(new NativeImageFileLoader(), new BufferedImageFileLoader())
                : new BufferedImageFileLoader();
    }

    static ImageFileLoader fallback(ImageFileLoader main, ImageFileLoader fallback) {
        return file -> {
            CaptureResult mainResult = main.load(file);
            return mainResult.isSuccessful() ? mainResult : fallback.load(file);
        };
    }
}
