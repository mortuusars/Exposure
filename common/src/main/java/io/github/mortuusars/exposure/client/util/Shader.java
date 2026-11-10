package io.github.mortuusars.exposure.client.util;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Shader {
    private static boolean suppressViewfinder = false;

    /**
     * Processes specified shader (if it is present and active) to a specified render target.
     * Shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public static void process(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {
        try {
            ResourceLocation shaderLocation = ResourceLocation.parse(shader.getName());

            PostChain tempShader = new PostChain(Minecrft.get().getTextureManager(), Minecrft.get().getResourceManager(),
                    renderTarget, shaderLocation);
            tempShader.resize(renderTarget.width, renderTarget.height);

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            tempShader.process(Minecrft.get().getTimer().getGameTimeDeltaTicks());
        } catch (IOException e) {
            Exposure.LOGGER.warn("Failed to load shader: {}", shader.getName(), e);
        } catch (JsonSyntaxException e) {
            Exposure.LOGGER.warn("Failed to parse shader: {}", shader.getName(), e);
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
            CaptureShader.process();
        }
    }

    public static void resize(int width, int height) {
        if (CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().shader().resize(width, height);
        }

        if (CaptureShader.hasShader()) {
            CaptureShader.resize(width, height);
        }
    }
}
