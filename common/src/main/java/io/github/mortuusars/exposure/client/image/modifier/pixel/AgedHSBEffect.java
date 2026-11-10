package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class AgedHSBEffect implements PixelEffect {
    protected final int tintColor;
    protected final float tintOpacity;
    protected final int blackPoint;
    protected final int whitePoint;

    /**
     * @param tintColor in 0xXXXXXX rgb format. Only rightmost 24 bits would be used, anything extra will be discarded.
     * @param tintOpacity ratio of the original color to tint color. Like a layer opacity.
     * @param blackPoint Like in a Levels adjustment. 0-255.
     * @param whitePoint Like in a Levels adjustment. 0-255.
     */
    public AgedHSBEffect(int tintColor, float tintOpacity, int blackPoint, int whitePoint) {
        this.tintColor = tintColor;
        this.tintOpacity = tintOpacity;
        this.blackPoint = blackPoint & 0xFF; // 0-255
        this.whitePoint = whitePoint & 0xFF; // 0-255
    }

    @Override
    public String getIdentifier() {
        return "aged";
    }

    public int modify(int ARGB) {
        int alpha = FastColor.ARGB32.alpha(ARGB);
        int red = FastColor.ARGB32.red(ARGB);
        int green = FastColor.ARGB32.green(ARGB);
        int blue = FastColor.ARGB32.blue(ARGB);

        // Modify black and white points to make the image appear faded:
        red = (int) Mth.map(red, 0, 255, blackPoint, whitePoint);
        green = (int) Mth.map(green, 0, 255, blackPoint, whitePoint);
        blue = (int) Mth.map(blue, 0, 255, blackPoint, whitePoint);

        float[] baseHSB = new float[3];
        Color.HSB.RGBtoHSB(red, green, blue, baseHSB);

        float[] tintHSB = new float[3];
        Color.HSB.RGBtoHSB(FastColor.ARGB32.red(tintColor), FastColor.ARGB32.green(tintColor), FastColor.ARGB32.blue(tintColor), tintHSB);

        // Luma is brighter than it would have been originally, but brighter looks better.
        int luma = Mth.clamp((int) (0.45 * red + 0.65 * green + 0.2 * blue), 0, 255);
        int tintedRGB = Color.HSB.HSBtoRGB(tintHSB[0], tintHSB[1], luma / 255f);

        // Blend two colors together:
        int newBlue = Mth.clamp((int) Mth.lerp(tintOpacity, blue, FastColor.ARGB32.blue(tintedRGB)), 0, 255);
        int newGreen = Mth.clamp((int) Mth.lerp(tintOpacity, green, FastColor.ARGB32.green(tintedRGB)), 0, 255);
        int newRed = Mth.clamp((int) Mth.lerp(tintOpacity, red, FastColor.ARGB32.red(tintedRGB)), 0, 255);

        return FastColor.ARGB32.color(alpha, newRed, newGreen, newBlue);
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
