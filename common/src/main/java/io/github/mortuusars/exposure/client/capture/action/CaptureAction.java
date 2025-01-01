package io.github.mortuusars.exposure.client.capture.action;

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

    default void onFailure() {
    }

    default void afterCapture() {
    }

    default CaptureAction combine(CaptureAction other) {
        if (this.equals(EMPTY)) return other;
        if (other.equals(EMPTY)) return this;
        else return new CompositeAction(this, other);
    }
}
