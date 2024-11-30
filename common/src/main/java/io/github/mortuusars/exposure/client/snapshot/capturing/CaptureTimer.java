package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;

import java.util.Objects;
import java.util.function.Consumer;

public class CaptureTimer {
    private int ticks;
    private boolean isRunning;
    private long startedTick = -1;
    private long lastTick = -1;

    private Runnable onStart;
    private Consumer<Integer> onTick;
    private Runnable onEnd;

    public CaptureTimer(int ticks) {
        Preconditions.checkState(ticks >= 0, "Number of ticks cannot be negative. Ticks: %s", ticks);
        this.ticks = ticks;
    }

    public CaptureTimer whenStarted(Runnable whenStarted) {
        onStart = whenStarted;
        return this;
    }

    public CaptureTimer onTick(Consumer<Integer> onTick) {
        this.onTick = onTick;
        return this;
    }

    public CaptureTimer whenEnded(Runnable whenEnded) {
        onEnd = whenEnded;
        return this;
    }

    public CaptureTimer start() {
        isRunning = true;
        startedTick = getCurrentTick();
        lastTick = startedTick;
        onStart.run();

        update();

        return this;
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isDone() {
        return !isRunning() && startedTick >= 0 && ticks <= 0;
    }

    public void update() {
        if (isRunning()) {
            long currentTick = getCurrentTick();
            if (lastTick != currentTick) {
                if (ticks <= 0) {
                    stop();
                    onEnd.run();
                    return;
                }

                ticks--;
                onTick.accept(ticks);
                lastTick = currentTick;
            }
        }
    }

    private long getCurrentTick() {
        return Objects.requireNonNull(Minecraft.getInstance().level,
                "Snapshot system can only be used when level is loaded.").getGameTime();
    }
}
