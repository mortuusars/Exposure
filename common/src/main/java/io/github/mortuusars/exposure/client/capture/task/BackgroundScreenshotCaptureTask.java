package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.exposure.client.util.Shader;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.PostChain;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.CompletableFuture;

public class BackgroundScreenshotCaptureTask extends Task<Result<Image>> {
    @Override
    public CompletableFuture<Result<Image>> execute() {
        RenderTarget target = getOrCreateRenderTarget();
        renderToTarget(target);
        WrappedNativeImage image = new WrappedNativeImage(Screenshot.takeScreenshot(target));
        return CompletableFuture.completedFuture(Result.success(image));
    }

    protected @NotNull RenderTarget getOrCreateRenderTarget() {
        int width = Minecrft.get().getWindow().getWidth();
        int height = Minecrft.get().getWindow().getHeight();
        return new TextureTarget(width, height, true, Minecraft.ON_OSX);
    }

    protected void renderToTarget(RenderTarget target) {
        Minecraft minecraft = Minecrft.get();

        RenderTarget mainTarget = minecraft.getMainRenderTarget();

        try {
            minecraft.mainRenderTarget = target;
            target.bindWrite(true);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            FogRenderer.setupNoFog();
            RenderSystem.enableCull();
            minecraft.gameRenderer.renderLevel(minecraft.getTimer());
            applyPostEffects(target);
        } finally {
            minecraft.mainRenderTarget = mainTarget;
            mainTarget.bindWrite(true);
        }
    }

    protected void applyPostEffects(RenderTarget renderTarget) {
        //TODO: Refactor Shader and CaptureShader. It's confusing.
        if (Config.Client.KEEP_POST_EFFECT.get()
              && Minecrft.get().gameRenderer.effectActive
              && Minecrft.get().gameRenderer.currentEffect() instanceof PostChain postEffect) {
            Shader.process(postEffect, renderTarget);
        }

        CaptureShader.process(renderTarget);
    }
}
