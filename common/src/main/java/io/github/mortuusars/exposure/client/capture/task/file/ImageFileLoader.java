package io.github.mortuusars.exposure.client.capture.task.file;

import io.github.mortuusars.exposure.core.cycles.task.Result;
import io.github.mortuusars.exposure.client.image.Image;

import java.io.File;

public interface ImageFileLoader {
    Result<Image> load(File file);

    static ImageFileLoader chooseFitting(File file) {
        return new BufferedImageFileLoader();
    }

    static ImageFileLoader fallback(ImageFileLoader main, ImageFileLoader fallback) {
        return file -> {
            Result<Image> mainResult = main.load(file);
            return mainResult.isSuccessful() ? mainResult : fallback.load(file);
        };
    }
}
