package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ScreenshotCaptureMethod implements CaptureMethod {
    @Override
    public @NotNull CompletableFuture<CaptureResult> capture() {
        return CompletableFuture.completedFuture(
                CaptureResult.success(Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())));
    }
}
