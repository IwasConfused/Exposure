package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.core.frame.CaptureProperties;
import io.github.mortuusars.exposure.util.task.Task;
import net.minecraft.client.player.LocalPlayer;

public interface CaptureTemplate {
    Task<?> createTask(LocalPlayer localPlayer, String id, CaptureProperties captureProperties);
}
