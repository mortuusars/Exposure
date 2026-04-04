package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.client.image.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StackedScreenshotCaptureTask extends Task<Result<Image>> {
    @Nullable
    private CompletableFuture<Result<Image>> future;

    private final int framesToCapture;
    private final float shutterBrightness;
    private int framesCaptured = 0;
    private final List<NativeImage> frames = new ArrayList<>();
    private long lastGameTick = -1;

    public StackedScreenshotCaptureTask(int durationTicks, float shutterBrightness) {
        this.framesToCapture = Math.max(1, durationTicks);
        this.shutterBrightness = shutterBrightness;
        this.lastGameTick = getCurrentGameTick();
    }

    @Override
    public @NotNull CompletableFuture<Result<Image>> execute() {
        if (future == null) {
            future = new CompletableFuture<>();
        }
        return future;
    }

    @Override
    public void tick() {
        if (future == null || future.isDone()) {
            return;
        }
        if (getCurrentGameTick() == lastGameTick) return;
        lastGameTick = getCurrentGameTick();

        try {
            NativeImage img = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
            frames.add(img);
            framesCaptured++;
        } catch (Exception e) {
            for (NativeImage frame : frames) {
                frame.close();
            }
            future.completeExceptionally(e);
            return;
        }

        if (framesCaptured >= framesToCapture && !future.isDone()) {
            try {
                NativeImage accumulated = accumulateFrames(frames, shutterBrightness);
                future.complete(Result.success(new WrappedNativeImage(accumulated)));
            } catch (Exception e) {
                for (NativeImage frame : frames) {
                    frame.close();
                }
                future.completeExceptionally(e);
            }
        }
    }

    private NativeImage accumulateFrames(List<NativeImage> images, float exposureMultiplier) {
        if (images.isEmpty()) throw new IllegalArgumentException("Cannot accumulate 0 frames");

        int w = images.get(0).getWidth();
        int h = images.get(0).getHeight();
        NativeImage result = new NativeImage(NativeImage.Format.RGBA, w, h, false);

        float perFrameWeight = exposureMultiplier / images.size();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float a = 0, b = 0, g = 0, r = 0;

                for (NativeImage frame : images) {
                    int pixel = frame.getPixelRGBA(x, y);
                    float frameA = ((pixel >> 24) & 0xFF) / 255f;
                    float frameB = ((pixel >> 16) & 0xFF) / 255f;
                    float frameG = ((pixel >> 8) & 0xFF) / 255f;
                    float frameR = (pixel & 0xFF) / 255f;

                    a += frameA;
                    b += frameB * perFrameWeight;
                    g += frameG * perFrameWeight;
                    r += frameR * perFrameWeight;
                }

                // Clamp to valid range [0, 1]
                a = Math.min(1f, Math.max(0f, a / images.size()));
                b = Math.min(1f, Math.max(0f, b));
                g = Math.min(1f, Math.max(0f, g));
                r = Math.min(1f, Math.max(0f, r));

                int outA = (int) (a * 255);
                int outB = (int) (b * 255);
                int outG = (int) (g * 255);
                int outR = (int) (r * 255);

                result.setPixelRGBA(x, y, (outA << 24) | (outB << 16) | (outG << 8) | outR);
            }
        }

        // Close all source frames to prevent memory leaks
        for (NativeImage frame : images) {
            frame.close();
        }

        return result;
    }

    private long getCurrentGameTick() {
        return Minecrft.level().getGameTime();
    }
}
