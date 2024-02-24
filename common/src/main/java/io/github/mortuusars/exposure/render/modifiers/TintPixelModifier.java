package io.github.mortuusars.exposure.render.modifiers;

public class TintPixelModifier implements IPixelModifier {
    public final int tintColor;

    public TintPixelModifier(int tintColor) {
        this.tintColor = tintColor;
    }

    @Override
    public int modifyPixel(int ABGR) {
        if (tintColor == 0)
            return ABGR;

        int alpha = ABGR >> 24 & 0xFF;
        int blue = ABGR >> 16 & 0xFF;
        int green = ABGR >> 8 & 0xFF;
        int red = ABGR & 0xFF;

        int tintAlpha = (tintColor >> 24) & 0xFF;
        int tintRed = (tintColor >> 16) & 0xFF;
        int tintGreen = (tintColor >> 8) & 0xFF;
        int tintBlue = tintColor & 0xFF;

        alpha = Math.min(255, (alpha * tintAlpha) / 255);
        blue = Math.min(255, (blue * tintBlue) / 255);
        green = Math.min(255, (green * tintGreen) / 255);
        red = Math.min(255, (red * tintRed) / 255);

        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    @Override
    public String getIdSuffix() {
        return tintColor != 0 ? "_tint" + Integer.toHexString(tintColor) : "";
    }
}
