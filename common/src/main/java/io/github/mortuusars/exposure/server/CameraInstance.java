package io.github.mortuusars.exposure.server;

import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class CameraInstance {
    protected final CameraID cameraID;

    private UUID lastPhotographerId = Util.NIL_UUID;
    private int deferredCooldownTicks = 0;
    private long projectionDeadline = -1;
    private ProjectionState projectionState = ProjectionState.IDLE;

    public CameraInstance(CameraID cameraID) {
        this.cameraID = cameraID;
    }

    public void tick(ItemStack stack, PhotographerEntity photographer) {
//        if (waitingForProjectionResult
//                && projectionTimeoutTicks >= 0
//                && photographer.asEntity().level().getGameTime() - captureStartTick > projectionTimeoutTicks) {
//            waitingForProjectionResult = false;
//            projectionTimeoutTicks = -1;
//            projectionResult = ProjectionResult.TIMED_OUT;
//
//            ItemAndStack.executeIfItemMatches(CameraItem.class, stack, cameraItem ->
//                    cameraItem.handleProjectionResult(photographer, stack, projectionResult));
//        }
    }

    // --

    public Optional<PhotographerEntity> getLastPhotographer(Level level) {
        return PhotographerEntity.fromUUID(level, lastPhotographerId);
    }

    public void setPhotographer(PhotographerEntity photographer) {
        lastPhotographerId = photographer.asEntity().getUUID();
    }

    // --

    public int getDeferredCooldown() {
        return deferredCooldownTicks;
    }

    public void setDeferredCooldown(int ticks) {
        deferredCooldownTicks = ticks;
    }

    // --

    public ProjectionState getProjectionState(Level level) {
        if (isWaitingForProjection() && isProjectionTimedOut(level)) {
            return ProjectionState.TIMED_OUT;
        }
        return projectionState;
    }

    public long getProjectionDeadline() {
        return projectionDeadline;
    }

    public void waitForProjection(long deadline) {
        projectionDeadline = deadline;
    }

    public boolean isWaitingForProjection() {
        return projectionDeadline >= 0;
    }

    public boolean isProjectionTimedOut(Level level) {
        return level.getGameTime() > projectionDeadline;
    }

    public void setProjectionResult(Level level, boolean successful) {
        if (isWaitingForProjection() && !isProjectionTimedOut(level)) {
            projectionState = successful ? ProjectionState.SUCCESSFUL : ProjectionState.FAILED;
            projectionDeadline = -1;
        }
    }

    public void stopWaitingForProjection() {
        projectionState = ProjectionState.IDLE;
        projectionDeadline = -1;
    }

    // --

    public boolean canReleaseShutter() {
        return !isWaitingForProjection();
    }

    public boolean canOpenAttachments() {
        return !isWaitingForProjection();
    }

    public enum ProjectionState {
        IDLE,
        WAITING,
        SUCCESSFUL,
        FAILED,
        TIMED_OUT;
    }
}
