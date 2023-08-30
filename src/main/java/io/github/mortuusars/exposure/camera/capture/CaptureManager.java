package io.github.mortuusars.exposure.camera.capture;


import com.mojang.datafixers.util.Pair;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CaptureManager {
    private static final Queue<Capture> captureQueue = new LinkedList<>();
    @Nullable
    private static Capture currentCapture;

    public static void enqueue(Capture capture) {
        captureQueue.add(capture);
    }

    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END))
            return;

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
