package io.github.mortuusars.exposure.client.snapshot.saving;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.snapshot.converter.Converter;
import io.github.mortuusars.exposure.warehouse.PalettedImage;
import org.slf4j.Logger;

import java.io.File;

public class FileSaver implements Saver {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final File file;

    public FileSaver(File file) {
        this.file = file;
    }

    @Override
    public void save(PalettedImage palettedImage) {
        try (NativeImage image = Converter.convertBack(palettedImage)) {
            boolean ignored = file.getParentFile().mkdirs();
            image.writeToFile(file);
        }
        catch (Exception e) {
            LOGGER.error("Failed to save image to file: {}", e.toString());
        }
    }
}
