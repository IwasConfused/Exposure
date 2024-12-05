package io.github.mortuusars.exposure.client.snapshot.capturing;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.snapshot.capturing.action.CompositeAction;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.*;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.task.*;
import io.github.mortuusars.exposure.client.snapshot.capturing.action.CaptureAction;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
                    setDone();
                    // Execution of components and stuff should be on the RenderThread
                    // because it may need to access something in the game that may throw if executed from other threads.
                    Minecraft.getInstance().execute(component::afterCapture);
                    return result;
                })
                .thenAccept(completableFuture::complete);
    }

//    public Task<Result<T>> overridenBy(Task<Result<T>> override) {
//        return new OverrideTask<>(this, override);
//    }
//
//    public Task<Result<T>> fallbackTo(Task<Result<T>> fallback) {
//        return new FallbackTask<>(this, fallback);
//    }

    public Task<T> handleErrorAndGetResult() {
        return handleErrorAndGetResult(err -> {});
    }

    public Task<T> handleErrorAndGetResult(Consumer<TranslatableError> errorConsumer) {
        return onError(errorConsumer).then(Result::unwrap);
    }

    public static class StacklessThrowable extends Throwable {
        protected StacklessThrowable(String message) {
            super(message, null, false, false);
        }
    }

    // --

//    public static <T> Task<T> compose(Task<Result<T>> capture) {
//        return capture
//                .onError(err -> Exposure.LOGGER.error(err.getLocalizedMessage()))
//                .then(Result::unwrap);
//    }

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

    public static Task<Result<Image>> file(String filePath) {
        return new FileCaptureTask(filePath);
    }
}
