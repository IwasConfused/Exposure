package io.github.mortuusars.exposure.client.capture.template;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CaptureTemplates {
    private static final Map<Item, CaptureTemplate> TEMPLATES = new HashMap<>();

    public static void register(Item item, CaptureTemplate template) {
        Preconditions.checkState(!TEMPLATES.containsKey(item), "Template for item '%s' is already registered.", item);
        TEMPLATES.put(item, template);
    }

    public static CaptureTemplate getOrThrow(Item item) {
        @Nullable CaptureTemplate template = TEMPLATES.get(item);
        Preconditions.checkNotNull(template, "No template for item '%s' is registered.", item);
        return template;
    }

    static {
        register(Exposure.Items.CAMERA.get(), new CameraCaptureTemplate());
    }
}
