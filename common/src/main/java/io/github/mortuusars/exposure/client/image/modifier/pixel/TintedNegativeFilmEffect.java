package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.world.camera.FilmColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class TintedNegativeFilmEffect implements PixelEffect {
    private final FilmColor tintColor;

    public TintedNegativeFilmEffect(FilmColor tintColor) {
        this.tintColor = tintColor;
    }

    @Override
    public String getIdentifier() {
        return "tinted-negative-film-" + tintColor;
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        // Modify opacity to make lighter colors transparent, like in real film.
        int brightness = (red + green + blue) / 3;
        int opacity = (int) Mth.clamp(brightness * 1.5f, 0, 255);
        alpha = (alpha * opacity) / 255;

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        // Tint
        red = (int) (red * tintColor.r() / 255);
        green = (int) (green * tintColor.g() / 255);
        blue = (int) (blue * tintColor.b() / 255);

        return ARGB.color(alpha, red, green, blue);
    }
}
