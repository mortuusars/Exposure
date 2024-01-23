package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;

public class ExposureTexture extends SimpleTexture {
    @Nullable
    private NativeImage image;

    public ExposureTexture(ResourceLocation location) {
        super(location);
    }

    public static @Nullable ExposureTexture getTexture(ResourceLocation location) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        @Nullable AbstractTexture existingTexture = textureManager.byPath.get(location);
        if (existingTexture != null) {
            return existingTexture instanceof ExposureTexture exposureTexture ? exposureTexture : null;
        }

        try {
            ExposureTexture texture = new ExposureTexture(location);
            textureManager.register(location, texture);
            return texture;
        }
        catch (Exception e) {
            LogUtils.getLogger().error("Cannot load texture [" + location + "]. " + e);
            return null;
        }
    }

    public @Nullable NativeImage getImage() {
        if (this.image != null)
            return image;

        try {
            NativeImage image = super.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
            this.image = image;
            return image;
        } catch (IOException e) {
            LogUtils.getLogger().error("Cannot load texture: " + e);
            return null;
        }
    }

    @Override
    public void reset(@NotNull TextureManager pTextureManager, @NotNull ResourceManager pResourceManager, @NotNull ResourceLocation pPath, @NotNull Executor pExecutor) {
        super.reset(pTextureManager, pResourceManager, pPath, pExecutor);
        image = null;
    }

    @Override
    public void close() {
        super.close();

        if (this.image != null) {
            image.close();
            image = null;
        }
    }
}
