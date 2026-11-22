package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.ARGB;

public class MultiplyEffect implements PixelEffect {
    protected final int multiplyColor;

    public MultiplyEffect(int multiplyColor) {
        this.multiplyColor = multiplyColor;
    }

    @Override
    public String getIdentifier() {
        return multiplyColor != 0 ? "multiply-" + Integer.toHexString(multiplyColor) : "";
    }

    public int modify(int colorARGB) {
        if (multiplyColor == 0) return colorARGB;

        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        int tintAlpha = ARGB.alpha(colorARGB);
        int tintRed = ARGB.red(colorARGB);
        int tintGreen = ARGB.green(colorARGB);
        int tintBlue = ARGB.blue(colorARGB);

        alpha = Math.min(255, (alpha * tintAlpha) / 255);
        red = Math.min(255, (red * tintRed) / 255);
        green = Math.min(255, (green * tintGreen) / 255);
        blue = Math.min(255, (blue * tintBlue) / 255);

        return ARGB.color(alpha, green, red, blue);
    }
}
