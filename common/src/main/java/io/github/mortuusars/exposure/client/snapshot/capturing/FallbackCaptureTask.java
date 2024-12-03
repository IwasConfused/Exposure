package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * If first task is failed, second task will be executed, and it's result returned.
 */
public class FallbackCaptureTask extends CaptureTask {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CaptureTask main;
    private final CaptureTask fallback;

    public FallbackCaptureTask(CaptureTask main, CaptureTask fallback) {
        this.main = main;
        this.fallback = fallback;
    }

    @Override
    public CompletableFuture<TaskResult<Image>> capture() {
        return main.capture()
                .handle((mainResult, mainException) -> {
                    if (mainResult != null && mainResult.isSuccessful()) {
                        return mainResult;
                    } else if (mainException != null) {
                        LOGGER.error("Main CaptureTask is failed: ", mainException);
                    } else if (mainResult != null && mainResult.isError()) {
                        LOGGER.error("Main CaptureTask is failed: {}", mainResult.getErrorMessage());
                    }

                    return captureFallback();
                });
    }

    private TaskResult<Image> captureFallback() {
        return fallback.capture().handle((fallbackResult, fallbackException) -> {
            if (fallbackException != null) {
                LOGGER.error("Both Main and Fallback SnapShot tasks are failed!");
                return TaskResult.<Image>error(ErrorMessage.GENERIC);
            }
            return fallbackResult;
        }).join();
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