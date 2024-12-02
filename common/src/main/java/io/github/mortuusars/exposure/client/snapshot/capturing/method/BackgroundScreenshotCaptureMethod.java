package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderShader;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class BackgroundScreenshotCaptureMethod implements CaptureMethod {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public @NotNull CompletableFuture<TaskResult<Image>> capture() {
        Minecraft minecraft = Minecraft.getInstance();

        RenderTarget renderTarget = new TextureTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), true, Minecraft.ON_OSX);
        renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        renderTarget.clear(Minecraft.ON_OSX);

        try {
            // For whatever reason setPanoramicMode makes water visible again. So we cannot omit it.
            minecraft.gameRenderer.setPanoramicMode(true);

            minecraft.gameRenderer.setRenderBlockOutline(false);

            minecraft.levelRenderer.graphicsChanged();
            renderTarget.bindWrite(false);
            minecraft.gameRenderer.renderLevel(minecraft.getTimer());

            ViewfinderShader.process(renderTarget);

            WrappedNativeImage image = new WrappedNativeImage(Screenshot.takeScreenshot(renderTarget));

            return CompletableFuture.supplyAsync(() -> {
                return TaskResult.success(image);
            });
        } catch (Exception e) {
            LOGGER.error("Couldn't capture image", e);
            return CompletableFuture.completedFuture(TaskResult.error(ErrorMessage.EMPTY));
        } finally {
            minecraft.gameRenderer.setPanoramicMode(false);
            minecraft.gameRenderer.setRenderBlockOutline(true);
            renderTarget.destroyBuffers();
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }
}
