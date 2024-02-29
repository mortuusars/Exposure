package io.github.mortuusars.exposure.render.modifiers;

import io.github.mortuusars.exposure.util.HUSLColorConverter;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class AgedHSBPixelModifier implements IPixelModifier {
    public final int tintColor;
    public final double[] tintColorHsluv;
    public final float tintOpacity;
    public final int blackPoint;
    public final int whitePoint;

    /**
     * @param tintColor in 0xXXXXXX rgb format. Only rightmost 24 bits would be used, anything extra will be discarded.
     * @param tintOpacity ratio of the original color to tint color. Like a layer opacity.
     * @param blackPoint Like in a Levels adjustment. 0-255.
     * @param whitePoint Like in a Levels adjustment. 0-255.
     */
    public AgedHSBPixelModifier(int tintColor, float tintOpacity, int blackPoint, int whitePoint) {
        this.tintColor = tintColor;
        String hexStr = StringUtils.leftPad(Integer.toHexString(tintColor & 0xFFFFFF), 6, "0");
        this.tintColorHsluv = HUSLColorConverter.hexToHsluv("#" + hexStr);
        this.tintOpacity = tintOpacity;
        this.blackPoint = blackPoint & 0xFF; // 0-255
        this.whitePoint = whitePoint & 0xFF; // 0-255
    }

    @Override
    public String getIdSuffix() {
        return "_sepia";
    }

    @Override
    public int modifyPixel(int ABGR) {
        int alpha = (ABGR >> 24) & 0xFF;
        int red = (ABGR >> 16) & 0xFF;
        int green = (ABGR >> 8) & 0xFF;
        int blue = ABGR & 0xFF;

        // Raise black point to make the image appear faded:
        red = (int) Mth.map(red, 0, 255, blackPoint, whitePoint);
        green = (int) Mth.map(green, 0, 255, blackPoint, whitePoint);
        blue = (int) Mth.map(blue, 0, 255, blackPoint, whitePoint);

        float[] baseHSB = new float[3];
        Color.RGBtoHSB(red, green, blue, baseHSB);

        Color tint = new Color(tintColor);
        float[] tintHSB = new float[3];
        Color.RGBtoHSB(tint.getRed(), tint.getGreen(), tint.getBlue(), tintHSB);

        // Luma is no 100% correct. It's brighter than it would have been originally, but brighter looks better.
        int luma = Mth.clamp((int) (0.45 * red + 0.65 * green + 0.2 * blue), 0, 255);
        int rgb = Color.HSBtoRGB(tintHSB[0], tintHSB[1], luma / 255f);

        // Blend two colors together:
        int newRed = Mth.clamp((int) Mth.lerp(tintOpacity, red, rgb & 0xFF), 0, 255);
        int newGreen = Mth.clamp((int) Mth.lerp(tintOpacity, green, (rgb >> 8) & 0xFF), 0, 255);
        int newBlue = Mth.clamp((int) Mth.lerp(tintOpacity, blue, (rgb >> 16) & 0xFF), 0, 255);

        ABGR = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
        return ABGR;
    }

    @Override
    public String toString() {
        return "AgedHSBPixelModifier{" +
                "tintColor=#" + Integer.toHexString(tintColor) +
                ", tintOpacity=" + tintOpacity +
                ", blackPoint=" + blackPoint +
                ", whitePoint=" + whitePoint +
                '}';
    }
}
