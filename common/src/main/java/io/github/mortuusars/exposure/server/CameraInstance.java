package io.github.mortuusars.exposure.server;

import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.core.frame.CaptureProperties;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CameraInstance {
    @Nullable
    private CaptureProperties currentCaptureProperties = null;

    private long captureStartTick = -1;
    private boolean waitingForProjectionResult = false;
    private int projectionTimeoutTicks = -1;
    private ProjectionResult projectionResult = ProjectionResult.TIMED_OUT;

    public void tick(PhotographerEntity photographer, ItemStack stack) {
        if (waitingForProjectionResult
                && projectionTimeoutTicks >= 0
                && photographer.asEntity().level().getGameTime() - captureStartTick > projectionTimeoutTicks) {
            waitingForProjectionResult = false;
            projectionTimeoutTicks = -1;
            projectionResult = ProjectionResult.TIMED_OUT;

            ItemAndStack.executeIfItemMatches(CameraItem.class, stack, cameraItem ->
                    cameraItem.handleProjectionResult(photographer, stack, projectionResult));
        }
    }

    public Optional<CaptureProperties> getCurrentCaptureData() {
        return Optional.ofNullable(currentCaptureProperties);
    }

    public void setCurrentCaptureData(Level level, CaptureProperties captureProperties) {
        this.currentCaptureProperties = captureProperties;
        captureStartTick = level.getGameTime();

        if (captureProperties.fileProjectingInfo().isPresent()) {
            waitForInterplanarProjectionResult(25);
        }
    }

    public CameraInstance waitForInterplanarProjectionResult(int projectionTimeoutTicks) {
        this.projectionTimeoutTicks = projectionTimeoutTicks;
        this.waitingForProjectionResult = true;
        return this;
    }

    public CameraInstance setProjectionResult(boolean isSuccessful) {
        this.projectionResult = isSuccessful ? ProjectionResult.SUCCESSFUL : ProjectionResult.FAILED;
        this.waitingForProjectionResult = false;
        return this;
    }

    public enum ProjectionResult {
        SUCCESSFUL,
        FAILED,
        TIMED_OUT;

        public static ProjectionResult get(@Nullable Boolean result) {
            return result == null ? TIMED_OUT :
                    result ? SUCCESSFUL
                            : FAILED;
        }
    }
}
