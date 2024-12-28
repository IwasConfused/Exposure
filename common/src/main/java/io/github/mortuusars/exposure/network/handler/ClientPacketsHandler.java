package io.github.mortuusars.exposure.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.cycles.Cycles;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.capture.processing.Process;
import io.github.mortuusars.exposure.client.capture.processing.Processor;
import io.github.mortuusars.exposure.client.capture.saving.ImageUploader;
import io.github.mortuusars.exposure.core.CaptureClientData;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.client.ClientTrichromeFinalizer;
import io.github.mortuusars.exposure.core.frame.CaptureData;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.data.lenses.Lenses;
import io.github.mortuusars.exposure.client.image.PalettizedImage;
import io.github.mortuusars.exposure.client.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.network.packet.server.ActiveCameraAddFrameC2SP;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPacketsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void applyShader(ApplyShaderS2CP packet) {
        executeOnMainThread(() -> {
            if (packet.shouldRemove()) {
                Minecraft.getInstance().gameRenderer.shutdownEffect();
            } else {
                Minecraft.getInstance().gameRenderer.loadEffect(packet.shaderLocation());
            }
        });
    }

    //TODO: Use CaptureData
    public static void exposeScreenshot(ExposureIdentifier identifier, int size, float brightnessStops) {
        if (size <= 0 || size > 2048) {
            LOGGER.error("Cannot expose a screenshot: size '{}' is invalid. Should be larger than 0.", size);
            return;
        }

        executeOnMainThread(() -> {
            Cycles.enqueue(Capture.of(Capture.screenshot(),
                            CaptureActions.hideGui(),
                            CaptureActions.forceRegularOrSelfieCamera(),
                            CaptureActions.disablePostEffect(),
                            CaptureActions.modifyGamma(brightnessStops))
                    .handleErrorAndGetResult()
                    .thenAsync(Process.with(
                            Processor.Crop.SQUARE,
                            Processor.Resize.to(size),
                            Processor.brightness(brightnessStops)))
                    .thenAsync(image -> {
                        PalettizedImage palettizedImage = ImagePalettizer.DITHERED_MAP_COLORS.palettize(image, ColorPalette.MAP_COLORS);
                        image.close();
                        return palettizedImage;
                    })
                    .acceptAsync(new ImageUploader(identifier)::upload));
        });
    }

    public static void loadExposure(ExposureIdentifier identifier, String filePath, int size, boolean dither) {
        LocalPlayer player = Minecrft.player();

        if (StringUtil.isNullOrEmpty(filePath)) {
            LOGGER.error("Cannot load exposure: filePath is null or empty.");
            return;
        }

        executeOnMainThread(() -> {
            Cycles.enqueue(Capture.of(Capture.file(filePath))
                    .handleErrorAndGetResult()
                    .thenAsync(Process.with(
                            Processor.Crop.SQUARE,
                            Processor.Resize.to(size)))
                    .thenAsync(image -> {
                        PalettizedImage palettizedImage = (dither
                                ? ImagePalettizer.DITHERED_MAP_COLORS
                                : ImagePalettizer.NEAREST_MAP_COLORS).palettize(image, ColorPalette.MAP_COLORS);
                        image.close();
                        return palettizedImage;
                    })
                    .acceptAsync(new ImageUploader(identifier)::upload)
                    .acceptAsync(v -> player.displayClientMessage(
                            Component.translatable("command.exposure.load_from_file.success", identifier)
                                    .withStyle(ChatFormatting.GREEN), false)));
        });
    }

    public static void showExposure(ShowExposureCommandS2CP packet) {
        executeOnMainThread(() -> {
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
        });
    }

    public static void clearRenderingCache() {
        executeOnMainThread(() -> ExposureClient.imageRenderer().clearCache());
    }

    public static void syncLensesData(SyncLensesDataS2CP packet) {
        executeOnMainThread(() -> Lenses.reload(packet.lenses()));
    }

    public static void waitForExposureChange(WaitForExposureChangeS2CP packet) {
//        executeOnMainThread(() -> ExposureClient.exposureCache().putOnWaitingList(packet.identifier()));
    }

    public static void exposureDataChanged(ExposureDataChangedS2CP packet) {
        executeOnMainThread(() -> {
            ExposureClient.exposureStore().refresh(packet.identifier());
//            ExposureClient.exposureCache().remove(packet.identifier());
            ExposureClient.imageRenderer().clearCacheOf(identifier -> identifier.matches(packet.identifier()));
        });
    }

    public static void createChromaticExposure(CreateChromaticExposureS2CP packet) {
        if (packet.identifier().isEmpty()) {
            LOGGER.error("Cannot create chromatic exposure: identifier is empty.");
            return;
        }

        if (packet.layers().size() != 3) {
            LOGGER.error("Cannot create chromatic exposure: 3 layers required. Provided: '{}'.", packet.layers().size());
            return;
        }

        executeOnMainThread(() -> ClientTrichromeFinalizer.finalizeTrichrome(packet.identifier(), packet.layers()));
    }

    private static void executeOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }

    public static void startCapture(StartCaptureS2CP packet) {
        LocalPlayer player = Minecrft.player();
        CaptureData data = packet.captureData();

        executeOnMainThread(() -> {
            player.ifActiveExposureCameraPresent((item, stack) -> {
                CaptureClientData clientSideFrameData = item.getClientSideFrameData(data.photographer(), stack);
                Packets.sendToServer(new ActiveCameraAddFrameC2SP(data.photographer(), clientSideFrameData));

                Task<?> captureTask = CaptureTemplates.getOrThrow(item).createTask(player, data.identifier(), data);
                Cycles.enqueue(captureTask);
            }, () -> LOGGER.error("Cannot start capture: no active camera."));
        });
    }
}
