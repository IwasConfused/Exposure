package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.camera.capture.*;
import io.github.mortuusars.exposure.camera.capture.component.BaseComponent;
import io.github.mortuusars.exposure.camera.capture.component.ExposureUploaderComponent;
import io.github.mortuusars.exposure.camera.capture.component.ICaptureComponent;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringColorConverter;
import io.github.mortuusars.exposure.camera.capture.converter.SimpleColorConverter;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.frame.FrameProperties;
import io.github.mortuusars.exposure.client.ClientTrichromeFinalizer;
import io.github.mortuusars.exposure.data.lenses.Lenses;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.warehouse.client.ClientsideExposureExporter;
import io.github.mortuusars.exposure.client.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.core.pixel_modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.util.ClientsideWorldNameGetter;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
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
            if (packet.shaderLocation().getPath().equals("none")) {
                Minecraft.getInstance().gameRenderer.shutdownEffect();
            } else {
                Minecraft.getInstance().gameRenderer.loadEffect(packet.shaderLocation());
            }
        });
    }

    public static void exposeScreenshot(int size) {
        Preconditions.checkState(size > 0, size + " size is invalid. Should be larger than 0.");
        if (size == Integer.MAX_VALUE)
            size = Math.min(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow()
                    .getHeight());

        int finalSize = size;
        executeOnMainThread(() -> {
            String filename = Util.getFilenameFormattedDateTime();
            CompoundTag frameData = new CompoundTag();
            frameData.putString(FrameProperties.ID, filename);
            Capture capture = new ScreenshotCapture()
                    .setSize(finalSize)
                    .cropFactor(1f)
                    .setComponents(
                            new BaseComponent(true),
                            new ClientsideExposureExporter(filename)
                                    .organizeByWorld(Config.Client.EXPOSURE_SAVING_LEVEL_SUBFOLDER.get(),
                                            ClientsideWorldNameGetter::getWorldName)
                                    .withModifier(ExposurePixelModifiers.EMPTY)
                                    .withSize(ExposureSize.X1),
                            new ICaptureComponent() {
                                @Override
                                public void end(Capture capture) {
                                    Exposure.LOGGER.info("Saved exposure screenshot: {}", filename);
                                }
                            })
                    .setConverter(new DitheringColorConverter());
            CaptureManager.enqueue(capture);
        });
    }

    public static void loadExposure(String exposureId, String filePath, int size, boolean dither) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (StringUtil.isNullOrEmpty(exposureId)) {
            if (player == null)
                throw new IllegalStateException("Cannot load exposure: filePath is null or empty and player is null.");
            exposureId = player.getName().getString() + player.level().getGameTime();
        }

        String finalExposureId = exposureId;
        new Thread(() -> {
            Capture capture = new FileCapture(filePath, error -> { if (player != null) player.displayClientMessage(
                            error.getTechnicalTranslation().withStyle(ChatFormatting.RED), false); })
                    .setSize(size)
                    .cropFactor(1f)
                    .setComponents(new ExposureUploaderComponent(finalExposureId))
                    .setConverter(dither ? new DitheringColorConverter() : new SimpleColorConverter());
            CaptureManager.enqueue(capture);

            ExposureFrame frame = ExposureFrame.EMPTY.toMutable().setIdentifier(new ExposureIdentifier(finalExposureId)).toImmutable();
            CapturedFramesHistory.add(frame);

            Exposure.LOGGER.info("Loaded exposure from file '{}' with Id: '{}'.", filePath, finalExposureId);
            Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                    Component.translatable("command.exposure.load_from_file.success", finalExposureId)
                            .withStyle(ChatFormatting.GREEN), false);
        }).start();
    }

    public static void startExposure(StartExposureS2CP packet) {
        executeOnMainThread(() -> {
            LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
            CameraAccessor cameraAccessor = packet.cameraAccessor();
            CameraClient.handleExposureStart(player, cameraAccessor, packet.exposureId(), packet.flashHasFired());
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
        executeOnMainThread(() -> ExposureClient.exposureRenderer().clearData());
    }

    public static void syncLensesData(SyncLensesDataS2CP packet) {
        executeOnMainThread(() -> Lenses.reload(packet.lenses()));
    }

    public static void waitForExposureChange(WaitForExposureChangeS2CP packet) {
        executeOnMainThread(() -> ExposureClient.exposureCache().putOnWaitingList(packet.exposureId()));
    }

    public static void onExposureChanged(ExposureChangedS2CP packet) {
        executeOnMainThread(() -> {
            ExposureClient.exposureCache().remove(packet.exposureId());
            ExposureClient.exposureRenderer().clearDataSingle(packet.exposureId(), true);
        });
    }

    public static void onFrameAdded(OnFrameAddedS2CP packet) {
        executeOnMainThread(() -> CapturedFramesHistory.add(packet.frame()));
    }

    public static void createChromaticExposure(CreateChromaticExposureS2CP packet) {
        executeOnMainThread(() -> ClientTrichromeFinalizer.finalizeTrichrome(packet.red(), packet.green(), packet.blue(), packet.chromaticExposureId()));
    }

    private static void executeOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }
}
