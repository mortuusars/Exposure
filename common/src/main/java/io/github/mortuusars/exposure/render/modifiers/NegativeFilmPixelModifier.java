package io.github.mortuusars.exposure.render.modifiers;

import net.minecraft.util.Mth;

@SuppressWarnings("ClassCanBeRecord")
public class NegativeFilmPixelModifier implements IPixelModifier {
    public final boolean simulateFilmTransparency;

    public NegativeFilmPixelModifier(boolean simulateFilmTransparency) {
        this.simulateFilmTransparency = simulateFilmTransparency;
    }

    @Override
    public String getIdSuffix() {
        return simulateFilmTransparency ? "_negative_transparent" : "_negative";
    }

    @Override
    public int modifyPixel(int ABGR) {
        int alpha = ABGR >> 24 & 0xFF;
        int blue = ABGR >> 16 & 0xFF;
        int green = ABGR >> 8 & 0xFF;
        int red = ABGR & 0xFF;

        // Invert
        alpha = 255 - alpha;
        blue = 255 - blue;
        green = 255 - green;
        red = 255 - red;

        if (simulateFilmTransparency) {
            // Modify opacity to make lighter colors transparent, like in real film.
            int brightness = (blue + green + red) / 3;
            int opacity = (int) Mth.clamp(brightness * 1.5f, 0, 255);
            alpha = (alpha * opacity) / 255;
        }

        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    @Override
    public String toString() {
        return "NegativeFilmPixelModifier{simulateTransparency=" + simulateFilmTransparency + '}';
    }
}
