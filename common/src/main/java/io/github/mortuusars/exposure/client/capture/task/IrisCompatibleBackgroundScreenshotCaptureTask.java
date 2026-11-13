package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.integration.iris.IrisCompat;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IrisCompatibleBackgroundScreenshotCaptureTask extends BackgroundScreenshotCaptureTask {
    private static @Nullable RenderTarget target;

    @Override
    protected @NotNull RenderTarget getOrCreateRenderTarget() {
        int width = Minecrft.get().getWindow().getWidth();
        int height = Minecrft.get().getWindow().getHeight();
        if (target == null) {
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        } else if (target.width != width || target.height != height) {
            target.resize(width, height, Minecraft.ON_OSX);
        }
        return target;
    }

    @Override
    protected void renderToTarget(RenderTarget target) {
        Minecraft minecraft = Minecrft.get();

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        IrisCompat.RenderSystemState oldRenderState = IrisCompat.RenderSystemState.store();

        try {
            minecraft.mainRenderTarget = target;
            target.bindWrite(true);

            IrisCompat.onRenderTargetBound(target);

            try (IrisCompat.CaptureScope ignored = IrisCompat.beginBackgroundCapture()) {
                // Still not sure if clearing is needed or not
                // RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                FogRenderer.setupNoFog();
                RenderSystem.enableCull();
                minecraft.gameRenderer.renderLevel(minecraft.getTimer());
                applyPostEffects(target);
            }
            Exposure.LOGGER.info("Captured using Iris-compatible background task.");
        } finally {
            // Still not sure if clearing is needed or not
            // RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            minecraft.mainRenderTarget = mainTarget;
            mainTarget.bindWrite(true);
            oldRenderState.restore();
        }
    }
}
