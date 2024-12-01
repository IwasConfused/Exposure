package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import java.util.ArrayList;
import java.util.List;

public class CaptureComponentsList {
    private final List<CaptureComponent> components = new ArrayList<>();

    public CaptureComponentsList add(CaptureComponent component) {
        components.add(component);
        return this;
    }

    public int requiredDelayTicks() {
        return components.stream().map(CaptureComponent::requiredDelayTicks).reduce(Integer::max).orElse(0);
    }

    public void initialize() {
        components.forEach(CaptureComponent::initialize);
    }

    public void delayTick(int delayTicksLeft) {
        components.forEach(component -> component.delayTick(delayTicksLeft));
    }

    public void beforeCapture() {
        components.forEach(CaptureComponent::beforeCapture);
    }

    public void afterCapture() {
        components.forEach(CaptureComponent::afterCapture);
    }
}
