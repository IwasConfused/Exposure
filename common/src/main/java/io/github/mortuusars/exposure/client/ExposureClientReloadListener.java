package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.ExposureClient;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

public class ExposureClientReloadListener extends SimplePreparableReloadListener<Boolean> {
    @Override
    protected @NotNull Boolean prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return true;
    }

    @Override
    protected void apply(Boolean object, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (ExposureClient.exposureCache() != null) {
            ExposureClient.exposureCache().clear();
        }
        if (ExposureClient.exposureReceiver() != null) {
            ExposureClient.exposureReceiver().clear();
        }
        if (ExposureClient.imageRenderer() != null) {
            ExposureClient.imageRenderer().clearCache();
        }
    }
}
