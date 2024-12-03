package io.github.mortuusars.exposure.client.snapshot.capturing.method.file;

import com.google.common.io.Files;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;

import java.io.File;

public interface ImageFileLoader {
    TaskResult<Image> load(File file);

    static ImageFileLoader chooseFitting(File file) {
        return new BufferedImageFileLoader();
//        return Files.getFileExtension(file.toString()).equalsIgnoreCase("png")
//                ? fallback(new NativeImageFileLoader(), new BufferedImageFileLoader())
//                : new BufferedImageFileLoader();
    }

    static ImageFileLoader fallback(ImageFileLoader main, ImageFileLoader fallback) {
        return file -> {
            TaskResult<Image> mainResult = main.load(file);
            return mainResult.isSuccessful() ? mainResult : fallback.load(file);
        };
    }
}
