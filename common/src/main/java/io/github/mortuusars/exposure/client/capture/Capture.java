package io.github.mortuusars.exposure.client.capture;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.action.CompositeAction;
import io.github.mortuusars.exposure.client.capture.task.*;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.cycles.task.Result;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

//TODO: revisit and think about capturing more

// Capture Types:
// Screenshot (background | direct)
// File

// Capture Components:
// HideGui, ForceFirstPerson, DisablePostEffect
// Flash (sound and particles)?
// Interplanar Projection (sound and particles)

// Capture can consist of multiple capture types (with separate components) in fallback system
// Processing is then applied to whatever capture result

// Image Processing: T process(Image image) contains list of pixel processors
// <Composite class to handle multiple at once>
// Crop
// Resize
// Pixel Processing: int process(int ARGB)
// Brightness
// BlackAndWhite | Selective Channel BW

// Converting to paletted image: <Converting can also be done with Image Processors, as a next step after modifications>
// Dithered
// Nearest

    /*

    SnapShot.setup()
        .capture(Capture.takeScreenshot() // Supplier<T>
            .withComponents(
                CaptureComponents.hideGui(),
                CaptureComponents.forceFirstPersonCamera(),
                CaptureComponents.disablePostEffect()
                CaptureComponents.optional(stops != 0, () -> CaptureComponents.modifyGamma(stops)))
            .overridenBy(Capture.fromFile(filePath)
                .onError(error -> player.display(error))) // send packet to brake projector
        .then(Process.with( // Consumer<T>
            ImageProcessors.crop(getCropFactor(), Crop.CENTER_SQUARE))
            ImageProcessors.resize(size)
            ImageProcessors.modifyPixel(
                PixelModifiers.optional(stops != 0, () -> PixelModifiers.brightness(stops)),
                PixelModifiers.optional(filmType == BW, () -> PixelModifiers.blackAndWhite())
            ),
            ImageProcessors.convert(Converter.DITHER)
        )
        .thenConsume(FileSaver.saveToDefaultFolder()) // Consumer<T2>
        .thenConsume(Uploader.toServer(exposureId))   // Consumer<T2>
        .enqueue()

        ))

     */

public class Capture<T> extends Task<Result<T>> {
    public static final String ERROR_TIMED_OUT = "gui.exposure.capture.error.timed_out";
    public static final int TIMEOUT_MS = 10_000; // 10 seconds
    public static final String ERROR_FAILED_GENERIC = "gui.exposure.capture.error.failed";

    protected final Task<Result<T>> capturingTask;
    protected final CaptureAction component;
    protected final CaptureTimer timer;
    protected final CompletableFuture<Result<T>> completableFuture;

    public Capture(Task<Result<T>> capturingTask, CaptureAction component) {
        this.capturingTask = capturingTask;
        this.component = component;
        this.timer = new CaptureTimer(component.requiredDelayTicks())
                .whenStarted(this.component::initialize)
                .onGameTick(this.component::delayTick)
                .whenEnded(() -> {
                    this.component.beforeCapture();
                    capture();
                });
        this.completableFuture = new CompletableFuture<>();
    }

    public CompletableFuture<Result<T>> execute() {
        if (!timer.isRunning()) {
            timer.start();
            setStarted();
        }

        return completableFuture;
    }

    public void tick() {
        capturingTask.tick();
        timer.tick();
    }

    private void capture() {
        capturingTask.execute()
                .completeOnTimeout(Result.error(ERROR_TIMED_OUT), TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    Exposure.LOGGER.error("Capturing failed: {}", throwable.toString());
                    return Result.error(ERROR_FAILED_GENERIC);
                })
                .thenApply(result -> {
                    if (result.isSuccessful()) {
                        component.onSuccess();
                    } else {
                        component.onFailure();
                    }
                    component.afterCapture();
                    return result;
                })
                .thenAccept(result -> {
                    setDone();
                    completableFuture.complete(result);
                });
    }

    public Task<T> handleErrorAndGetResult() {
        return handleErrorAndGetResult(err -> {});
    }

    public Task<T> handleErrorAndGetResult(Consumer<TranslatableError> errorConsumer) {
        return onError(errorConsumer).then(Result::unwrap);
    }

    public static <T> Capture<T> of(Task<Result<T>> capturingTask) {
        return new Capture<>(capturingTask, CaptureAction.EMPTY);
    }

    public static <T> Capture<T> of(Task<Result<T>> capturingTask, CaptureAction action) {
        return new Capture<>(capturingTask, action);
    }

    public static <T> Capture<T> of(Task<Result<T>> capturingTask, CaptureAction... actions) {
        return new Capture<>(capturingTask, new CompositeAction(actions));
    }

    public static Task<Result<Image>> screenshot() {
        return ExposureClient.isIrisOrOculusInstalled() || Config.Client.FORCE_DIRECT_SCREENSHOT_CAPTURE.isTrue()
                ? new DirectScreenshotCaptureTask()
                : new BackgroundScreenshotCaptureTask();
    }

    //TODO: maybe add timeout for 1-2 seconds, so result is not that delayed when loading big images.
    // But this might be problematic if used has slow hdd or loading takes long for other reasons.
    public static Task<Result<Image>> file(String filePath) {
        return new FileCaptureTask(filePath);
    }
}
