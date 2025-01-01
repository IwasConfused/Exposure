package io.github.mortuusars.exposure.client.capture.template;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CaptureTemplates {
    private static final Map<ResourceLocation, Supplier<CaptureTemplate>> TEMPLATES = new HashMap<>();

    public static void register(ResourceLocation id, Supplier<CaptureTemplate> template) {
        Preconditions.checkState(!TEMPLATES.containsKey(id), "Template with id '%s' is already registered.", id);
        TEMPLATES.put(id, template);
    }

    public static CaptureTemplate getOrThrow(ResourceLocation id) {
        @Nullable Supplier<CaptureTemplate> template = TEMPLATES.get(id);
        Preconditions.checkNotNull(template, "No template for id '%s' is registered.", id);
        return template.get();
    }

    static {
        register(Exposure.resource("camera"), CameraCaptureTemplate::new);
        register(Exposure.resource("debug_rgb"), SingleChannelCaptureTemplate::new);
    }
}
