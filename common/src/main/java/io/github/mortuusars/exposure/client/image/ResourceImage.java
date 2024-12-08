package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.image.IdentifiableImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;

public class ResourceImage extends SimpleTexture implements Image {
    @Nullable
    protected NativeImage image;

    public ResourceImage(ResourceLocation location) {
        super(location);
    }

    @Override
    public int getWidth() {
        @Nullable NativeImage image = getImage();
        return image != null ? image.getWidth() : 1;
    }

    @Override
    public int getHeight() {
        @Nullable NativeImage image = getImage();
        return image != null ? image.getHeight() : 1;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        @Nullable NativeImage image = getImage();
        return image != null ? image.getPixelRGBA(x, y) : 0x00000000;
    }

    public static @NotNull Image getOrCreate(ResourceLocation location) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        @Nullable AbstractTexture existingTexture = textureManager.byPath.get(location);
        if (existingTexture != null) {
            return existingTexture instanceof ResourceImage exposureTexture ? exposureTexture : Image.MISSING;
        }

        try {
            ResourceImage texture = new ResourceImage(location);
            textureManager.register(location, texture);
            return texture;
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Cannot load texture [{}]. {}", location, e);
            return Image.MISSING;
        }
    }

    public @Nullable NativeImage getImage() {
        if (this.image != null)
            return image;

        try {
            NativeImage image = super.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
            this.image = image;
            return image;
        } catch (IOException e) {
            Exposure.LOGGER.error("Cannot load texture: {}", e.toString());
            return null;
        }
    }

    @Override
    public void reset(@NotNull TextureManager pTextureManager, @NotNull ResourceManager pResourceManager, @NotNull ResourceLocation pPath, @NotNull Executor pExecutor) {
        super.reset(pTextureManager, pResourceManager, pPath, pExecutor);
        if (image != null) {
            image.close();
            image = null;
        }
    }

    @Override
    public void close() {
        super.close();

        if (this.image != null) {
            image.close();
            image = null;
        }
    }
}
