package io.github.mortuusars.exposure.client.capture;

import com.google.common.base.Preconditions;
import io.github.mortuusars.mortaar.client.Minecrft;

import java.util.function.Consumer;

public class CaptureTimer {
    private int ticks;
    private boolean isRunning;
    private long startedAtGameTick = -1;
    private long lastGameTick = -1;

    private Runnable onStart;
    private Consumer<Integer> onGameTick;
    private Runnable onEnd;

    public CaptureTimer(int ticks) {
        Preconditions.checkState(ticks >= 0, "Number of ticks cannot be negative. Ticks: %s", ticks);
        this.ticks = ticks;
    }

    public CaptureTimer whenStarted(Runnable whenStarted) {
        onStart = whenStarted;
        return this;
    }

    public CaptureTimer onGameTick(Consumer<Integer> onTick) {
        this.onGameTick = onTick;
        return this;
    }

    public CaptureTimer whenEnded(Runnable whenEnded) {
        onEnd = whenEnded;
        return this;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isDone() {
        return !isRunning && startedAtGameTick >= 0 && ticks <= 0;
    }

    public CaptureTimer start() {
        if (!isRunning) {
            isRunning = true;
            startedAtGameTick = getCurrentGameTick();
            lastGameTick = startedAtGameTick;
            onStart.run();
        }

        tick();

        return this;
    }

    public void pause() {
        isRunning = false;
    }

    public void tick() {
        if (isRunning) {
            long currentGameTick = getCurrentGameTick();
            boolean isNewTick = lastGameTick != currentGameTick;
            if (isNewTick) {
                ticks--;
                onGameTick.accept(ticks);
                lastGameTick = currentGameTick;
            }

            if (ticks <= 0) {
                stopAndEnd();
            }
        }
    }

    private void stopAndEnd() {
        isRunning = false;
        onEnd.run();
    }

    private long getCurrentGameTick() {
        return Minecrft.levelOrThrow().getGameTime();
    }
}
