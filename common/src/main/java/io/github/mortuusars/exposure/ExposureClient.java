package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.task.ClearStaleRenderedImagesIndefiniteTask;
import io.github.mortuusars.exposure.client.RenderedExposures;
import io.github.mortuusars.exposure.client.camera.viewfinder.*;
import io.github.mortuusars.exposure.client.capture.template.CameraCaptureTemplate;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.capture.template.SingleChannelCaptureTemplate;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.render.image.ImageRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyles;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.util.cycles.Cycles;
import io.github.mortuusars.exposure.client.ExposureStore;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.CameraItem;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

public class ExposureClient {
    private static final Cycles CYCLES = new Cycles();
    private static final ExposureStore EXPOSURE_STORE = new ExposureStore();
    private static final RenderedExposures RENDERED_EXPOSURES = new RenderedExposures();
    private static final ImageRenderer IMAGE_RENDERER = new ImageRenderer();
    private static final PhotographRenderer PHOTOGRAPH_RENDERER = new PhotographRenderer();

    private static boolean isIrisOrOculusInstalled;

    public static void init() {
        ViewfinderRegistry.register(Exposure.Items.CAMERA.get(), camera ->
                new Viewfinder(camera, ViewfinderZoom::new, ViewfinderOverlay::new, ViewfinderShader::new, ViewfinderCameraControlsScreen::new));

        CaptureTemplates.register(CaptureType.CAMERA, new CameraCaptureTemplate());
        CaptureTemplates.register(CaptureType.DEBUG_RGB, new SingleChannelCaptureTemplate());

        PhotographStyles.register(PhotographType.REGULAR, PhotographStyle.REGULAR);
        PhotographStyles.register(PhotographType.AGED, new PhotographStyle(
                ExposureClient.Textures.Photograph.AGED_PAPER,
                ExposureClient.Textures.Photograph.AGED_OVERLAY,
                ExposureClient.Textures.Photograph.AGED_ALBUM_PAPER,
                ExposureClient.Textures.Photograph.AGED_ALBUM_OVERLAY,
                Processor.AGED));

        cycles().addParallelTask(new ClearStaleRenderedImagesIndefiniteTask());

        registerItemModelProperties();
        isIrisOrOculusInstalled = PlatformHelper.isModLoaded("iris") || PlatformHelper.isModLoaded("oculus");
    }

    public static Cycles cycles() {
        return CYCLES;
    }

    public static ExposureStore exposureStore() {
        return EXPOSURE_STORE;
    }

    public static RenderedExposures renderedExposures() {
        return RENDERED_EXPOSURES;
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

    private static void registerItemModelProperties() {
        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_state"), (stack, level, entity, seed) -> {
            if (!(stack.getItem() instanceof CameraItem cameraItem) || !cameraItem.isActive(stack)) {
                return 0f;
            }

            if (cameraItem.isInSelfieMode(stack)) {
                // Longer selfie stick for current player (to not obscure the view) and regular for everyone else
                return entity == Minecrft.player() ? 0.2f : 0.3f;
            }

            return 0.1f;
        });
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
