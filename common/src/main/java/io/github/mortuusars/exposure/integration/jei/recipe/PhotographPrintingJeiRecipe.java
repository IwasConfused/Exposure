package io.github.mortuusars.exposure.integration.jei.recipe;

import io.github.mortuusars.exposure.core.ExposureType;

public class PhotographPrintingJeiRecipe {
    private final ExposureType exposureType;

    public PhotographPrintingJeiRecipe(ExposureType exposureType) {
        this.exposureType = exposureType;
    }

    public ExposureType getExposureType() {
        return exposureType;
    }
}
