package io.github.mortuusars.exposure.data.export;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ServersideExposureExporter extends ExposureExporter<ServersideExposureExporter> {
    public ServersideExposureExporter(String name) {
        super(name);
    }

    @Override
    public boolean export(PalettedExposure exposure) {
        try {
            BufferedImage image = convertToBufferedImage(exposure);
            @Nullable File writtenFile = writeImageToFile(image);
            if (writtenFile == null) {
                return false;
            }

            long timestamp = exposure.getTag().unixTimestamp();
            trySetFileCreationDate(writtenFile.getAbsolutePath(), timestamp);
            Exposure.LOGGER.info("Exposure saved: {}", writtenFile);
            return true;
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot convert exposure pixels to BufferedImage: {}", e.toString());
            return false;
        }
    }

    public @Nullable File writeImageToFile(BufferedImage image) {
        // Existing file would be overwritten
        try {
            String filepath = getFolder() + "/" + (getWorldSubfolder() != null ? getWorldSubfolder() + "/" : "") + getName() + ".png";
            File outputFile = new File(filepath);
            boolean ignored = outputFile.getParentFile().mkdirs();

            if (!ImageIO.write(image, "png", outputFile)) {
                Exposure.LOGGER.error("Exposure was not saved. No appropriate writer has been found.");
                return null;
            }
            return outputFile;
        } catch (IOException e) {
            Exposure.LOGGER.error("Failed to save exposure to file: {}", e.toString());
            return null;
        }
    }

    @NotNull
    protected BufferedImage convertToBufferedImage(PalettedExposure palettedExposure) {
        int width = palettedExposure.getWidth();
        int height = palettedExposure.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        PixelModifier modifier = getModifier();
//
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                int ABGR = MapColor.getColorFromPackedId(palettedExposure.getPixel(x, y)); // Mojang returns BGR color
//                ABGR = modifier.modifyPixel(ABGR);
//                image.setRGB(x, y, Color.ABGRtoARGB(ABGR));
//            }
//        }
//
//        if (getSize() != ExposureSize.X1) {
//            image = resize(image, getSize());
//        }

        return image;
    }

    protected BufferedImage resize(BufferedImage sourceImage, ExposureSize size) {
        int targetWidth = sourceImage.getWidth() * size.getMultiplier();
        int targetHeight = sourceImage.getHeight() * size.getMultiplier();
        Image scaledInstance = sourceImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_FAST);
        BufferedImage outputImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        outputImg.getGraphics().drawImage(scaledInstance, 0, 0, null);
        return outputImg;
    }
}
