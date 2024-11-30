package io.github.mortuusars.exposure.client.snapshot.processing;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Processor {
    protected final List<ProcessingStep> processingSteps;

    public Processor(List<ProcessingStep> processingSteps) {
        this.processingSteps = processingSteps;
    }

    public NativeImage process(NativeImage image) {
        NativeImage result = image;

        for (ProcessingStep processingStep : processingSteps) {
            result = processingStep.process(result);
        }

        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected final List<ProcessingStep> processingSteps = new ArrayList<>();

        public Builder addSteps(ProcessingStep... processingSteps) {
            this.processingSteps.addAll(Arrays.asList(processingSteps));
            return this;
        }

        public Processor build() {
            return new Processor(processingSteps);
        }
    }
}
