package io.github.mortuusars.exposure.client.cycles;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.task.Task;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class Cycles {
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
