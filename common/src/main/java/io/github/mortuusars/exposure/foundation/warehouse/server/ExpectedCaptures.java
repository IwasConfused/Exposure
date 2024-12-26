package io.github.mortuusars.exposure.foundation.warehouse.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.frame.CaptureData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExpectedCaptures {
    private final Map<ExposureIdentifier, CaptureData> captures = new HashMap<>();

    public void add(ExposureIdentifier identifier, CaptureData captureData) {
        captures.put(identifier, captureData);
    }

    public @NotNull CaptureData get(ExposureIdentifier identifier) {
        @Nullable CaptureData captureData = captures.get(identifier);
        Preconditions.checkNotNull(captureData, "No expected capture with ID '%s' is present. Check with 'contains' first.", identifier);
        return captureData;
    }

    public boolean contains(ExposureIdentifier identifier) {
        return captures.containsKey(identifier);
    }
}
