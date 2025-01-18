package io.github.mortuusars.exposure.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.task.ExposureRetrieveTask;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplate;
import io.github.mortuusars.exposure.client.image.TrichromeImage;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.processor.Process;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.client.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPacketsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void applyShader(ApplyShaderS2CP packet) {
        if (packet.shouldRemove()) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        } else {
            Minecraft.getInstance().gameRenderer.loadEffect(packet.shaderLocation());
        }
    }

    public static void showExposure(ShowExposureCommandS2CP packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            LOGGER.error("Cannot show exposures. Player is null.");
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
            LOGGER.error("Cannot create chromatic exposure: identifier is empty.");
            return;
        }

        if (packet.layers().size() != 3) {
            LOGGER.error("Cannot create chromatic exposure: 3 layers required. Provided: '{}'.", packet.layers().size());
            return;
        }

        Holder<ColorPalette> colorPalette = ColorPalettes.getDefault(Minecrft.registryAccess());
        ColorPalette palette = colorPalette.value();
        ResourceLocation paletteId = colorPalette.unwrapKey().orElseThrow().location();

        ExposureClient.cycles().addParallelTask(new ExposureRetrieveTask(packet.layers(), 20_000)
                .then(Result::unwrap)
                .thenAsync(layers -> (Image)new TrichromeImage(layers.get(0), layers.get(1), layers.get(2)))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(img -> new ExposureData(img.width(), img.height(), img.pixels(), paletteId,
                        new ExposureData.Tag(ExposureType.COLOR, Minecrft.player().getScoreboardName(),
                                UnixTimestamp.Seconds.now(), false, false)))
                .acceptAsync(ExposureUploader.upload(packet.id())));
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
