package io.github.mortuusars.exposure.client.snapshot;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureTimer;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponent;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponents;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.CaptureMethod;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Captor {
    public static final ErrorMessage ERROR_TIMED_OUT = ErrorMessage.create("gui.exposure.capture.error.timed_out");

    protected final CaptureMethod method;
    protected final CaptureComponents components;
    protected final long timeoutMs;

    protected final CaptureTimer timer;
    protected final CompletableFuture<CaptureResult> completableFuture;

    public Captor(CaptureMethod captureMethod, CaptureComponents components, long timeoutMs) {
        this.method = captureMethod;
        this.components = components;
        this.timeoutMs = timeoutMs;

        this.timer = new CaptureTimer(components.requiredDelayTicks())
                .whenStarted(this.components::initialize)
                .onTick(this.components::delayTick)
                .whenEnded(() -> {
                    this.components.beforeCapture();
                    captureImage();
                });
        this.completableFuture = new CompletableFuture<>();
    }

    public CompletableFuture<CaptureResult> capture() {
        if (!timer.isRunning()) {
            timer.start();
        }

        return completableFuture;
    }

    public void tick() {
        if (timer.isRunning()) {
            timer.update();
        }
    }

    private void captureImage() {
        method.capture()
                .completeOnTimeout(CaptureResult.error(ERROR_TIMED_OUT), timeoutMs, TimeUnit.MILLISECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        Exposure.LOGGER.error("Capturing failed: {}", throwable.toString());
                        return CaptureResult.error(CaptureMethod.ERROR_FAILED_GENERIC);
                    }
                    return result != null ? result : CaptureResult.error(CaptureMethod.ERROR_FAILED_GENERIC);
                })
                .thenApply(result -> {
                    Minecraft.getInstance().execute(components::afterCapture);
                    return result;
                })
                .thenAccept(completableFuture::complete);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CaptureMethod captureMethod;
        private final CaptureComponents components = new CaptureComponents();
        private long timeoutMs = 10_000; // 10 seconds

        public Builder method(CaptureMethod captureMethod) {
            this.captureMethod = captureMethod;
            return this;
        }

        public Builder addComponents(CaptureComponent... components) {
            Arrays.stream(components).forEach(this.components::add);
            return this;
        }

        public Builder timeoutAfter(long duration, TimeUnit unit) {
            timeoutMs = unit.toMillis(duration);
            return this;
        }

        public Captor create() {
            Preconditions.checkState(captureMethod != null,
                    "Capture Method wasn't specified. Use 'method' to specify.");
            return new Captor(captureMethod, components, timeoutMs);
        }
    }
}
