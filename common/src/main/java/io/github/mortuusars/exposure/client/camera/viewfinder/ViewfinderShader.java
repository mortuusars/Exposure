package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ViewfinderShader {
    private final Minecraft minecraft;
    private final Camera camera;
    private final Viewfinder viewfinder;

    @Nullable
    private Identifier shaderId;
    private boolean active;

    public ViewfinderShader(Camera camera, Viewfinder viewfinder) {
        this.minecraft = Minecrft.get();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.update();
    }

    public void apply(Identifier shaderLocation) {
        shaderId = shaderLocation;
        active = true;
    }

    /**
     * Processes current viewfinder shader (if it is present and active).
     */
    @SuppressWarnings("deprecation")
    public void process() {
        if (shaderId != null && active) {
            PostChain shader = Minecrft.get().getShaderManager().getPostChain(shaderId, LevelTargetBundle.MAIN_TARGETS);
            if (shader != null) {
//                //RenderSystem.disableBlend();
//                //RenderSystem.disableDepthTest();
//                //RenderSystem.resetTextureMatrix();

                shader.process(this.minecraft.getMainRenderTarget(), Minecrft.get().gameRenderer.resourcePool);
            } else {
                Exposure.LOGGER.warn("Failed to get shader: {}", shaderId);
            }
        }
    }

    public void update() {
        setActive(viewfinder.isLookingThrough());
        if (active) {
            ItemStack filterStack = Attachment.FILTER.get(camera.getItemStack()).getForReading();
            Filters.of(Minecrft.registryAccess(), filterStack).map(Filter::shader).ifPresent(this::apply);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
