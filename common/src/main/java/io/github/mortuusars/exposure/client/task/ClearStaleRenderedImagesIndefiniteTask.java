package io.github.mortuusars.exposure.client.task;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.util.cycles.task.Task;

import java.util.concurrent.CompletableFuture;

public class ClearStaleRenderedImagesIndefiniteTask extends Task<Void> {
    private final CompletableFuture<Void> future = new CompletableFuture<>();

    @Override
    public CompletableFuture<Void> execute() {
        return future;
    }

    @Override
    public void tick() {
        if (Minecrft.get().level != null && Minecrft.get().level.getGameTime() % 2400 == 0) { // 2 minutes
            ExposureClient.renderedExposures().clearStale();
        }
    }
}
