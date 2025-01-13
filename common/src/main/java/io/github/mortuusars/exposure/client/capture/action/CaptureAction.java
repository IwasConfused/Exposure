package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.util.TranslatableError;

public interface CaptureAction {
    CaptureAction EMPTY = new CaptureAction() {};

    default int requiredDelayTicks() {
        return 0;
    }

    default void initialize() {
    }

    default void delayTick(int delayTicksLeft) {
    }

    default void beforeCapture() {
    }

    default void onSuccess() {
    }

    default void onFailure(TranslatableError error) {
    }

    default void afterCapture() {
    }
}
