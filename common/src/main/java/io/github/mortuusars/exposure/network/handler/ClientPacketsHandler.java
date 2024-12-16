package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.client.capture.CaptureTemplates;
import io.github.mortuusars.exposure.client.snapshot.SnapShot;
import io.github.mortuusars.exposure.client.snapshot.capturing.Capture;
import io.github.mortuusars.exposure.client.snapshot.capturing.action.CaptureActions;
import io.github.mortuusars.exposure.client.snapshot.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.snapshot.processing.Process;
import io.github.mortuusars.exposure.client.snapshot.processing.Processor;
import io.github.mortuusars.exposure.client.snapshot.saving.ImageUploader;
import io.github.mortuusars.exposure.core.ExposureFrameClientData;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.client.ClientTrichromeFinalizer;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.NewCameraInHand;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClientPacketsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void setInHandActiveCamera(SetActiveInHandCameraS2CP packet) {
        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level, "level");
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player, "player");

        Entity owner = level.getEntities().get(packet.ownerUUID());
        if (owner instanceof LivingEntity livingOwner) {
            player.setActiveCamera(new NewCameraInHand(livingOwner, packet.hand()));
        } else {
            LOGGER.error("Cannot set active camera in hand: owner should be a LivingEntity. Got '{}' instead.", owner);
        }
    }

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
            CameraAccessor<?> cameraAccessor = packet.cameraAccessor();
            CameraClient.handleExposureStart(player, cameraAccessor, packet.identifier(), packet.flashHasFired());
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
        executeOnMainThread(() -> ExposureClient.exposureCache().putOnWaitingList(packet.identifier()));
    }

    public static void onExposureChanged(ExposureChangedS2CP packet) {
        executeOnMainThread(() -> {
            ExposureClient.exposureCache().remove(packet.identifier());
            ExposureClient.imageRenderer().clearCacheOf(identifier -> identifier.matches(packet.identifier()));
        });
    }

    public static void onFrameAdded(OnFrameAddedS2CP packet) {
//        executeOnMainThread(() -> CapturedFramesHistory.add(packet.frame()));
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

    public static void startCapture(StartCaptureS2CP packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        Preconditions.checkNotNull(player, "Minecraft.getInstance().player");

        CaptureData data = packet.captureData();

        executeOnMainThread(() -> {
            player.getActiveCamera().ifPresentOrElse(camera -> {
                ExposureFrameClientData clientSideFrameData = camera.getItem().getClientSideFrameData(player, camera.getItemStack());
                Packets.sendToServer(new ActiveCameraAddFrameC2SP(data.cameraHolderID(), clientSideFrameData));

                Task<?> captureTask = CaptureTemplates.getOrThrow(camera.getItem()).createTask(player, data);
                SnapShot.enqueue(captureTask);
            }, () -> LOGGER.error("Cannot start capture: not active camera."));
        });
    }
}
