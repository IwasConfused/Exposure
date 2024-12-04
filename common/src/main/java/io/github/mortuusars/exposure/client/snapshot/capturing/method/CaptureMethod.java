package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.util.task.Result;
import io.github.mortuusars.exposure.core.image.Image;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface CaptureMethod {
    String ERROR_FAILED_GENERIC = "gui.exposure.capture.error.failed";

    @NotNull CompletableFuture<Result<Image>> capture();
    default void tick() {}

    static CaptureMethod screenshot() {
        return ExposureClient.isIrisOrOculusInstalled() || Config.Client.FORCE_DIRECT_SCREENSHOT_CAPTURE.isTrue()
                ? new DirectScreenshotCaptureMethod()
                : new BackgroundScreenshotCaptureMethod();
    }

    static CaptureMethod fromFile(String filePath) {
        return new FileCaptureMethod(filePath);
    }
}
