package io.github.mortuusars.exposure.client.capture.template;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CaptureTemplates {
    private static final Map<ResourceLocation, CaptureTemplate> TEMPLATES = new HashMap<>();

    public static void register(ResourceLocation id, CaptureTemplate template) {
        Preconditions.checkState(!TEMPLATES.containsKey(id), "Template with id '%s' is already registered.", id);
        TEMPLATES.put(id, template);
    }

    public static CaptureTemplate getOrThrow(ResourceLocation id) {
        @Nullable CaptureTemplate template = TEMPLATES.get(id);
        Preconditions.checkNotNull(template, "No template for id '%s' is registered.", id);
        return template;
    }

    static {
        register(Exposure.resource("camera"), new CameraCaptureTemplate());
    }
}
