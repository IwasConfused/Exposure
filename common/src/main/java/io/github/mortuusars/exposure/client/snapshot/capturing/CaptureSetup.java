package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.snapshot.capturing.component.CaptureComponent;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CaptureSetup {
    protected List<CaptureComponent> components = new ArrayList<>();
    protected int delayTicks = 0;
    protected long initializedAt = -1;
    protected long lastTick = -1;

    public boolean isInitialized() {
        return initializedAt >= 0;
    }

    public boolean canCapture() {
        return isInitialized() && delayTicks <= 0;
    }

    public void tick() {
        Preconditions.checkState(isInitialized(), "Should be initialized first before ticking.");
        long currentTick = getCurrentTick();
        if (lastTick != currentTick) {
            delayTicks--;
            delayTick(delayTicks);
            lastTick = currentTick;
        }
    }

    public void initialize() {
        initializedAt = getCurrentTick();
        lastTick = initializedAt;

        for (CaptureComponent component : components) {
            this.delayTicks = Math.max(this.delayTicks, component.requiredDelayTicks());
        }

        for (CaptureComponent component : components) {
            component.initialize();
        }
    }

    public void delayTick(int ticksLeft) {
        for (CaptureComponent component : components) {
            component.delayTick(ticksLeft);
        }
    }

    public void beforeCapture() {
        for (CaptureComponent component : components) {
            component.beforeCapture();
        }
    }

    public void afterCapture() {
        for (CaptureComponent component : components) {
            component.afterCapture();
        }
    }

    private long getCurrentTick() {
        return Objects.requireNonNull(Minecraft.getInstance().level,
                "Snapshot system can only be used when level is loaded.").getGameTime();
    }
}
