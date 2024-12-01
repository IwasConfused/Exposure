package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ScreenshotCaptureMethod implements CaptureMethod {
    // At least 1 frame of delay is needed because some immediate CaptureComponents may only apply on the next frame
    // and in this method we take a screenshot of what's already rendered.
    // BackgroundScreenshotMethod does not have this problem because it renders the level again for himself.
    protected int delay = Math.max(1, Config.Client.CAPTURE_DELAY_FRAMES.get());
    @Nullable
    protected CompletableFuture<CaptureResult> future;

    @Override
    public @NotNull CompletableFuture<CaptureResult> capture() {
        future = new CompletableFuture<>();
        return future;
    }

    @Override
    public void frameTick() {
        if (future == null || future.isDone()) {
            return;
        }

        if (delay <= 0) {
            future.complete(CaptureResult.success(Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())));
        }

        delay--;
    }
}
