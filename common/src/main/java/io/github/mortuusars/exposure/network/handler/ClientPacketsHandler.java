package io.github.mortuusars.exposure.network.handler;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.task.ExposureRetrieveTask;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplate;
import io.github.mortuusars.exposure.client.image.TrichromeImage;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.image.processor.Process;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.capture.saving.PalettedExposureUploader;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.CaptureProperties;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import io.github.mortuusars.exposure.core.cycles.task.Result;
import io.github.mortuusars.exposure.core.warehouse.ExposureData;
import io.github.mortuusars.exposure.client.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPacketsHandler {
    public static void applyShader(ApplyShaderS2CP packet) {
        if (packet.shouldRemove()) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        } else {
            Minecraft.getInstance().gameRenderer.loadEffect(packet.shaderLocation());
        }
    }

    //TODO: Use CaptureData
    public static void exposeScreenshot(String exposureId, int size, float brightnessStops) {
        throw new NotImplementedException();

//        if (size <= 0 || size > 2048) {
//            LOGGER.error("Cannot expose a screenshot: size '{}' is invalid. Should be larger than 0.", size);
//            return;
//        }

//        executeOnMainThread(() -> {
//            Cycles.enqueue(Capture.of(Capture.screenshot(),
//                            CaptureActions.hideGui(),
//                            CaptureActions.forceRegularOrSelfieCamera(),
//                            CaptureActions.disablePostEffect(),
//                            CaptureActions.modifyGamma(brightnessStops))
//                    .handleErrorAndGetResult()
//                    .thenAsync(Process.with(
//                            Processor.Crop.SQUARE,
//                            Processor.Resize.to(size),
//                            Processor.brightness(brightnessStops)))
//                    .thenAsync(image -> {
//                        PalettizedImage palettizedImage = ImagePalettizer.DITHERED_MAP_COLORS.palettize(image, ColorPalette.MAP_COLORS);
//                        image.close();
//                        return palettizedImage;
//                    })
//                    .acceptAsync(new ImageUploader(identifier)::upload));
//        });
    }

    public static void loadExposure(String id, String filePath, int size, boolean dither) {
        LocalPlayer player = Minecrft.player();

        if (StringUtil.isNullOrEmpty(filePath)) {
            Exposure.LOGGER.error("Cannot load exposure: filePath is null or empty.");
            return;
        }

        Holder<ColorPalette> colorPalette = ColorPalettes.get(Minecrft.registryAccess(), ColorPalettes.DEFAULT);

        ExposureClient.cycles().enqueueTask(Capture.of(Capture.file(filePath))
                .handleErrorAndGetResult()
                .thenAsync(Process.with(
                        Processor.Crop.SQUARE_CENTER,
                        Processor.Resize.to(size)))
                .thenAsync(image -> ImagePalettizer.palettizeAndClose(image, colorPalette.value(), dither))
                .then(image -> new ExposureData(image.width(), image.height(), image.pixels(), colorPalette.unwrapKey().orElseThrow().location(),
                        new ExposureData.Tag(ExposureType.COLOR, player.getScoreboardName(),
                                UnixTimestamp.Seconds.now(), true, false)))
                .accept(image -> PalettedExposureUploader.upload(id, image)));
    }

    public static void showExposure(ShowExposureCommandS2CP packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            Exposure.LOGGER.error("Cannot show exposures. Player is null.");
            return;
        }

        List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>(packet.frames().stream().map(frame -> {
            ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            photographStack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);
            return new ItemAndStack<PhotographItem>(photographStack);
        }).toList());

        Collections.reverse(photographs);

        @Nullable Screen screen = packet.negative() ? new NegativeExposureScreen(photographs) : new PhotographScreen(photographs);

        Minecraft.getInstance().setScreen(screen);
    }

    public static void clearRenderingCache() {
        ExposureClient.imageRenderer().clearCache();
        ExposureClient.renderedExposures().clearCache();
    }

    public static void exposureDataChanged(ExposureDataChangedS2CP packet) {
        ExposureClient.exposureStore().refresh(packet.id());
        ExposureClient.imageRenderer().clearCacheOf(packet.id());
        ExposureClient.renderedExposures().clearCacheOf(packet.id());
    }

    public static void createChromaticExposure(CreateChromaticExposureS2CP packet) {
        if (packet.id().isEmpty()) {
            Exposure.LOGGER.error("Cannot create chromatic exposure: identifier is empty.");
            return;
        }

        if (packet.layers().size() != 3) {
            Exposure.LOGGER.error("Cannot create chromatic exposure: 3 layers required. Provided: '{}'.", packet.layers().size());
            return;
        }

        Holder<ColorPalette> colorPalette = ColorPalettes.get(Minecrft.registryAccess(), ColorPalettes.DEFAULT);

        ExposureClient.cycles().addParallelTask(new ExposureRetrieveTask(packet.layers(), 20_000)
                .then(Result::unwrap)
                .then(layers -> new TrichromeImage(layers.get(0), layers.get(1), layers.get(2)))
                .thenAsync(img -> ImagePalettizer.palettizeAndClose(img, colorPalette.value(), true))
                .then(img -> new ExposureData(img.width(), img.height(), img.pixels(), colorPalette.unwrapKey().orElseThrow().location(),
                        new ExposureData.Tag(ExposureType.COLOR, Minecrft.player().getScoreboardName(),
                                UnixTimestamp.Seconds.now(), false, false)))
                .accept(exposure -> PalettedExposureUploader.upload(packet.id(), exposure)));
    }

    public static void startCapture(StartCaptureS2CP packet) {
        Task<?> captureTask = CaptureTemplates.getOrThrow(packet.templateId()).createTask(packet.captureProperties());
        ExposureClient.cycles().enqueueTask(captureTask);
    }

    public static void startDebugRGBCapture(StartDebugRGBCaptureS2CP packet) {
        CaptureTemplate template = CaptureTemplates.getOrThrow(packet.templateId());

        for (CaptureProperties captureProperties : packet.captureProperties()) {
            Task<?> captureTask = template.createTask(captureProperties);
            ExposureClient.cycles().enqueueTask(captureTask);
        }
    }
}
