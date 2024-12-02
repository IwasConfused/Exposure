package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface CaptureMethod {
    ErrorMessage ERROR_FAILED_GENERIC = ErrorMessage.create("gui.exposure.capture.error.failed");

    @NotNull CompletableFuture<TaskResult<Image>> capture();
    default void frameTick() {}
}
