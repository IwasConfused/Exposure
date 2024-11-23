package io.github.mortuusars.exposure.camera.capture;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderShader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Captures a screenshot without showing it on the screen by rendering to the separate render target.
 */
public class BackgroundScreenshotCapture extends Capture {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public @Nullable NativeImage captureImage() {
        Minecraft minecraft = Minecraft.getInstance();

        RenderTarget renderTarget = new TextureTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), true, Minecraft.ON_OSX);
        renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        renderTarget.clear(Minecraft.ON_OSX);

        // For whatever reason setPanoramicMode makes water visible again. So we cannot omit it.
        minecraft.gameRenderer.setPanoramicMode(true);

        minecraft.gameRenderer.setRenderBlockOutline(false);

        try {
            minecraft.levelRenderer.graphicsChanged();
            renderTarget.bindWrite(false);

            minecraft.gameRenderer.renderLevel(minecraft.getTimer());

            ViewfinderShader.process(renderTarget);

            return Screenshot.takeScreenshot(renderTarget);
        } catch (Exception e) {
            LOGGER.error("Couldn't capture image", e);
        } finally {
            minecraft.gameRenderer.setPanoramicMode(false);
            minecraft.gameRenderer.setRenderBlockOutline(true);
            renderTarget.destroyBuffers();
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }

        return null;
    }
}
