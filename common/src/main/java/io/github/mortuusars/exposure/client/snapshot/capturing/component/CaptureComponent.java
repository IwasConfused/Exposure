package io.github.mortuusars.exposure.client.snapshot.capturing.component;

public interface CaptureComponent {
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
}
