package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponent;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CompositeCaptureComponent;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.CaptureMethod;
import io.github.mortuusars.exposure.client.snapshot.capturing.method.FileCaptureMethod;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.task.FallbackTask;
import io.github.mortuusars.exposure.util.task.OverrideTask;
import io.github.mortuusars.exposure.util.task.Result;
import io.github.mortuusars.exposure.util.task.Task;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CaptureOld extends Task<Result<Image>> {
    public static final String ERROR_TIMED_OUT = "gui.exposure.capture.error.timed_out";

    protected final CaptureMethod method;
    protected final CaptureComponent component;
    protected final long timeoutMs;
    protected final Consumer<TranslatableError> onError;

    protected final CaptureTimer timer;
    protected final CompletableFuture<Result<Image>> completableFuture;

    public CaptureOld(CaptureMethod captureMethod, CaptureComponent component, long timeoutMs, Consumer<TranslatableError> onError) {
        this.method = captureMethod;
        this.component = component;
        this.timeoutMs = timeoutMs;
        this.onError = onError;

        this.timer = new CaptureTimer(component.requiredDelayTicks())
                .whenStarted(this.component::initialize)
                .onGameTick(this.component::delayTick)
                .whenEnded(() -> {
                    this.component.beforeCapture();
                    captureImage();
                });
        this.completableFuture = new CompletableFuture<>();
    }

    public CompletableFuture<Result<Image>> execute() {
        if (!timer.isRunning()) {
            timer.start();
            setStarted();
        }

        return completableFuture;
    }

    public void tick() {
        method.tick();
        timer.tick();
    }

    private void captureImage() {
        method.capture()
                .completeOnTimeout(Result.error(ERROR_TIMED_OUT), timeoutMs, TimeUnit.MILLISECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        Exposure.LOGGER.error("Capturing failed: {}", throwable.toString());
                        return Result.<Image>error(CaptureMethod.ERROR_FAILED_GENERIC);
                    }
                    return result != null ? result : Result.<Image>error(CaptureMethod.ERROR_FAILED_GENERIC);
                })
                .thenApply(result -> {
                    setDone();
                    // Execution of components and stuff should be on the RenderThread
                    // because it may need to access something in the game that may throw if executed from other threads.
                    Minecraft.getInstance().execute(() -> {
                        component.afterCapture();
                        if (result.isError()) {
                            onError.accept(result.getError());
                        }
                    });
                    return result;
                })
                .thenAccept(completableFuture::complete);
    }

    public Task<Result<Image>> overridenBy(Task<Result<Image>> override) {
        return new OverrideTask<>(this, override);
    }

    public Task<Result<Image>> fallbackTo(Task<Result<Image>> fallback) {
        return new FallbackTask<>(this, fallback);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder file(String filePath) {
        return new Builder().method(new FileCaptureMethod(filePath));
    }

    public static class Builder {
        private CaptureMethod captureMethod;
        private CaptureComponent component = CaptureComponent.EMPTY;
        private Consumer<TranslatableError> onError;
        private long timeoutMs = 10_000; // 10 seconds

        @Nullable
        private Task<Result<Image>> overrideTask;
        @Nullable
        private Task<Result<Image>> fallbackTask;

        public Builder method(CaptureMethod captureMethod) {
            this.captureMethod = captureMethod;
            return this;
        }

        public Builder addComponent(CaptureComponent component) {
            this.component = this.component.combine(component);
            return this;
        }

        public Builder addComponents(CaptureComponent... components) {
            return addComponent(new CompositeCaptureComponent(components));
        }

        public Builder onError(Consumer<TranslatableError> errorMessageConsumer) {
            this.onError = errorMessageConsumer;
            return this;
        }

        public Builder timeoutAfter(long duration, TimeUnit unit) {
            timeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder overridenBy(Task<Result<Image>> overrideTask) {
            Preconditions.checkState(fallbackTask == null, "Capture task cannot be 'overridenBy' and 'fallbackTo' at the same time.");
            this.overrideTask = overrideTask;
            return this;
        }

        public Builder fallbackTo(Task<Result<Image>> fallbackTask) {
            Preconditions.checkState(overrideTask == null, "Capture task cannot be 'overridenBy' and 'fallbackTo' at the same time.");
            this.fallbackTask = fallbackTask;
            return this;
        }

        public Task<Result<Image>> createTask() {
            Preconditions.checkState(captureMethod != null,
                    "Capture Method wasn't specified. Use 'method' to specify.");
            CaptureOld captor = new CaptureOld(captureMethod, component, timeoutMs, onError);

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
