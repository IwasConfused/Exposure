
package io.github.mortuusars.exposure.client.capture.task;

import com.google.common.io.Files;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.capture.task.file.ImageFileLoader;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.cycles.task.Result;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class FileCaptureTask extends Task<Result<Image>> {
    public static final String ERROR_PATH_EMPTY = "gui.exposure.capture.file.error.path_empty";
    public static final String ERROR_PATH_INVALID = "gui.exposure.capture.file.error.path_invalid";
    public static final String ERROR_NO_EXTENSION = "gui.exposure.capture.file.error.no_extension";
    public static final String ERROR_PATH_IS_DIRECTORY = "gui.exposure.capture.file.error.path_is_directory";
    public static final String ERROR_FILE_DOES_NOT_EXIST = "gui.exposure.capture.file.error.file_does_not_exist";
    public static final String ERROR_CANNOT_READ = "gui.exposure.capture.file.error.cannot_read";
    public static final String ERROR_NOT_SUPPORTED = "gui.exposure.capture.file.error.not_supported";

    protected final String filePath;

    public FileCaptureTask(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        return CompletableFuture.supplyAsync(() -> {
            Exposure.LOGGER.info("Attempting to load image from file: '{}'", filePath);

            Result<File> result = findFileWithExtension(filePath);

            if (result.isError()) {
                return result.remapError();
            }

            result = validateFilepath(result.getValue());

            if (result.isError()) {
                return result.remapError();
            }

            File file = result.getValue();

            Exposure.LOGGER.info("Reading image from file: '{}'", file);

            return ImageFileLoader.chooseFitting(file).load(file);
        });
    }

    private static Result<File> validateFilepath(File file) {
        String filepath = file.getPath();

        if (StringUtil.isNullOrEmpty(filepath)) {
            return Result.error(ERROR_PATH_EMPTY);
        }

        if (file.isDirectory()) {
            return Result.error(ERROR_PATH_IS_DIRECTORY);
        }

        String extension = Files.getFileExtension(filepath);
        if (StringUtil.isNullOrEmpty(extension)) {
            return Result.error(ERROR_NO_EXTENSION);
        }

        if (!file.exists()) {
            return Result.error(ERROR_FILE_DOES_NOT_EXIST);
        }

        return Result.success(file);
    }

    /**
     * If provided filepath is missing an extension - searches for first file
     * in parent directory that matches the name of given file.
     *
     * @return File with extension or error.
     */
    private static Result<File> findFileWithExtension(String filepath) {
        File file = new File(filepath);

        String extension = Files.getFileExtension(filepath);
        if (!StringUtil.isNullOrEmpty(extension)) {
            return Result.success(file);
        }

        @Nullable File parentFile = file.getParentFile();
        if (parentFile == null) {
            return Result.error(ERROR_PATH_INVALID);
        }

        File[] files = parentFile.listFiles();
        if (files == null) {
            return Result.error(ERROR_CANNOT_READ);
        }

        String name = file.getName();
        for (File fileInDirectory : files) {
            if (fileInDirectory.isDirectory()) {
                continue;
            }

            String fileName = Files.getNameWithoutExtension(fileInDirectory.getName());
            if (fileName.equals(name)) {
                return Result.success(fileInDirectory);
            }
        }

        return Result.error(ERROR_FILE_DOES_NOT_EXIST);
    }
}
