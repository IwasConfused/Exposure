package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import java.util.Arrays;
import java.util.List;

public class CompositeCaptureComponent implements CaptureComponent {
    private final List<CaptureComponent> components;

    public CompositeCaptureComponent(List<CaptureComponent> components) {
        this.components = components;
    }

    public CompositeCaptureComponent(CaptureComponent... components) {
        this(Arrays.stream(components).toList());
    }

    @Override
    public int requiredDelayTicks() {
        return components.stream().mapToInt(CaptureComponent::requiredDelayTicks).max().orElse(0);
    }

    @Override
    public void initialize() {
        components.forEach(CaptureComponent::initialize);
    }

    @Override
    public void delayTick(int delayTicksLeft) {
        components.forEach(component -> component.delayTick(delayTicksLeft));
    }

    @Override
    public void beforeCapture() {
        components.forEach(CaptureComponent::beforeCapture);
    }

    @Override
    public void afterCapture() {
        components.forEach(CaptureComponent::afterCapture);
    }
}
