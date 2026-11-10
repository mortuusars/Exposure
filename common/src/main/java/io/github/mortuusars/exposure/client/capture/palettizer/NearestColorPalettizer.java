package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.mortaar.util.color.Color;
import io.github.mortuusars.exposure.client.image.PalettedImage;

public class NearestColorPalettizer implements Palettizer {
    @Override
    public PalettedImage palettize(Image image, ColorPalette palette) {
        int width = image.width();
        int height = image.height();

        byte[] indexedPixels = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getPixelARGB(x, y);
                int colorIndex = Color.alpha(color) == 0 ? 255 : palette.closestTo(Color.argb(color));
                indexedPixels[x + y * width] = (byte)colorIndex;
            }
        }

        return new PalettedImage(width, height, indexedPixels, palette);
    }
}
