package io.github.mortuusars.exposure.client.util;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Shader {
    /**
     * Processes specified shader (if it is present and active) to a specified render target.
     * Shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    // This is probably wrong class for it, but it'll do for now.
    public static void apply(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {
        try {
            ResourceLocation shaderLocation = ResourceLocation.parse(shader.getName());

            PostChain tempShader = new PostChain(Minecrft.get().getTextureManager(), Minecrft.get().getResourceManager(),
                    renderTarget, shaderLocation);
            tempShader.resize(renderTarget.width, renderTarget.height);

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            tempShader.process(Minecrft.get().getTimer().getGameTimeDeltaTicks());
        } catch (IOException e) {
            Exposure.LOGGER.warn("Failed to load shader: {}", shader.getName(), e);
        } catch (JsonSyntaxException e) {
            Exposure.LOGGER.warn("Failed to parse shader: {}", shader.getName(), e);
        }
    }
}
