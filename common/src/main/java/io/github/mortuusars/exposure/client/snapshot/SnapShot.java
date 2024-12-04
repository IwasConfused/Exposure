package io.github.mortuusars.exposure.client.snapshot;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.task.Task;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class SnapShot {
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

    private static final Queue<Task<?>> snapshotQueue = new LinkedList<>();
    @Nullable
    private static Task<?> currentTask;

    public static void enqueue(Task<?> snapshot) {
        Preconditions.checkState(!isQueued(snapshot), "This snapshot is already in queue.");
        snapshotQueue.add(snapshot);
    }

    public static boolean isQueued(Task<?> snapshot) {
        return currentTask == snapshot || snapshotQueue.contains(snapshot);
    }

    public static void tick() {
        if (currentTask == null) {
            currentTask = snapshotQueue.poll();
            if (currentTask == null) {
                return;
            }

            currentTask.execute();
        }

        if (currentTask.isDone()) {
            currentTask = null;
        }
        else if (currentTask.isStarted()) {
            currentTask.tick();
        }
    }
}
