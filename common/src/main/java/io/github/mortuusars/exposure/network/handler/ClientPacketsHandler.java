package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.camera.capture.*;
import io.github.mortuusars.exposure.client.snapshot.SnapShot;
import io.github.mortuusars.exposure.client.snapshot.capturing.Capture;
import io.github.mortuusars.exposure.client.snapshot.capturing.action.CaptureActions;
import io.github.mortuusars.exposure.client.snapshot.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.snapshot.processing.Process;
import io.github.mortuusars.exposure.client.snapshot.processing.Processor;
import io.github.mortuusars.exposure.client.snapshot.saving.ImageUploader;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.client.ClientTrichromeFinalizer;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.data.lenses.Lenses;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.warehouse.PalettizedImage;
import io.github.mortuusars.exposure.client.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ClientPacketsHandler {
    public static void applyShader(ApplyShaderS2CP packet) {
        executeOnMainThread(() -> {
            if (packet.shouldRemove()) {
                Minecraft.getInstance().gameRenderer.shutdownEffect();
            } else {
                Minecraft.getInstance().gameRenderer.loadEffect(packet.shaderLocation());
            }
        });
    }

    public static void exposeScreenshot(ExposureIdentifier identifier, int size, float brightnessStops) {
        Preconditions.checkState(size > 0 && size <= 2048, size + " size is invalid. Should be larger than 0.");

        executeOnMainThread(() -> {
            SnapShot.enqueue(Capture.of(Capture.screenshot(), CaptureActions.of(
                            CaptureActions.hideGui(),
                            CaptureActions.forceRegularOrSelfieCamera(),
                            CaptureActions.disablePostEffect(),
                            CaptureActions.modifyGamma(brightnessStops)))
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
        @Nullable LocalPlayer player = Minecraft.getInstance().player;
        Preconditions.checkState(player != null, "Cannot load exposure: player is null.");
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(filePath), "Cannot load exposure: filePath is null or empty.");

        executeOnMainThread(() -> {
            SnapShot.enqueue(Capture.of(Capture.file(filePath))
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

    public static void startExposure(StartExposureS2CP packet) {
        executeOnMainThread(() -> {
            LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
            CameraAccessor cameraAccessor = packet.cameraAccessor();
            CameraClient.handleExposureStart(player, cameraAccessor, packet.identifier(), packet.flashHasFired());
        });
    }

    public static void showExposure(ShowExposureCommandS2CP packet) {
        executeOnMainThread(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                Exposure.LOGGER.error("Cannot show exposures. Player is null.");
                return;
            }

            boolean negative = packet.negative();

            @Nullable Screen screen;

            if (packet.showLatest()) {
                screen = createLatestScreen(player, negative);
            } else {
                if (negative) {
                    screen = new NegativeExposureScreen(List.of(packet.identifier()));
                } else {
                    ExposureFrame frame = ExposureFrame.EMPTY.toMutable().setIdentifier(packet.identifier()).toImmutable();

                    ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
                    stack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);

                    screen = new PhotographScreen(List.of(new ItemAndStack<>(stack)));
                }
            }

            if (screen != null)
                Minecraft.getInstance().setScreen(screen);
        });
    }

    private static @Nullable Screen createLatestScreen(Player player, boolean negative) {
        List<ExposureFrame> latestFrames = CapturedFramesHistory.get();

        if (latestFrames.isEmpty()) {
            player.displayClientMessage(Component.translatable("command.exposure.show.latest.error.no_exposures"), false);
            return null;
        }

        if (negative) {
            List<ExposureIdentifier> exposures = latestFrames.stream().map(ExposureFrame::identifier).toList();
            return new NegativeExposureScreen(exposures);
        } else {
            List<ItemAndStack<PhotographItem>> photographs = latestFrames.stream().map(frame -> {
                ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
                stack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);
                return new ItemAndStack<PhotographItem>(stack);
            }).toList();

            return new PhotographScreen(photographs);
        }
    }

    public static void clearRenderingCache() {
        executeOnMainThread(() -> ExposureClient.imageRenderer().clearCache());
    }

    public static void syncLensesData(SyncLensesDataS2CP packet) {
        executeOnMainThread(() -> Lenses.reload(packet.lenses()));
    }

    public static void waitForExposureChange(WaitForExposureChangeS2CP packet) {
        executeOnMainThread(() -> ExposureClient.exposureCache().putOnWaitingList(packet.identifier()));
    }

    public static void onExposureChanged(ExposureChangedS2CP packet) {
        executeOnMainThread(() -> {
            ExposureClient.exposureCache().remove(packet.identifier());
            ExposureClient.imageRenderer().clearCacheOf(identifier -> identifier.matches(packet.identifier()));
        });
    }

    public static void onFrameAdded(OnFrameAddedS2CP packet) {
        executeOnMainThread(() -> CapturedFramesHistory.add(packet.frame()));
    }

    public static void createChromaticExposure(CreateChromaticExposureS2CP packet) {
        Preconditions.checkState(!packet.identifier().isEmpty(),
                "Cannot create chromatic exposure: identifier is empty.");
        Preconditions.checkState(packet.layers().size() == 3,
                "Cannot create chromatic exposure: 3 layers is required. %s is provided", packet.layers().size());
        executeOnMainThread(() -> ClientTrichromeFinalizer.finalizeTrichrome(packet.identifier(), packet.layers()));
    }

    private static void executeOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }
}
