
package io.github.mortuusars.exposure.client.capture.method;

import com.google.common.io.Files;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileCaptureMethod implements CaptureMethod {
    public static final ErrorMessage ERROR_PATH_EMPTY = ErrorMessage.create("gui.exposure.capture.file.error.path_empty");
    public static final ErrorMessage ERROR_PATH_INVALID = ErrorMessage.create("gui.exposure.capture.file.error.path_invalid");
    public static final ErrorMessage ERROR_NO_EXTENSION = ErrorMessage.create("gui.exposure.capture.file.error.no_extension");
    public static final ErrorMessage ERROR_PATH_IS_DIRECTORY = ErrorMessage.create("gui.exposure.capture.file.error.path_is_directory");
    public static final ErrorMessage ERROR_FILE_DOES_NOT_EXIST = ErrorMessage.create("gui.exposure.capture.file.error.file_does_not_exist");
    public static final ErrorMessage ERROR_CANNOT_READ = ErrorMessage.create("gui.exposure.capture.file.error.cannot_read");
    public static final ErrorMessage ERROR_NOT_SUPPORTED = ErrorMessage.create("gui.exposure.capture.file.error.not_supported");

    protected final String filepath;

    public FileCaptureMethod(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public @NotNull Either<NativeImage, ErrorMessage> capture() {
        Either<File, ErrorMessage> file = findFileWithExtension(filepath);

        if (file.right().isPresent()) {
            return Either.right(file.right().get());
        }

        file = validateFilepath(file.left().orElseThrow());

        if (file.right().isPresent()) {
            return Either.right(file.right().get());
        }

        try (FileInputStream inputStream = new FileInputStream(file.left().orElseThrow())) {
            return Either.left(NativeImage.read(NativeImage.Format.RGBA, inputStream));
        } catch (IOException e) {
            Exposure.LOGGER.error("Loading image failed: {}", e.toString());
            return Either.right(ERROR_CANNOT_READ);
        }
    }

    public static Either<File, ErrorMessage> validateFilepath(File file) {
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
     * @return File with extension or error.
     */
    public static Either<File, ErrorMessage> findFileWithExtension(String filepath) {
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
