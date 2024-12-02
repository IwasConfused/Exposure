package io.github.mortuusars.exposure.client.snapshot;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class SnapShot1 {
    // Capture Types:
        // Screenshot (background | direct)
        // File

    // Capture Components:
        // HideGui, ForceFirstPerson, DisablePostEffect
        // Flash (sound and particles)?
        // Interplanar Projection (sound and particles)

// Capture can consist of multiple capture types (with separate components) in fallback system
// Processing is then applied to whatever capture result

    // Image Processing: T process(Image image) contains list of pixel processors
        // <Composite class to handle multiple at once>
        // Crop
        // Resize
    // Pixel Processing: int process(int ARGB)
        // Brightness
        // BlackAndWhite | Selective Channel BW

    // Converting to paletted image: <Converting can also be done with Image Processors, as a next step after modifications>
        // Dithered
        // Nearest

    /*

    SnapShot.setup()
        .capture(Capture.takeScreenshot() // Supplier<T>
            .withComponents(
                CaptureComponents.hideGui(),
                CaptureComponents.forceFirstPersonCamera(),
                CaptureComponents.disablePostEffect()
                CaptureComponents.optional(stops != 0, () -> CaptureComponents.modifyGamma(stops)))
            .overridenBy(Capture.fromFile(filePath)
                .onError(error -> player.display(error))) // send packet to brake projector
        .then(Process.with( // Consumer<T>
            ImageProcessors.crop(getCropFactor(), Crop.CENTER_SQUARE))
            ImageProcessors.resize(size)
            ImageProcessors.modifyPixel(
                PixelModifiers.optional(stops != 0, () -> PixelModifiers.brightness(stops)),
                PixelModifiers.optional(filmType == BW, () -> PixelModifiers.blackAndWhite())
            ),
            ImageProcessors.convert(Converter.DITHER)
        )
        .thenConsume(FileSaver.saveToDefaultFolder()) // Consumer<T2>
        .thenConsume(Uploader.toServer(exposureId))   // Consumer<T2>
        .enqueue()


        ))

     */

    private final Queue<SnapShotTask<TaskResult<?>>> snapshotQueue = new LinkedList<>();
    @Nullable
    private SnapShotTask<TaskResult<?>> currentSnapshot;

    public void enqueue(SnapShotTask<TaskResult<?>> snapshot) {
        Preconditions.checkState(!isQueued(snapshot), "This snapshot is already in queue.");
        snapshotQueue.add(snapshot);
    }

    public boolean isQueued(SnapShotTask<TaskResult<?>> snapshot) {
        return currentSnapshot == snapshot || snapshotQueue.contains(snapshot);
    }

    public void tick() {
        if (currentSnapshot == null) {
            currentSnapshot = snapshotQueue.poll();
            if (currentSnapshot == null) {
                return;
            }

            currentSnapshot.start();
        }

        if (currentSnapshot.isDone()) {
            currentSnapshot = null;
        }
        else if (currentSnapshot.isStarted()) {
            currentSnapshot.tick();
        }
    }

//    public static <T> SnapShotTask<T> invertedFallback(SnapShotTask<T> main, SnapShotTask<T> fallback) {
//        return new SnapShotCompositeTask<>(main, fallback, SnapShotCompositeTask.ExecutionStrategy.INVERTED_FALLBACK);
//    }
}
