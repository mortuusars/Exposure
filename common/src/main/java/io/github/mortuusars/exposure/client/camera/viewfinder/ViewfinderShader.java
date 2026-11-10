package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ViewfinderShader implements AutoCloseable {
    private final Minecraft minecraft;
    private final Camera camera;
    private final Viewfinder viewfinder;

    @Nullable
    private PostChain shader;
    private boolean active;

    public ViewfinderShader(Camera camera, Viewfinder viewfinder) {
        this.minecraft = Minecrft.get();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.update();
    }

    public void apply(ResourceLocation shaderLocation) {
        if (shader != null) {
            if (shader.getName().equals(shaderLocation.toString())) {
                return;
            }

            shader.close();
        }

        try {
            shader = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                    minecraft.getMainRenderTarget(), shaderLocation);
            shader.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            active = true;
        } catch (IOException e) {
            Exposure.LOGGER.warn("Failed to load shader: {}", shaderLocation, e);
            active = false;
        } catch (JsonSyntaxException e) {
            Exposure.LOGGER.warn("Failed to parse shader: {}", shaderLocation, e);
            active = false;
        }
    }

    public void resize(int width, int height) {
        if (shader != null) {
            shader.resize(width, height);
        }
    }

    /**
     * Processes current viewfinder shader (if it is present and active).
     */
    public void process() {
        if (shader != null && active) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            shader.process(minecraft.getTimer().getGameTimeDeltaTicks());
        }
    }

    public void update() {
        setActive(viewfinder.isLookingThrough());
        if (active) {
            ItemStack filterStack = Attachment.FILTER.get(camera.getItemStack()).getForReading();
            Filters.of(Minecrft.registryAccess(), filterStack).map(Filter::shader).ifPresentOrElse(this::apply, this::remove);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void remove() {
        if (shader != null) {
            shader.close();
        }

        shader = null;
    }

    @Override
    public void close() {
        remove();
    }
}
