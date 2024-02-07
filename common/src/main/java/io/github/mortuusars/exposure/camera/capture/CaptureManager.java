package io.github.mortuusars.exposure.camera.capture;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CaptureManager {
    private static final Queue<Capture> captureQueue = new LinkedList<>();
    @Nullable
    private static Capture currentCapture;

    public static void enqueue(Capture capture) {
        captureQueue.add(capture);
    }

    public static void onRenderTickEnd() {
        if (currentCapture == null) {
            currentCapture = captureQueue.poll();
            if (currentCapture != null) {
                currentCapture.initialize();
            }
            return;
        }

        if (currentCapture.isCompleted()) {
            currentCapture = null;
            return;
        }

        currentCapture.tick();
    }
}
