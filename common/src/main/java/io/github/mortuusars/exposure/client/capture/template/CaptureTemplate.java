package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.core.CaptureProperties;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import net.minecraft.client.player.LocalPlayer;

public interface CaptureTemplate {
    Task<?> createTask(CaptureProperties captureProperties);
}
