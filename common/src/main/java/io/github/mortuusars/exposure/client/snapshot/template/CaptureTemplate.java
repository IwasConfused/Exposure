package io.github.mortuusars.exposure.client.snapshot.template;

import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.frame.CaptureData;
import io.github.mortuusars.exposure.util.task.Task;
import net.minecraft.client.player.LocalPlayer;

public interface CaptureTemplate {
    Task<?> createTask(LocalPlayer localPlayer, ExposureIdentifier identifier, CaptureData captureData);
}
