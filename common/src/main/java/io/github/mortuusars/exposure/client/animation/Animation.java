package io.github.mortuusars.exposure.client.animation;

import io.github.mortuusars.mortaar.util.Easing;

public class Animation {
    protected final int duration;
    protected final Easing easing;

    protected long startedAt;

    public Animation(int duration, Easing easing) {
        this.duration = duration;
        this.easing = easing;
        startedAt = getCurrentTime();
    }

    public Animation(int duration) {
        this.duration = duration;
        this.easing = Easing.LINEAR;
        startedAt = getCurrentTime();
    }

    public int getDuration() {
        return duration;
    }

    public Easing getEasing() {
        return easing;
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

    public void resetProgress() {
        startedAt = getCurrentTime();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
