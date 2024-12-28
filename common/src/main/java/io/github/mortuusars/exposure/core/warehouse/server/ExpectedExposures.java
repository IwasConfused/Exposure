package io.github.mortuusars.exposure.core.warehouse.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExpectedExposures {
    private final Map<ExposureIdentifier, PalettedExposure.Tag> exposures = new HashMap<>();

    public void add(ExposureIdentifier identifier, PalettedExposure.Tag exposureMetadata) {
        exposures.put(identifier, exposureMetadata);
    }

    public @NotNull PalettedExposure.Tag get(ExposureIdentifier identifier) {
        @Nullable PalettedExposure.Tag captureData = exposures.get(identifier);
        Preconditions.checkNotNull(captureData, "No expected capture with ID '%s' is present. Check with 'contains' first.", identifier);
        return captureData;
    }

    public boolean contains(ExposureIdentifier identifier) {
        return exposures.containsKey(identifier);
    }

    public void remove(ExposureIdentifier identifier) {
        exposures.remove(identifier);
    }
}
