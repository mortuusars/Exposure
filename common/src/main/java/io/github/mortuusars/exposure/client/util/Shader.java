package io.github.mortuusars.exposure.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class Shader {
    private static boolean suppressViewfinder = false;

    /**
     * Processes specified shader (if it is present and active) to a specified render target.
     * Shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    @SuppressWarnings("deprecation")
    public static void process(@NotNull Identifier shaderId, @NotNull RenderTarget renderTarget) {
        PostChain shader = Minecrft.get().getShaderManager().getPostChain(shaderId, LevelTargetBundle.MAIN_TARGETS);

        if (shader != null) {
            //RenderSystem.disableBlend();
            //RenderSystem.disableDepthTest();
            //RenderSystem.resetTextureMatrix();
            shader.process(renderTarget, Minecraft.getInstance().gameRenderer.resourcePool);
        }
    }

    public static void setSuppressViewfinder(boolean suppress) {
        suppressViewfinder = suppress;
    }

    public static void processForGameRenderer() {
        if (!suppressViewfinder && CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().shader().process();
        }

        if (CaptureShader.hasShader()) {
            CaptureShader.process(Minecrft.get().getMainRenderTarget());
        }
    }
}
