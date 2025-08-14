package io.github.mortuusars.exposure.camera.capture.converter;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Objects;

public class SimpleColorConverter implements IImageToMapColorsConverter {

    public static MapColor[] getMapColors() {
        MapColor[] colors = new MapColor[64];
        for (int i = 0; i <= 63; i++){
            colors[i] = MapColor.byId(i);
        }
        return colors;
    }

    @Override
    public byte[] convert(Capture capture, NativeImage image) {
        if (capture.getFilmType() == FilmType.COLOR_POSITIVE) {
            applySaturation(image, 2.0f);
        }
        return convert(image);
    }

    @Override
    public byte[] convert(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        MapColor[] mapColors = Arrays.stream(getMapColors()).filter(Objects::nonNull).toArray(MapColor[]::new);
        byte[] bytes = new byte[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixelRGBA = image.getPixelRGBA(x, y);

                int R = FastColor.ABGR32.red(pixelRGBA);
                int G = FastColor.ABGR32.green(pixelRGBA);
                int B = FastColor.ABGR32.blue(pixelRGBA);
                int A = FastColor.ABGR32.alpha(pixelRGBA);

                if (A == 0) {
                    bytes[x + y * width] = (byte)MapColor.NONE.id;
                }
                else {
                    byte mapColorIndex = (byte)nearestColor(mapColors, R, G, B, A);
                    bytes[x + y * width] = mapColorIndex;
                }
            }
        }

        return bytes;
    }

    private final double[] shadeCoeffs = { 0.71, 0.86, 1.0, 0.53 };


    //AI slop for saturating an image

    private void applySaturation(NativeImage image, float saturationMultiplier) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);

                int alpha = FastColor.ABGR32.alpha(abgr);
                if (alpha == 0) continue;

                int blue = FastColor.ABGR32.blue(abgr);
                int green = FastColor.ABGR32.green(abgr);
                int red = FastColor.ABGR32.red(abgr);

                float[] hsb = rgbToHsb(red, green, blue);

                hsb[1] *= saturationMultiplier;
                hsb[1] = Mth.clamp(hsb[1], 0.0f, 1.0f);

                int newAbgr = hsbToAbgr(hsb[0], hsb[1], hsb[2], alpha);
                image.setPixelRGBA(x, y, newAbgr);
            }
        }
    }

    private static float[] rgbToHsb(int r, int g, int b) {
        float hue, saturation, brightness;
        int cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        int cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        return new float[]{hue, saturation, brightness};
    }

    private static int hsbToAbgr(float hue, float saturation, float brightness, int alpha) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> { r = (int) (brightness * 255.0f + 0.5f); g = (int) (t * 255.0f + 0.5f); b = (int) (p * 255.0f + 0.5f); }
                case 1 -> { r = (int) (q * 255.0f + 0.5f); g = (int) (brightness * 255.0f + 0.5f); b = (int) (p * 255.0f + 0.5f); }
                case 2 -> { r = (int) (p * 255.0f + 0.5f); g = (int) (brightness * 255.0f + 0.5f); b = (int) (t * 255.0f + 0.5f); }
                case 3 -> { r = (int) (p * 255.0f + 0.5f); g = (int) (q * 255.0f + 0.5f); b = (int) (brightness * 255.0f + 0.5f); }
                case 4 -> { r = (int) (t * 255.0f + 0.5f); g = (int) (p * 255.0f + 0.5f); b = (int) (brightness * 255.0f + 0.5f); }
                case 5 -> { r = (int) (brightness * 255.0f + 0.5f); g = (int) (p * 255.0f + 0.5f); b = (int) (q * 255.0f + 0.5f); }
            }
        }
        return FastColor.ABGR32.color(alpha, b, g, r);
    }

    private double[] applyShade(double[] color, int shadeIndex) {
        double coeff = shadeCoeffs[shadeIndex];
        return new double[] { color[0] * coeff, color[1] * coeff, color[2] * coeff };
    }

    private int nearestColor(MapColor[] colors, int r, int g, int b, int a) {
        double[] imageVector = { r / 255.0, g / 255.0, b / 255.0 };

        int best_color = 0;
        double lowest_distance = 10000;
        for (int colorIndex = 0; colorIndex < colors.length; colorIndex++) {
            int mapColor = colors[colorIndex].col;
            int mapR = FastColor.ARGB32.red(mapColor);
            int mapG = FastColor.ARGB32.green(mapColor);
            int mapB = FastColor.ARGB32.blue(mapColor);
            double[] mcColorVector = { mapR / 255.0, mapG / 255.0, mapB / 255.0 };

            for (int shadeInd = 0; shadeInd < shadeCoeffs.length; shadeInd++) {
                double distance = distance(imageVector, applyShade(mcColorVector, shadeInd));
                if (distance < lowest_distance) {
                    lowest_distance = distance;
                    if (colorIndex == 0 && a == 255) {
                        best_color = 119;
                    } else {
                        best_color = colorIndex * shadeCoeffs.length + shadeInd;
                    }
                }
            }
        }
        return best_color;
    }

    private double distance(double[] vectorA, double[] vectorB) {
        return Math.sqrt(Math.pow(vectorA[0] - vectorB[0], 2) + Math.pow(vectorA[1] - vectorB[1], 2)
                + Math.pow(vectorA[2] - vectorB[2], 2));
    }
}
