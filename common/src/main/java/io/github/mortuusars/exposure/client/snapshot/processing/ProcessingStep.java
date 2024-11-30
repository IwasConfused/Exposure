package io.github.mortuusars.exposure.client.snapshot.processing;

import com.mojang.blaze3d.platform.NativeImage;

@FunctionalInterface
public interface ProcessingStep {
    NativeImage process(NativeImage nativeImage);
}
