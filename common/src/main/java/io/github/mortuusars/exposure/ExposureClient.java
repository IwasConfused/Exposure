package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.Censor;
import io.github.mortuusars.exposure.client.image.*;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.ImageRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.warehouse.client.ExposureStore;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.item.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

public class ExposureClient {
    private static final ExposureStore EXPOSURE_STORE = new ExposureStore();

    private static final ImageRenderer IMAGE_RENDERER = new ImageRenderer();
    private static final PhotographRenderer PHOTOGRAPH_RENDERER = new PhotographRenderer();

    private static boolean isIrisOrOculusInstalled;

    public static void init() {
        registerItemModelProperties();
        isIrisOrOculusInstalled = PlatformHelper.isModLoaded("iris") || PlatformHelper.isModLoaded("oculus");
    }

    public static ExposureStore exposureStore() {
        return EXPOSURE_STORE;
    }

    public static ImageRenderer imageRenderer() {
        return IMAGE_RENDERER;
    }

    public static PhotographRenderer photographRenderer() {
        return PHOTOGRAPH_RENDERER;
    }

    // --

    public static boolean isIrisOrOculusInstalled() {
        return isIrisOrOculusInstalled;
    }

    // --

    public static RenderableImage createRenderableExposureImage(ExposureFrame frame) {
        RenderableImage image = createRawRenderableExposureImage(frame.identifier());
        return Censor.isAllowedToRender(frame)
                ? image
                : image.processWith(Processor.CENSORED);
    }

    public static RenderableImage createRawRenderableExposureImage(ExposureIdentifier identifier) {
        return identifier.map(
                id -> ExposureClient.exposureStore().getOrRequest(id).map(
                        exposure -> RenderableImage.fromExposure(exposure, id),
                        RenderableImage.EMPTY),
                ResourceImage::getOrCreate);
    }

    // --

    private static void registerItemModelProperties() {
        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_state"), CameraItemClientExtensions::itemPropertyFunction);
        ItemProperties.register(Exposure.Items.CHROMATIC_SHEET.get(), Exposure.resource("channels"), (stack, clientLevel, livingEntity, seed) ->
                stack.getItem() instanceof ChromaticSheetItem chromaticSheet ?
                        chromaticSheet.getLayers(stack).size() / 10f : 0f);
        ItemProperties.register(Exposure.Items.STACKED_PHOTOGRAPHS.get(), Exposure.resource("count"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem ?
                                stackedPhotographsItem.getPhotographs(stack).size() / 100f : 0f);
        ItemProperties.register(Exposure.Items.ALBUM.get(), Exposure.resource("photos"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof AlbumItem albumItem ? albumItem.getPhotographsCount(stack) / 100f : 0f);
        ItemProperties.register(Exposure.Items.INTERPLANAR_PROJECTOR.get(), Exposure.resource("projector_active"),
                (stack, clientLevel, livingEntity, seed) -> stack.has(DataComponents.CUSTOM_NAME) ? 1f : 0f);
    }

    public static class Models {
        public static final ModelResourceLocation CAMERA_GUI =
                new ModelResourceLocation(Exposure.resource("camera_gui"), "standalone");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_SMALL =
                new ModelResourceLocation(Exposure.resource("photograph_frame_small"), "standalone");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_SMALL_STRIPPED =
                new ModelResourceLocation(Exposure.resource("photograph_frame_small_stripped"), "standalone");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_MEDIUM =
                new ModelResourceLocation(Exposure.resource("photograph_frame_medium"), "standalone");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_MEDIUM_STRIPPED =
                new ModelResourceLocation(Exposure.resource("photograph_frame_medium_stripped"), "standalone");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_LARGE =
                new ModelResourceLocation(Exposure.resource("photograph_frame_large"), "standalone");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_LARGE_STRIPPED =
                new ModelResourceLocation(Exposure.resource("photograph_frame_large_stripped"), "standalone");
    }

    public static class Textures {
        public static final ResourceLocation EMPTY = Exposure.resource("textures/empty.png");

        public static class Photograph {
            public static final ResourceLocation REGULAR_PAPER = Exposure.resource("textures/photograph/photograph.png");
            public static final ResourceLocation REGULAR_ALBUM_PAPER = Exposure.resource("textures/photograph/photograph_album.png");

            public static final ResourceLocation AGED_PAPER = Exposure.resource("textures/photograph/aged_photograph.png");
            public static final ResourceLocation AGED_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_overlay.png");
            public static final ResourceLocation AGED_ALBUM_PAPER = Exposure.resource("textures/photograph/aged_photograph_album.png");
            public static final ResourceLocation AGED_ALBUM_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_album_overlay.png");
        }
    }
}
