package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.ARGB;

public class NegativeEffect implements PixelEffect {
    @Override
    public String getIdentifier() {
        return "negative";
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        return ARGB.color(alpha, red, green, blue);
    }
}
