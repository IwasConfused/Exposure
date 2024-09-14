package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.warehouse.client.ClientsideExposureUploader;
import io.github.mortuusars.exposure.warehouse.client.ClientsideExposureCache;
import io.github.mortuusars.exposure.warehouse.client.ClientsideExposureReceiver;
import io.github.mortuusars.exposure.item.*;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ExposureClient {
    private static final ExposureRenderer exposureRenderer = new ExposureRenderer();

    private static ClientsideExposureCache exposureCache = new ClientsideExposureCache();
    private static ClientsideExposureUploader exposureSender;
    private static ClientsideExposureReceiver exposureReceiver;

    @Nullable
    private static KeyMapping openCameraControlsKey = null;

    public static void init() {
        exposureCache = new ClientsideExposureCache();
        exposureSender = new ClientsideExposureUploader();
        exposureReceiver = new ClientsideExposureReceiver(exposureCache);

        registerItemModelProperties();
    }

    public static ClientsideExposureCache exposureCache() {
        return exposureCache;
    }
    public static ClientsideExposureUploader exposureUploader() {
        return exposureSender;
    }
    public static ClientsideExposureReceiver exposureReceiver() {
        return exposureReceiver;
    }

    public static ExposureData getOrQuery(String exposureId) {
        return exposureCache().getOrQueryAndEmpty(exposureId);
    }

    public static ExposureRenderer exposureRenderer() {
        return exposureRenderer;
    }

    public static void registerKeymappings(Function<KeyMapping, KeyMapping> registerFunction) {
        KeyMapping keyMapping = new KeyMapping("key.exposure.camera_controls",
                InputConstants.UNKNOWN.getValue(), "category.exposure");

        openCameraControlsKey = registerFunction.apply(keyMapping);
    }

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
        ItemProperties.register(Exposure.Items.INTERPLANAR_PROJECTOR.get(), Exposure.resource("active"),
                (stack, clientLevel, livingEntity, seed) -> stack.has(DataComponents.CUSTOM_NAME) ? 1f : 0f);
    }

    public static KeyMapping getCameraControlsKey() {
        Preconditions.checkState(openCameraControlsKey != null,
                "Viewfinder Controls key mapping was not registered");

        return openCameraControlsKey.isUnbound() ? Minecraft.getInstance().options.keyShift : openCameraControlsKey;
    }

    public static boolean isShaderActive() {
        return Minecraft.getInstance().gameRenderer.currentEffect() != null && Minecraft.getInstance().gameRenderer.effectActive;
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
}
