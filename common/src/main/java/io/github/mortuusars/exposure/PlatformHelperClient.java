package io.github.mortuusars.exposure;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class PlatformHelperClient {
    @ExpectPlatform
    public static BakedModel getModel(ModelResourceLocation model) {
        throw new AssertionError();
    }
}
