
package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import com.google.common.io.Files;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.file.ImageFileLoader;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class FileCaptureMethod implements CaptureMethod {
    public static final ErrorMessage ERROR_PATH_EMPTY = ErrorMessage.create("gui.exposure.capture.file.error.path_empty");
    public static final ErrorMessage ERROR_PATH_INVALID = ErrorMessage.create("gui.exposure.capture.file.error.path_invalid");
    public static final ErrorMessage ERROR_NO_EXTENSION = ErrorMessage.create("gui.exposure.capture.file.error.no_extension");
    public static final ErrorMessage ERROR_PATH_IS_DIRECTORY = ErrorMessage.create("gui.exposure.capture.file.error.path_is_directory");
    public static final ErrorMessage ERROR_FILE_DOES_NOT_EXIST = ErrorMessage.create("gui.exposure.capture.file.error.file_does_not_exist");
    public static final ErrorMessage ERROR_CANNOT_READ = ErrorMessage.create("gui.exposure.capture.file.error.cannot_read");
    public static final ErrorMessage ERROR_NOT_SUPPORTED = ErrorMessage.create("gui.exposure.capture.file.error.not_supported");

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final String filepath;

    public FileCaptureMethod(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public @NotNull CompletableFuture<TaskResult<Image>> capture() {
        return CompletableFuture.supplyAsync(() -> {
            Either<File, ErrorMessage> file = findFileWithExtension(filepath);

            if (file.right().isPresent()) {
                return TaskResult.error(file.right().get());
            }

            file = validateFilepath(file.left().orElseThrow());

            if (file.right().isPresent()) {
                return TaskResult.error(file.right().get());
            }

            File f = file.left().orElseThrow();

            LOGGER.info("Loading image from file: {}", f);

            return ImageFileLoader.chooseFitting(f).load(f);
        });
    }

    private static Either<File, ErrorMessage> validateFilepath(File file) {
        String filepath = file.getPath();

        if (StringUtil.isNullOrEmpty(filepath)) {
            return Either.right(ERROR_PATH_EMPTY);
        }

        if (file.isDirectory()) {
            return Either.right(ERROR_PATH_IS_DIRECTORY);
        }

        String extension = Files.getFileExtension(filepath);
        if (StringUtil.isNullOrEmpty(extension)) {
            return Either.right(ERROR_NO_EXTENSION);
        }

        //TODO: supported formats
//        List<String> formats = getSupportedFormats();
//        String ext = extension.replace(".", "");
//        if (!formats.contains(ext)) {
//            return Optional.of(ImageLoader.Error.NOT_SUPPORTED);
//        }

        if (!file.exists()) {
            return Either.right(ERROR_FILE_DOES_NOT_EXIST);
        }

        return Either.left(file);
    }

    /**
     * If provided filepath is missing an extension - searches for first file
     * in parent directory that matches the name of given file.
     *
     * @return File with extension or error.
     */
    private static Either<File, ErrorMessage> findFileWithExtension(String filepath) {
        File file = new File(filepath);

        String extension = Files.getFileExtension(filepath);
        if (!StringUtil.isNullOrEmpty(extension)) {
            return Either.left(file);
        }

        @Nullable File parentFile = file.getParentFile();
        if (parentFile == null) {
            return Either.right(ERROR_PATH_INVALID);
        }

        File[] files = parentFile.listFiles();
        if (files == null) {
            return Either.right(ERROR_CANNOT_READ);
        }

        String name = file.getName();
        for (File fileInDirectory : files) {
            if (fileInDirectory.isDirectory()) {
                continue;
            }

            String fileName = Files.getNameWithoutExtension(fileInDirectory.getName());
            if (fileName.equals(name)) {
                return Either.left(fileInDirectory);
            }
        }

        return Either.right(ERROR_FILE_DOES_NOT_EXIST);
    }
}
