package io.github.mortuusars.exposure.client.renderer;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExposureImage {
    private final String name;
    @Nullable
    private final ExposureSavedData exposureData;
    @Nullable
    private final TextureAtlasSprite texture;

    public ExposureImage(String name, @NotNull ExposureSavedData exposureData) {
        this.name = name;
        this.exposureData = exposureData;
        this.texture = null;
    }

    public ExposureImage(String name, @NotNull TextureAtlasSprite texture) {
        this.name = name;
        this.exposureData = null;
        this.texture = texture;
    }

    public int getWidth() {
        if (exposureData != null) {
            return exposureData.getWidth();
        }
        else if (texture != null){
            return texture.contents().width();
        }
        throw new IllegalStateException("Neither exposureData nor texture was specified.");
    }

    public int getHeight() {
        if (exposureData != null) {
            return exposureData.getHeight();
        }
        else if (texture != null){
            return texture.contents().height();
        }
        throw new IllegalStateException("Neither exposureData nor texture was specified.");
    }

    public int getPixelABGR(int x, int y) {
        if (exposureData != null) {
            return MapColor.getColorFromPackedId(exposureData.getPixel(x, y));
        }
        else if (texture != null){
            return texture.getPixelRGBA(0, x, y);
        }
        throw new IllegalStateException("Neither exposureData nor texture was specified.");
    }

    public void validate() {
        if (texture != null) {
            ResourceLocation name = texture.contents().name();
            if (name.toString().equals("minecraft:missingno")) {
                Exposure.LOGGER.warn("Texture [" + this.name + "] is not found.");
            }
        }
    }
}
