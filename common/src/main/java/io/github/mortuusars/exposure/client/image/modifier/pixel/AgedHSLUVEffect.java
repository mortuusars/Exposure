package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.util.color.converter.HUSLColorConverter;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

// HSB is faster while giving only slightly worse result. HSLUV is slower and creates noticeable freezes when exposure is loaded.
public class AgedHSLUVEffect implements PixelEffect {
    protected final int tintColor;
    protected final double[] tintColorHsluv;
    protected final float tintOpacity;
    protected final int blackPoint;
    protected final int whitePoint;

    /**
     * @param tintColor in 0xXXXXXX rgb format. Only rightmost 24 bits would be used, anything extra will be discarded.
     * @param tintOpacity ratio of the original color to tint color. Like a layer opacity.
     * @param blackPoint Like in a Levels adjustment. 0-255.
     * @param whitePoint Like in a Levels adjustment. 0-255.
     */
    public AgedHSLUVEffect(int tintColor, float tintOpacity, int blackPoint, int whitePoint) {
        this.tintColor = tintColor;
        // dunno why, but this needs to be done for it to tint properly
        int newTintColor = ARGB.fromABGR(tintColor);
        String hexStr = StringUtils.leftPad(Integer.toHexString(newTintColor & 0xFFFFFF), 6, "0");
        this.tintColorHsluv = HUSLColorConverter.hexToHsluv("#" + hexStr);
        this.tintOpacity = tintOpacity;
        this.blackPoint = blackPoint & 0xFF; // 0-255
        this.whitePoint = whitePoint & 0xFF; // 0-255
    }

    @Override
    public String getIdentifier() {
        return "aged";
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        // Modify black and white points to make the image appear faded:
        red = (int) Mth.map(red, 0, 255, blackPoint, whitePoint);
        green = (int) Mth.map(green, 0, 255, blackPoint, whitePoint);
        blue = (int) Mth.map(blue, 0, 255, blackPoint, whitePoint);

        // Apply sepia tone with 'color' blending mode:
        double[] hsluv = HUSLColorConverter.rgbToHsluv(new double[] { red / 255f, green / 255f, blue / 255f });
        hsluv[0] = tintColorHsluv[0]; // Hue
        hsluv[1] = tintColorHsluv[1]; // Saturation

        double[] rgb = HUSLColorConverter.hsluvToRgb(hsluv);

        // Blend two colors together:
        int newRed = Mth.clamp((int) Mth.lerp(tintOpacity, red, rgb[0] * 255), 0, 255);
        int newGreen = Mth.clamp((int) Mth.lerp(tintOpacity, green, rgb[1] * 255), 0, 255);
        int newBlue = Mth.clamp((int) Mth.lerp(tintOpacity, blue, rgb[2] * 255), 0, 255);

        return ARGB.color(alpha, newRed, newGreen, newBlue);
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
