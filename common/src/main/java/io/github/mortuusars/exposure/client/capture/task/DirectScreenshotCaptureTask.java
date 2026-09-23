package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.client.image.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DirectScreenshotCaptureTask extends Task<Result<Image>> {
    // At least 1 frame of delay is needed because some immediate CaptureComponents may only apply on the next frame
    // and in this task we take a screenshot of what's already rendered.
    // BackgroundScreenshotCaptureTask does not have this problem because it renders the level again for himself.
    protected int delay = Math.max(1, Config.Client.DIRECT_CAPTURE_DELAY_FRAMES.get());

    @Nullable
    protected CompletableFuture<Result<Image>> future;

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

        if (delay <= 0) {
            try {
                NativeImage nativeImage = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
                future.complete(Result.success(new WrappedNativeImage(nativeImage)));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }

        delay--;
    }
}
