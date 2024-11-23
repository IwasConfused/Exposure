package io.github.mortuusars.exposure.camera.capture;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CaptureManager {
    private static final Queue<Capture> captureQueue = new LinkedList<>();
    @Nullable
    private static Capture currentCapture;

    public static void enqueue(Capture capture) {
        captureQueue.add(capture);
    }

    public static void onRenderTickStart() {
        if (currentCapture == null) {
            currentCapture = captureQueue.poll();
            if (currentCapture == null) {
                return;
            }

            currentCapture.initialize();
        }

        if (currentCapture.isDone()) {
            currentCapture = null;
        }
        else {
            currentCapture.tick();
        }
    }
}
