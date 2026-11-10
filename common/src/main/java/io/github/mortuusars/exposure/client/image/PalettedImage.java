package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.data.ColorPalettes;
import net.minecraft.resources.ResourceLocation;

public record PalettedImage(int width, int height, byte[] pixels, ColorPalette palette) implements Image {
    public PalettedImage {
        Image.validate(width, height, pixels.length);
    }

    public PalettedImage(int width, int height, byte[] pixels, ResourceLocation paletteId) {
        this(width, height, pixels, ColorPalettes.get(Minecrft.registryAccess(), paletteId).value());
    }

    public static PalettedImage fromExposure(ExposureData exposure) {
        return new PalettedImage(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(), exposure.getPaletteId());
    }

    public int getPixelARGB(int x, int y) {
        int id = pixels[y * width + x] & 0xFF;
        return palette.byId(id);
    }
}
