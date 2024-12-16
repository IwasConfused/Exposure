package io.github.mortuusars.exposure.server;

import io.github.mortuusars.exposure.core.frame.CaptureData;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CameraInstance {
    @Nullable
    private CaptureData currentCaptureData = null;

    private long captureStartTick = -1;
    private boolean waitingForProjectionResult = false;
    private int projectionTimeoutTicks = -1;
    private ProjectionResult projectionResult = ProjectionResult.TIMED_OUT;

    public void tick(LivingEntity photographer, ItemStack stack) {
        if (waitingForProjectionResult
                && projectionTimeoutTicks >= 0
                && photographer.level().getGameTime() - captureStartTick > projectionTimeoutTicks) {
            waitingForProjectionResult = false;
            projectionTimeoutTicks = -1;
            projectionResult = ProjectionResult.TIMED_OUT;

            ItemAndStack.executeIfItemMatches(CameraItem.class, stack, cameraItem ->
                    cameraItem.handleProjectionResult(photographer, stack, projectionResult));
        }
    }

    public Optional<CaptureData> getCurrentCaptureData() {
        return Optional.ofNullable(currentCaptureData);
    }

    public void setCurrentCaptureData(Level level, CaptureData captureData) {
        this.currentCaptureData = captureData;
        captureStartTick = level.getGameTime();

        if (captureData.fileProjectingInfo().isPresent()) {
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
