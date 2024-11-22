package io.github.mortuusars.exposure.core;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.ResourceLocation;

public record PhotographType(ResourceLocation id) {
    public static final PhotographType REGULAR = new PhotographType(Exposure.resource("regular"));
    public static final PhotographType AGED = new PhotographType(Exposure.resource("aged"));
}
