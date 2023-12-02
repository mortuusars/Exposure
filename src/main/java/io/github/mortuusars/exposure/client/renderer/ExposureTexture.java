package io.github.mortuusars.exposure.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ExposureTexture extends SimpleTexture {
    private final SimpleTexture texture;

    public ExposureTexture(ResourceLocation location, SimpleTexture texture) {
        super(location);
        this.texture = texture;
    }

    public @Nullable NativeImage getImage() {
        try {
            return super.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
        } catch (IOException e) {
            Exposure.LOGGER.error("Cannot load texture: " + e);
            return null;
        }
    }
}
