package io.github.mortuusars.exposure.camera.viewfinder;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.camera.AttachmentType;
import io.github.mortuusars.exposure.data.filter.Filter;
import io.github.mortuusars.exposure.data.filter.Filters;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ViewfinderShader {
    @Nullable
    private static ResourceLocation previousShader;

    public static Optional<ResourceLocation> getCurrent() {
        PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
        if (effect != null) {
            return Optional.of(ResourceLocation.parse(effect.getName()));
        }

        return Optional.empty();
    }

    public static void setPrevious(@Nullable ResourceLocation shader) {
        previousShader = shader;
    }

    public static void restorePrevious() {
        if (previousShader != null && shouldRestorePreviousShaderEffect()) {
            applyShader(previousShader);
            previousShader = null;
        }
    }

    public static void applyShader(ResourceLocation shader) {
        @Nullable PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
        if (effect != null && effect.getName().equals(shader.toString())) {
            return;
        }

        Minecraft.getInstance().gameRenderer.loadEffect(shader);
    }

    public static void removeShader() {
        Minecraft.getInstance().gameRenderer.shutdownEffect();
    }

    public static void update() {
        CameraClient.getActiveCamera().ifPresentOrElse(camera -> {
            ItemStack filterStack = camera.getItem().getAttachment(camera.getItemStack(), AttachmentType.FILTER).getForReading();
            Filters.getShaderOf(filterStack).ifPresentOrElse(ViewfinderShader::applyShader, ViewfinderShader::removeShader);
        }, ViewfinderShader::removeShader);
    }

    public static boolean shouldRestorePreviousShaderEffect() {
        /*
            Cold Sweat applies a shader effect when having high temperature.
            If we restore effect after exiting viewfinder it will apply blur even if temp is normal.
            Not restoring shader is fine, Cold Sweat will reapply it if needed.
         */
        if (PlatformHelper.isModLoaded("cold_sweat") && previousShader != null
                && previousShader.toString().equals("minecraft:shaders/post/blobs2.json"))
            return false;
        else
            return previousShader != null;
    }
}
