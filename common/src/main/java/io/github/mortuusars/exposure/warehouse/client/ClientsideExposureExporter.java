package io.github.mortuusars.exposure.warehouse.client;

public class ClientsideExposureExporter /*extends ExposureExporter<ClientsideExposureExporter>*/ {
//    public ClientsideExposureExporter(String name) {
//        super(name);
//    }
//
//    @Override
//    public boolean export(ExposureData savedData) {
//        try (NativeImage image = convertToNativeImage(savedData)) {
//            @Nullable File writtenFile = writeImageToFile(image);
//            if (writtenFile == null) {
//                return false;
//            }
//
//            long timestamp = savedData.getTimestamp();
//            trySetFileCreationDate(writtenFile.getAbsolutePath(), timestamp);
//            Exposure.LOGGER.info("Exposure saved: {}", writtenFile);
//            return true;
//        } catch (Exception e) {
//            Exposure.LOGGER.error("Cannot convert exposure pixels to NativeImage: {}", e.toString());
//            return false;
//        }
//    }
//
//    protected @Nullable File writeImageToFile(NativeImage image) {
//        // Existing file would be overwritten
//        try {
//            String filepath = getFolder() + "/" + (getWorldSubfolder() != null ? getWorldSubfolder() + "/" : "") + getName() + ".png";
//            File outputFile = new File(filepath);
//            boolean ignored = outputFile.getParentFile().mkdirs();
//            image.writeToFile(outputFile);
//            return outputFile;
//        } catch (IOException e) {
//            Exposure.LOGGER.error("Failed to save exposure to file: {}", e.toString());
//            return null;
//        }
//    }
//
//    @NotNull
//    protected NativeImage convertToNativeImage(ExposureData savedData) {
//        int width = savedData.getWidth();
//        int height = savedData.getHeight();
//        NativeImage image = new NativeImage(width, height, false);
//        PixelModifier modifier = getModifier();
//
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                int ABGR = MapColor.getColorFromPackedId(savedData.getPixel(x, y)); // Mojang returns BGR color
//                ABGR = modifier.modifyPixel(ABGR);
//                image.setPixelRGBA(x, y, ABGR);
//            }
//        }
//
//        if (getSize() != ExposureSize.X1) {
//            int resultWidth = image.getWidth() * getSize().getMultiplier();
//            int resultHeight = image.getHeight() * getSize().getMultiplier();
//            NativeImage resized = resize(image, 0, 0, image.getWidth(), image.getHeight(), resultWidth, resultHeight);
//            image.close();
//            image = resized;
//        }
//
//        return image;
//    }
//
//    protected NativeImage resize(NativeImage source, int sourceX, int sourceY, int sourceWidth, int sourceHeight,
//                                              int resultWidth, int resultHeight) {
//        NativeImage result = new NativeImage(source.format(), resultWidth, resultHeight, false);
//
//        for (int x = 0; x < resultWidth; x++) {
//            float ratioX = x / (float)resultWidth;
//            int sourcePosX = (int)(sourceX + (sourceWidth * ratioX));
//
//            for (int y = 0; y < resultHeight; y++) {
//                float ratioY = y / (float)resultHeight;
//                int sourcePosY = (int)(sourceY + (sourceHeight * ratioY));
//                int color = source.getPixelRGBA(sourcePosX, sourcePosY);
//                result.setPixelRGBA(x, y, color);
//            }
//        }
//
//        return result;
//    }
}
