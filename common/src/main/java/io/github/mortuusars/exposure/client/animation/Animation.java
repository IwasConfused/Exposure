package io.github.mortuusars.exposure.client.animation;

public class Animation {
    protected final int duration;
    protected final EasingFunction easing;

    protected long startedAt;

    public Animation(int duration, EasingFunction easing) {
        this.duration = duration;
        this.easing = easing;
        startedAt = getCurrentTime();
    }

    public Animation(int duration) {
        this.duration = duration;
        this.easing = EasingFunction.LINEAR;
        startedAt = getCurrentTime();
    }

    public void resetProgress() {
        startedAt = getCurrentTime();
    }

    public double getValue() {
        if (isFinished()) {
            return 1.0;
        }
        long currentTime = getCurrentTime();
        double value = (double) (currentTime - startedAt) / duration;
        return easing.ease(value);
    }

    public boolean isFinished() {
        return getCurrentTime() >= startedAt + duration;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
