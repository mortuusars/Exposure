package io.github.mortuusars.exposure.client.capture;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CaptureShader {
    @Nullable
    private static Identifier shaderId;

    public static boolean hasShader() {
        return shaderId != null;
    }

    public static void apply(Identifier shaderLocation) {
        shaderId = shaderLocation;
    }

    public static void process() {
        if (shaderId != null) {
            //RenderSystem.disableBlend();
            //RenderSystem.disableDepthTest();
            //RenderSystem.resetTextureMatrix();
            PostChain postChain = Minecrft.get().getShaderManager().getPostChain(shaderId, LevelTargetBundle.MAIN_TARGETS);

            postChain.process(Minecrft.get().getMainRenderTarget(), Minecrft.get().gameRenderer.resourcePool);
        }
    }

    /**
     * Processes current shader (if it is present and active) to a specified render target.
     * Current shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public static void process(RenderTarget renderTarget) {
        if (shaderId != null) {
            process(shaderId, renderTarget, Minecraft.getInstance().gameRenderer.resourcePool);
        }
    }

    /**
     * Processes specified shader (if it is present and active) to a specified render target.
     * Shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public static void process(@NotNull Identifier shaderId, @NotNull RenderTarget renderTarget, @NotNull GraphicsResourceAllocator resourceAllocator) {
        @Nullable PostChain postChain = Minecrft.get().getShaderManager().getPostChain(shaderId, LevelTargetBundle.MAIN_TARGETS);
        if (postChain != null) {
            //RenderSystem.disableBlend();
            //RenderSystem.disableDepthTest();
            //RenderSystem.resetTextureMatrix();
            postChain.process(renderTarget, resourceAllocator);
        }
    }

    public static void remove() {
        shaderId = null;
    }
}
