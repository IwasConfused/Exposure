package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponent;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponentsList;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.CaptureMethod;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Captor {
    public static final ErrorMessage ERROR_TIMED_OUT = ErrorMessage.create("gui.exposure.capture.error.timed_out");

    protected final CaptureMethod method;
    protected final CaptureComponentsList components;
    protected final long timeoutMs;

    protected final CaptureTimer timer;
    protected final CompletableFuture<TaskResult<Image>> completableFuture;

    public Captor(CaptureMethod captureMethod, CaptureComponentsList components, long timeoutMs) {
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

    public CompletableFuture<TaskResult<Image>> capture() {
        if (!timer.isRunning()) {
            timer.start();
        }

        return completableFuture;
    }

    public void frameTick() {
        method.frameTick();
        timer.frameTick();
    }

    private void captureImage() {
        method.capture()
                .completeOnTimeout(TaskResult.error(ERROR_TIMED_OUT), timeoutMs, TimeUnit.MILLISECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        Exposure.LOGGER.error("Capturing failed: {}", throwable.toString());
                        return TaskResult.<Image>error(CaptureMethod.ERROR_FAILED_GENERIC);
                    }
                    return result != null ? result : TaskResult.<Image>error(CaptureMethod.ERROR_FAILED_GENERIC);
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
        private final CaptureComponentsList components = new CaptureComponentsList();
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
