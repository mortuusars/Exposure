package io.github.mortuusars.exposure.client.capture;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CaptureShader {
    @Nullable
    private static PostChain shader = null;

    public static boolean hasShader() {
        return shader != null;
    }

    public static void apply(ResourceLocation shaderLocation) {
        if (shader != null) {
            if (shader.getName().equals(shaderLocation.toString())) {
                return;
            }

            shader.close();
        }

        try {
            Minecraft minecraft = Minecrft.get();
            shader = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                    minecraft.getMainRenderTarget(), shaderLocation);
            shader.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        } catch (IOException e) {
            Exposure.LOGGER.warn("Failed to load shader: {}", shaderLocation, e);
            remove();
        } catch (JsonSyntaxException e) {
            Exposure.LOGGER.warn("Failed to parse shader: {}", shaderLocation, e);
            remove();
        }
    }

    public static void resize(int width, int height) {
        if (shader != null) {
            shader.resize(width, height);
        }
    }

    public static void process() {
        if (shader != null) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            shader.process(Minecrft.get().getTimer().getGameTimeDeltaTicks());
        }
    }

    /**
     * Processes current shader (if it is present and active) to a specified render target.
     * Current shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public static void process(RenderTarget renderTarget) {
        if (shader != null) {
            process(shader, renderTarget);
        }
    }

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
            tempShader.close();
        } catch (IOException e) {
            Exposure.LOGGER.warn("Failed to load shader: {}", shader.getName(), e);
        } catch (JsonSyntaxException e) {
            Exposure.LOGGER.warn("Failed to parse shader: {}", shader.getName(), e);
        }
    }

    public static void remove() {
        if (shader != null) {
            shader.close();
        }

        shader = null;
    }
}
