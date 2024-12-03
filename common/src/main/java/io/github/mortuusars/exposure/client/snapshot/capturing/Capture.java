package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponent;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponentsList;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.BackgroundScreenshotCaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.CaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.FileCaptureMethod;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Capture extends CaptureTask {
    public static final ErrorMessage ERROR_TIMED_OUT = ErrorMessage.create("gui.exposure.capture.error.timed_out");

    protected final CaptureMethod method;
    protected final CaptureComponentsList components;
    protected final long timeoutMs;
    protected final Consumer<ErrorMessage> onError;

    protected final CaptureTimer timer;
    protected final CompletableFuture<TaskResult<Image>> completableFuture;

    public Capture(CaptureMethod captureMethod, CaptureComponentsList components, long timeoutMs, Consumer<ErrorMessage> onError) {
        this.method = captureMethod;
        this.components = components;
        this.timeoutMs = timeoutMs;
        this.onError = onError;

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
            setStarted();
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
                    setDone();
                    Minecraft.getInstance().execute(() -> {
                        components.afterCapture();
                        if (result.isError()) {
                            onError.accept(result.getErrorMessage());
                        }
                    });
                    return result;
                })
                .thenAccept(completableFuture::complete);
    }

    public CaptureTask overridenBy(CaptureTask task) {
        return FallbackCaptureTask.override(this, task);
    }

    public CaptureTask fallbackTo(CaptureTask task) {
        return new FallbackCaptureTask(this, task, FallbackCaptureTask.ExecutionStrategy.FALLBACK);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder screenshot() {
        return new Builder().method(new BackgroundScreenshotCaptureMethod());
    }

    public static Builder file(String filePath) {
        return new Builder().method(new FileCaptureMethod(filePath));
    }

    public static class Builder {
        private CaptureMethod captureMethod;
        private final CaptureComponentsList components = new CaptureComponentsList();
        private Consumer<ErrorMessage> onError;
        private long timeoutMs = 10_000; // 10 seconds

        @Nullable
        private CaptureTask overrideTask;
        @Nullable
        private CaptureTask fallbackTask;

        public Builder method(CaptureMethod captureMethod) {
            this.captureMethod = captureMethod;
            return this;
        }

        public Builder addComponent(CaptureComponent component) {
            this.components.add(component);
            return this;
        }

        public Builder addComponents(CaptureComponent... components) {
            Arrays.stream(components).forEach(this.components::add);
            return this;
        }

        public Builder addOptionalComponent(boolean shouldAdd, Supplier<CaptureComponent> componentSupplier) {
            if (shouldAdd) {
                this.components.add(componentSupplier.get());
            }
            return this;
        }

        public Builder onError(Consumer<ErrorMessage> errorMessageConsumer) {
            this.onError = errorMessageConsumer;
            return this;
        }

        public Builder timeoutAfter(long duration, TimeUnit unit) {
            timeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder overridenBy(CaptureTask overrideTask) {
            Preconditions.checkState(fallbackTask == null, "Capture task cannot be 'overridenBy' and 'fallbackTo' at the same time.");
            this.overrideTask = overrideTask;
            return this;
        }

        public Builder fallbackTo(CaptureTask fallbackTask) {
            Preconditions.checkState(overrideTask == null, "Capture task cannot be 'overridenBy' and 'fallbackTo' at the same time.");
            this.fallbackTask = fallbackTask;
            return this;
        }

        public CaptureTask create() {
            Preconditions.checkState(captureMethod != null,
                    "Capture Method wasn't specified. Use 'method' to specify.");
            Capture captor = new Capture(captureMethod, components, timeoutMs, onError);

            if (overrideTask != null) {
                return captor.overridenBy(overrideTask);
            }

            if (fallbackTask != null) {
                return captor.fallbackTo(fallbackTask);
            }

            return captor;
        }
    }
}
