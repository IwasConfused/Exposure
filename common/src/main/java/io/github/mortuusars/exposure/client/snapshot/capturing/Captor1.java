package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponent;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.CaptureMethod;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Captor1 {
    public static final int CAPTURE_TIMEOUT_MS = 15_000; // 15 seconds.
    public static final ErrorMessage ERROR_TIMED_OUT = ErrorMessage.create("gui.exposure.capture.error.timed_out");

    protected final CaptureMethod captureMethod;
    protected final boolean async;
    protected final List<CaptureComponent> components;

    protected final AtomicBoolean isCapturing = new AtomicBoolean(false);
    protected final AtomicBoolean isDone = new AtomicBoolean(false);

    protected @Nullable CaptureResult result;

    protected long initializedAt = -1;
    protected long previousTick = -1;
    protected int delayTicks = 0;

    public Captor1(CaptureMethod captureMethod, boolean async, List<CaptureComponent> components) {
        this.captureMethod = captureMethod;
        this.async = async;
        this.components = components;
    }

    public boolean isCurrentlyCapturing() {
        return isCapturing.get();
    }

    public boolean isDone() {
        return isDone.get();
    }

    public Optional<CaptureResult> tick(long gameTime) {
        // this method is called every frame (ie multiple times per tick), so we need to track if tick is changed.
        boolean newTick = previousTick != gameTime;
        previousTick = gameTime;

        if (isDone() || isCurrentlyCapturing()) {
            return Optional.ofNullable(result);
        }

        if (initializedAt == -1) {
            initialize(gameTime);
        }

        if (delayTicks > 0) {
            if (newTick) {
                for (CaptureComponent component : components) {
                    component.delayTick(delayTicks);
                }

                delayTicks--;
            }

            return Optional.empty();
        }

        if (!async) {
            return getCaptureResultBlocking();
        }

        if (!isCurrentlyCapturing()) {
            captureAsyncInternal();
        }

        return Optional.ofNullable(result);
    }

    private void initialize(long gameTime) {
        this.initializedAt = gameTime;

        for (CaptureComponent component : components) {
            this.delayTicks = Math.max(this.delayTicks, component.requiredDelayTicks());
        }

        for (CaptureComponent component : components) {
            component.initialize();
        }
    }

    private @NotNull Optional<CaptureResult> getCaptureResultBlocking() {
        try {
            if (!isDone()) {
                captureAsyncInternal().get();
            }
            return Optional.ofNullable(result);
        } catch (Exception e) {
            Exposure.LOGGER.error("Capturing failed: {}", e.toString());
            return Optional.of(CaptureResult.error(CaptureMethod.ERROR_FAILED_GENERIC));
        }
    }

    private CompletableFuture<Void> captureAsyncInternal() {
        Preconditions.checkState(!isCapturing.get(), "Already capturing.");
        Preconditions.checkState(!isDone.get(), "This capture has finished and result is available.");

        for (CaptureComponent component : components) {
            component.beforeCapture();
        }

        isCapturing.set(true);

        return captureMethod.capture()
                .completeOnTimeout(CaptureResult.error(ERROR_TIMED_OUT), CAPTURE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .thenAccept(captureResult -> {
            result = captureResult;
            isCapturing.set(false);
            isDone.set(true);
            Minecraft.getInstance().execute(() -> {
                for (CaptureComponent component : components) {
                    component.afterCapture();
                }
            });
        });
    }
}
