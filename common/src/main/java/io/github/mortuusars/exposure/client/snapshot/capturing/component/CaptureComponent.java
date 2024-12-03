package io.github.mortuusars.exposure.client.snapshot.capturing.component;

public interface CaptureComponent {
    CaptureComponent EMPTY = new CaptureComponent() {};

    default int requiredDelayTicks() {
        return 0;
    }

    default void initialize() {
    }

    default void delayTick(int delayTicksLeft) {
    }

    default void beforeCapture() {
    }

    default void afterCapture() {
    }

    default CaptureComponent combine(CaptureComponent other) {
        if (this.equals(EMPTY)) return other;
        if (other.equals(EMPTY)) return this;
        else return new CompositeCaptureComponent(this, other);
    }
}
