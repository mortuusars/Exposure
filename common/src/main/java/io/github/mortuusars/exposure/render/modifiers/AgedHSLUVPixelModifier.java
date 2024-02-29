package io.github.mortuusars.exposure.render.modifiers;

import io.github.mortuusars.exposure.util.HUSLColorConverter;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

public class AgedHSLUVPixelModifier implements IPixelModifier {
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
    public AgedHSLUVPixelModifier(int tintColor, float tintOpacity, int blackPoint, int whitePoint) {
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

        // Apply sepia tone with 'color' blending mode:
        double[] hsluv = HUSLColorConverter.rgbToHsluv(new double[] { red / 255f, green / 255f, blue / 255f });
        hsluv[0] = tintColorHsluv[0]; // Hue
        hsluv[1] = tintColorHsluv[1]; // Saturation

        double[] rgb = HUSLColorConverter.hsluvToRgb(hsluv);

        int newRed = Mth.clamp((int) Mth.lerp(tintOpacity, red, rgb[2] * 255), 0, 255);
        int newGreen = Mth.clamp((int) Mth.lerp(tintOpacity, green, rgb[1] * 255), 0, 255);
        int newBlue = Mth.clamp((int) Mth.lerp(tintOpacity, blue, rgb[0] * 255), 0, 255);

        ABGR = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
        return ABGR;
    }

    @Override
    public String toString() {
        return "AgedHSLUVPixelModifier{" +
                "tintColor=#" + Integer.toHexString(tintColor) +
                ", tintOpacity=" + tintOpacity +
                ", blackPoint=" + blackPoint +
                ", whitePoint=" + whitePoint +
                '}';
    }
}
