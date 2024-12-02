package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
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
    protected CompletableFuture<TaskResult<Image>> future;

    @Override
    public @NotNull CompletableFuture<TaskResult<Image>> capture() {
        future = new CompletableFuture<>();
        return future;
    }

    @Override
    public void frameTick() {
        if (future == null || future.isDone()) {
            return;
        }

        if (delay <= 0) {
            NativeImage nativeImage = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
            future.complete(TaskResult.success(new WrappedNativeImage(nativeImage)));
        }

        delay--;
    }
}
