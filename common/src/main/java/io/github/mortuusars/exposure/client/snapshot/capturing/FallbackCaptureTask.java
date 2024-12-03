package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class FallbackCaptureTask extends CaptureTask {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CaptureTask first;
    private final CaptureTask second;
    private final ExecutionStrategy executionStrategy;

    public FallbackCaptureTask(CaptureTask first, CaptureTask second, ExecutionStrategy executionStrategy) {
        this.first = first;
        this.second = second;
        this.executionStrategy = executionStrategy;
    }

    public static FallbackCaptureTask fallback(CaptureTask main, CaptureTask fallback) {
        return new FallbackCaptureTask(main, fallback, ExecutionStrategy.FALLBACK);
    }

    public static FallbackCaptureTask override(CaptureTask main, CaptureTask override) {
        return new FallbackCaptureTask(main, override, ExecutionStrategy.OVERRIDE);
    }

    @Override
    public CompletableFuture<TaskResult<Image>> capture() {
        return switch (executionStrategy) {
            case FALLBACK -> first.capture()
                    .handle((firstResult, firstException) -> {
                        if (firstException != null || (firstResult != null && firstResult.isError())) {
                            return second.capture().handle((secondResult, secondException) -> {
                                if (secondException != null) {
                                    LOGGER.error("Fallback SnapShot task failed: ", secondException);
                                    return TaskResult.<Image>error(ErrorMessage.GENERIC);
                                }
                                return secondResult;
                            }).join();
                        }

                        return firstResult;
                    });
            case OVERRIDE -> first.capture()
                    .thenCompose(firstResult -> second.capture()
                            .handle((secondResult, secondException) -> {
                                if (secondException == null && secondResult != null && secondResult.isSuccessful()) {
                                    return secondResult;
                                }

                                if (secondException != null || (secondResult != null && secondResult.isError())) {
                                    String errorMsg = secondException != null
                                            ? secondException.toString()
                                            : secondResult.getErrorMessage().getTechnicalTranslation().getString();
                                    LOGGER.error("Override SnapShot task failed: {}", errorMsg);
                                }

                                return firstResult;
                            }));
        };
    }

    @Override
    public void frameTick() {
        first.frameTick();
        second.frameTick();
    }

    @Override
    public boolean isDone() {
        return first.isDone() && second.isDone();
    }

    @Override
    public boolean isStarted() {
        return first.isStarted() || second.isStarted();
    }

    public enum ExecutionStrategy {
        /**
         * Second task is returned if first task fails.
         */
        FALLBACK,
        /**
         * First task executes first, then second task. Second is returned if it's successful.
         */
        OVERRIDE
    }
}