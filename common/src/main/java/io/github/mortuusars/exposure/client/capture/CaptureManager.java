package io.github.mortuusars.exposure.client.capture;

import io.github.mortuusars.exposure.camera.capture.Capture;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CaptureManager {
    private final Queue<Capture> captureQueue = new LinkedList<>();
    @Nullable
    private Capture currentCapture;

    public void enqueue(Capture capture) {
        captureQueue.add(capture);
    }

    public void onRenderTickStart() {
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
