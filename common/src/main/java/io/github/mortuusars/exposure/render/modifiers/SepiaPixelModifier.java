package io.github.mortuusars.exposure.render.modifiers;

import io.github.mortuusars.exposure.util.HUSLColorConverter;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;

import java.awt.*;

public class SepiaPixelModifier implements IPixelModifier {
    @Override
    public int modifyPixel(int ABGR) {

        return blendColors2(ABGR, 0x86b0dd);
//        // Extracting individual color components (ARGB order)
//        int alpha = (ABGR >> 24) & 0xFF;
//        int red = (ABGR >> 16) & 0xFF;
//        int green = (ABGR >> 8) & 0xFF;
//        int blue = ABGR & 0xFF;
//
//        int newRed = (int)Mth.clamp((0.393f * red + 0.769f * green + 0.189f * blue) * 1.25, 0, 255);
//        int newGreen =  (int)Mth.clamp((0.349f * red + 0.686f * green + 0.168f * blue) * 1.12, 0, 255);
//        int newBlue =  (int)Mth.clamp(0.272f * red + 0.534f * green + 0.131f * blue, 0, 255);
//
//        // Combining back to ABGR color
//        ABGR = (alpha << 24) | (newBlue << 16) | (newGreen << 8) | newRed;
//
//        return ABGR;
    }

    public static int blendColors(int colorABGR, int toneColorABGR) {
        // Extract individual color components (A: alpha, B: blue, G: green, R: red)
        int alpha = (colorABGR >> 24) & 0xFF;
        int red = (colorABGR >> 16) & 0xFF;
        int green = (colorABGR >> 8) & 0xFF;
        int blue = colorABGR & 0xFF;

        int toneRed = (toneColorABGR >> 16) & 0xFF;
        int toneGreen = (toneColorABGR >> 8) & 0xFF;
        int toneBlue = toneColorABGR & 0xFF;

        // Blend the colors (using simple average)
        int blendedRed = (red + toneRed) / 2;
        int blendedGreen = (green + toneGreen) / 2;
        int blendedBlue = (blue + toneBlue) / 2;

        // Combine the blended components back into a single ABGR color
        return (alpha << 24) | (blendedRed << 16) | (blendedGreen << 8) | blendedBlue;
    }

    public static int blendColors1(int colorABGR, int toneColorABGR) {
        // Convert ABGR colors to HSL



        int r = (colorABGR >> 16) & 0xFF;
        int g = (colorABGR >> 8) & 0xFF;
        int b = colorABGR & 0xFF;
        int luma = Mth.clamp((int) (0.4 * r + 0.6 * g + 0.15 * b), 0, 255);
        float[] colorHSL = Color.RGBtoHSB(
                r,
                g,
                b,
                null
        );

        float[] toneHSL = Color.RGBtoHSB(
                (toneColorABGR >> 16) & 0xFF,
                (toneColorABGR >> 8) & 0xFF,
                toneColorABGR & 0xFF,
                null
        );

        // Combine hue and saturation from colorABGR, and luminance from toneColorABGR
        float blendedHue = toneHSL[0];
        float blendedSaturation = toneHSL[1];
        float blendedLuminance = luma / 255f;

        // Convert back to RGB
        int blendedRGB = Color.HSBtoRGB(blendedHue, blendedSaturation, blendedLuminance);

        // Set alpha channel to match colorABGR
        int alpha = (colorABGR >> 24) & 0xFF;
        return (alpha << 24) | (blendedRGB & 0xFFFFFF);
    }

    public static int blendColors2(int ABGR, int toneColorABGR) {
        int alpha = (ABGR >> 24) & 0xFF;
        int red = (ABGR >> 16) & 0xFF;
        int green = (ABGR >> 8) & 0xFF;
        int blue = ABGR & 0xFF;

        red = (int) Mth.map(red, 0, 255, 40, 255);
        green = (int) Mth.map(green, 0, 255, 40, 255);
        blue = (int) Mth.map(blue, 0, 255, 40, 255);

        double[] colorHSLUV = HUSLColorConverter.rgbToHsluv(new double[]{red / 255f, green / 255f, blue / 255f});
//        double[] toneHSLUV = HUSLColorConverter.hexToHpluv("#ddb086");
        double[] toneHSLUV = HUSLColorConverter.hexToHsluv("#d9a863");

        colorHSLUV[0] = toneHSLUV[0];
        colorHSLUV[1] = toneHSLUV[1];

//        double blendedHue = toneHSL[0];
//        double blendedSaturation = toneHSL[1];
//        double blendedLuminance = luma / 255f;

        double[] rgb = HUSLColorConverter.hsluvToRgb(colorHSLUV);

        float ratio = 0.7f;

        int newRed = Mth.clamp((int)Mth.lerp(ratio, red, rgb[2] * 255), 0, 255);
        int newGreen = Mth.clamp((int)Mth.lerp(ratio, green, rgb[1] * 255), 0, 255);
        int newBlue = Mth.clamp((int)Mth.lerp(ratio, blue, rgb[0] * 255), 0, 255);

        ABGR = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
        return ABGR;
    }

    @Override
    public String getIdSuffix() {
        return "_sepia";
    }
}
