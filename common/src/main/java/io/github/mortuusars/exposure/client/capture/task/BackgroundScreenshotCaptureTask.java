package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import io.github.mortuusars.exposure.Config;
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
import net.minecraft.client.renderer.PostChain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Captures a screenshot without showing it on screen. Makes photographing a seamless experience™.
 */
public class BackgroundScreenshotCaptureTask extends Task<Result<Image>> {
    private static boolean capturing = false;
    private static @Nullable RenderTarget renderTarget = null;

    public static boolean isCapturing() {
        return capturing && renderTarget != null;
    }

    public static @NotNull RenderTarget getRenderTarget() {
        return Objects.requireNonNull(renderTarget);
    }

    // --

    @Override
    public CompletableFuture<Result<Image>> execute() {
        if (ExposureClient.shouldUseDirectCapture()) {
            Exposure.LOGGER.warn("BackgroundScreenshotCaptureMethod is used while incompatible mods are installed. " +
                    "Captured image most likely will not look as expected.");
        }

        Minecraft minecraft = Minecrft.get();

        renderTarget = new TextureTarget(minecraft.getWindow().getWidth(),
                minecraft.getWindow().getHeight(), true, Minecraft.ON_OSX);
        renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        renderTarget.clear(Minecraft.ON_OSX);

        try {
            capturing = true;

            if (Config.Client.BACKGROUND_CAPTURE_USE_PANORAMIC_MODE.get()) {
                // Panoramic mode was enabled by default before to make water visible on images, but it seems to not be needed anymore.
                // But keeping this as a setting might not hurt.
                minecraft.gameRenderer.setPanoramicMode(true);
            }

            minecraft.gameRenderer.setRenderBlockOutline(false);

            minecraft.levelRenderer.graphicsChanged();
            renderTarget.bindWrite(false);
            minecraft.gameRenderer.renderLevel(minecraft.getTimer());

            applyShaderEffects(renderTarget);

            WrappedNativeImage image = new WrappedNativeImage(Screenshot.takeScreenshot(renderTarget));

            return CompletableFuture.completedFuture(Result.success(image));
        } catch (Exception e) {
            Exposure.LOGGER.error("Couldn't capture image: ", e);
            return CompletableFuture.completedFuture(Result.error(Capture.ERROR_FAILED_GENERIC));
        } finally {
            if (Config.Client.BACKGROUND_CAPTURE_USE_PANORAMIC_MODE.get()) {
                minecraft.gameRenderer.setPanoramicMode(false);
            }
            minecraft.gameRenderer.setRenderBlockOutline(true);
            renderTarget.destroyBuffers();
            renderTarget = null;
            capturing = false;
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }

    private void applyShaderEffects(RenderTarget renderTarget) {
        @Nullable PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
        if (effect != null && Minecraft.getInstance().gameRenderer.effectActive) {
            Shader.process(effect, renderTarget);
        }

        CaptureShader.process(renderTarget);
    }
}
