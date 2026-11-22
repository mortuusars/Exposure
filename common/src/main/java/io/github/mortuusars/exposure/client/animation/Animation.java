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

    public int getDuration() {
        return duration;
    }

    public EasingFunction getEasing() {
        return easing;
    }

    public float getValue() {
        if (isFinished()) {
            return 1.0f;
        }
        long currentTime = getCurrentTime();
        double value = (double) (currentTime - startedAt) / duration;
        return (float) easing.ease(value);
    }

    public boolean isFinished() {
        return getCurrentTime() >= startedAt + duration;
    }

    public void resetProgress() {
        startedAt = getCurrentTime();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
