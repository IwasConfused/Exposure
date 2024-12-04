package io.github.mortuusars.exposure.client.snapshot.capturing.method.file;

import io.github.mortuusars.exposure.util.Result;
import io.github.mortuusars.exposure.core.image.Image;

import java.io.File;

public interface ImageFileLoader {
    Result<Image> load(File file);

    static ImageFileLoader chooseFitting(File file) {
        return new BufferedImageFileLoader();
//        return Files.getFileExtension(file.toString()).equalsIgnoreCase("png")
//                ? fallback(new NativeImageFileLoader(), new BufferedImageFileLoader())
//                : new BufferedImageFileLoader();
    }

    static ImageFileLoader fallback(ImageFileLoader main, ImageFileLoader fallback) {
        return file -> {
            Result<Image> mainResult = main.load(file);
            return mainResult.isSuccessful() ? mainResult : fallback.load(file);
        };
    }
}
