package io.github.mortuusars.exposure.client.snapshot;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class SnapshotManager {
    private final Queue<SnapShot> snapshotQueue = new LinkedList<>();
    @Nullable
    private SnapShot currentSnapshot;

    public void enqueue(SnapShot snapshot) {
        Preconditions.checkState(!isQueued(snapshot), "This snapshot is already in queue.");
        snapshotQueue.add(snapshot);
    }

    public boolean isQueued(SnapShot snapshot) {
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
}
