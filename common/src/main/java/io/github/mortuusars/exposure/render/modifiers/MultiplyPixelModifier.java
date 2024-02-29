package io.github.mortuusars.exposure.render.modifiers;

@SuppressWarnings({"ClassCanBeRecord", "unused"})
public class MultiplyPixelModifier implements IPixelModifier {
    public final int multiplyColor;

    public MultiplyPixelModifier(int multiplyColor) {
        this.multiplyColor = multiplyColor;
    }

    @Override
    public int modifyPixel(int ABGR) {
        if (multiplyColor == 0)
            return ABGR;

        int alpha = ABGR >> 24 & 0xFF;
        int blue = ABGR >> 16 & 0xFF;
        int green = ABGR >> 8 & 0xFF;
        int red = ABGR & 0xFF;

        int tintAlpha = (multiplyColor >> 24) & 0xFF;
        int tintRed = (multiplyColor >> 16) & 0xFF;
        int tintGreen = (multiplyColor >> 8) & 0xFF;
        int tintBlue = multiplyColor & 0xFF;

        alpha = Math.min(255, (alpha * tintAlpha) / 255);
        blue = Math.min(255, (blue * tintBlue) / 255);
        green = Math.min(255, (green * tintGreen) / 255);
        red = Math.min(255, (red * tintRed) / 255);

        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    @Override
    public String getIdSuffix() {
        return multiplyColor != 0 ? "_tint" + Integer.toHexString(multiplyColor) : "";
    }
}
