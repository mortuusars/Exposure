package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExposureImage {
    private final String name;
    @Nullable
    private final ExposureSavedData exposureData;
    @Nullable
    private final ExposureTexture texture;

    public ExposureImage(String name, @NotNull ExposureSavedData exposureData) {
        this.name = name;
        this.exposureData = exposureData;
        this.texture = null;
    }

    public ExposureImage(String name, @NotNull ExposureTexture texture) {
        this.name = name;
        this.exposureData = null;
        this.texture = texture;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        if (exposureData != null) {
            return exposureData.getWidth();
        }
        else if (texture != null){
            @Nullable NativeImage image = texture.getImage();
            return image != null ? image.getWidth() : 1;
        }
        throw new IllegalStateException("Neither exposureData nor texture was specified.");
    }

    public int getHeight() {
        if (exposureData != null) {
            return exposureData.getHeight();
        }
        else if (texture != null){
            @Nullable NativeImage image = texture.getImage();
            return image != null ? image.getHeight() : 1;
        }
        throw new IllegalStateException("Neither exposureData nor texture was specified.");
    }

    public int getPixelABGR(int x, int y) {
        if (exposureData != null) {
            return MaterialColor.getColorFromPackedId(exposureData.getPixel(x, y));
        }
        else if (texture != null){
            @Nullable NativeImage image = texture.getImage();
            return image != null ? image.getPixelRGBA(x, y) : 0x00000000;
        }
        throw new IllegalStateException("Neither exposureData nor texture was specified.");
    }
}
