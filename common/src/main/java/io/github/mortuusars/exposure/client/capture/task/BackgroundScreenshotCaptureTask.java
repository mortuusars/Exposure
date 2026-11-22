package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.util.Shader;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Captures a screenshot without showing it on screen. Makes photographing a seamless experience™.
 */
public class BackgroundScreenshotCaptureTask extends Task<Result<Image>> {
    public static boolean capturing = false;
    public static @Nullable RenderTarget renderTarget = null;

    @Override
    public CompletableFuture<Result<Image>> execute() {
        if (ExposureClient.shouldUseDirectCapture()) {
            Exposure.LOGGER.warn("BackgroundScreenshotCaptureMethod is used while Iris or Oculus is installed. " +
                    "Captured image most likely will not look as expected.");
        }

        Minecraft minecraft = Minecrft.get();
        CompletableFuture<Result<Image>> future = new CompletableFuture<>();
        renderTarget = minecraft.getMainRenderTarget();

        try {
            capturing = true;
            minecraft.gameRenderer.setRenderBlockOutline(false);
            minecraft.gameRenderer.updateCamera(minecraft.getDeltaTracker());
            minecraft.gameRenderer.renderLevel(minecraft.getDeltaTracker());
            applyShaderEffects(renderTarget);
            Screenshot.takeScreenshot(renderTarget, nativeImage ->
                    future.complete(Result.success(new WrappedNativeImage(nativeImage))));
            return future;
        } catch (Exception e) {
            Exposure.LOGGER.error("Couldn't capture image: ", e);
            future.complete(Result.error(Capture.ERROR_FAILED_GENERIC));
            return future;
        } finally {
            minecraft.gameRenderer.setRenderBlockOutline(true);
            renderTarget = null;
            capturing = false;
        }
    }

    private void applyShaderEffects(RenderTarget renderTarget) {
        @Nullable Identifier effect = Minecraft.getInstance().gameRenderer.currentPostEffect();
        if (effect != null && Minecraft.getInstance().gameRenderer.effectActive) {
            Shader.process(effect, renderTarget);
        }

        CaptureShader.process(renderTarget);
    }
}
