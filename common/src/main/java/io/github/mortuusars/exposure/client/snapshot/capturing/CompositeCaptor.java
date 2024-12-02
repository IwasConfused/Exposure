package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.snapshot.CaptureTask;
import io.github.mortuusars.exposure.client.snapshot.ExecutionStrategy;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class CompositeCaptor extends CaptureTask {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CaptureTask main;
    private final CaptureTask fallback;
    private final ExecutionStrategy executionStrategy;

    public CompositeCaptor(CaptureTask main, CaptureTask fallback, ExecutionStrategy executionStrategy) {
        this.main = main;
        this.fallback = fallback;
        this.executionStrategy = executionStrategy;
    }

    @Override
    public CompletableFuture<TaskResult<Image>> capture() {
        return switch (executionStrategy) {
            case FALLBACK -> main.capture()
                    .handle((mainResult, mainException) -> {
                        if (mainException != null || (mainResult != null && mainResult.isError())) {
                            return fallback.capture().handle((fallbackResult, fallbackException) -> {
                                if (fallbackException != null) {
                                    LOGGER.error("Fallback SnapShot task failed: ", fallbackException);
                                    return TaskResult.<Image>error(ErrorMessage.GENERIC);
                                }
                                return fallbackResult;
                            }).join();
                        }

                        return mainResult;
                    });
            case INVERTED_FALLBACK -> fallback.capture()
                    .thenCompose(fallbackResult -> main.capture()
                            .handle((originalResult, exception) -> {
                                if (exception != null || (originalResult != null && originalResult.isError())) {
                                    LOGGER.error("Original SnapShot task failed: {}", (exception != null
                                            ? exception.toString()
                                            : originalResult.getErrorMessage().getTechnicalTranslation().getString()));
                                    return fallbackResult;
                                }

                                return originalResult;
                            }));
        };
    }

    @Override
    public void frameTick() {
        main.frameTick();
        fallback.frameTick();
    }

    @Override
    public boolean isDone() {
        return main.isDone() && fallback.isDone();
    }

    @Override
    public boolean isStarted() {
        return main.isStarted() || fallback.isStarted();
    }
}