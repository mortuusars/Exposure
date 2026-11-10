package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImageIdentifier;
import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;

public class ResourceImage extends SimpleTexture implements RenderableImage {
    @Nullable
    protected NativeImage image;

    public ResourceImage(ResourceLocation location) {
        super(location);
    }

    public static @NotNull RenderableImage getOrCreate(ResourceLocation location) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        @Nullable AbstractTexture existingTexture = textureManager.byPath.get(location);
        if (existingTexture instanceof ResourceImage resourceImage) {
            return resourceImage;
        }

        try {
            ResourceImage texture = new ResourceImage(location);
            textureManager.register(location, texture);
            return texture;
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Cannot load texture [{}]. {}", location, e);
            return RenderableImage.MISSING;
        }
    }

    @Override
    public int width() {
        @Nullable NativeImage image = getNativeImage();
        return image != null ? image.getWidth() : 1;
    }

    @Override
    public int height() {
        @Nullable NativeImage image = getNativeImage();
        return image != null ? image.getHeight() : 1;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        @Nullable NativeImage image = getNativeImage();
        return image != null ? Color.ABGRtoARGB(image.getPixelRGBA(x, y)) : 0x00000000;
    }

    public @Nullable NativeImage getNativeImage() {
        if (this.image != null)
            return image;

        try {
            NativeImage image = super.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
            this.image = image;
            return image;
        } catch (IOException e) {
            Exposure.LOGGER.error("Cannot load texture: {}", e.toString());
            return null;
        }
    }

    @Override
    public void reset(@NotNull TextureManager textureManager, @NotNull ResourceManager resourceManager,
                      @NotNull ResourceLocation path, @NotNull Executor executor) {
        super.reset(textureManager, resourceManager, path, executor);
        if (image != null) {
            image.close();
            image = null;
        }
    }

    @Override
    public void close() {
        super.close();

        if (this.image != null) {
            image.close();
            image = null;
        }
    }

    @Override
    public Image getImage() {
        return this;
    }

    @Override
    public RenderableImageIdentifier getIdentifier() {
        return new RenderableImageIdentifier(location.toString());
    }
}
