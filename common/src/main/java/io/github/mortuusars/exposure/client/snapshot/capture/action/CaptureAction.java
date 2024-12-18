package io.github.mortuusars.exposure.client.snapshot.capture.action;

import java.util.function.Supplier;

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

    // --

    static CaptureAction optional(boolean predicate, Supplier<CaptureAction> componentSupplier) {
        return predicate ? componentSupplier.get() : CaptureAction.EMPTY;
    }

    static CaptureAction optional(boolean predicate, CaptureAction component) {
        return predicate ? component : CaptureAction.EMPTY;
    }
}
