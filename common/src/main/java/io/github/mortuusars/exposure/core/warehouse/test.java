package io.github.mortuusars.exposure.core.warehouse;

import io.github.mortuusars.exposure.client.image.Image;

public class test {
    public static void testWarehouse() {
//        ExposureData data = ExposureClient.STORE.getOrRequest(ExposureIdentifier.id("asd")).orElse(ExposureData.EMPTY);
//        ExposureDataImage image = new ExposureDataImage(data);
//
//        image.toRenderable(ImageIdentifier.of(ExposureIdentifier.id("asd")));
    }

    public static class ExposureImage implements Image {
        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getPixelARGB(int x, int y) {
            return 0;
        }
    }
    public static class ExposureTexture implements Image {
        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getPixelARGB(int x, int y) {
            return 0;
        }
    }
}
