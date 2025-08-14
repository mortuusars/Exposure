package io.github.mortuusars.exposure.camera.capture.converter;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.util.Color;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Objects;

public class DitheringColorConverter implements IImageToMapColorsConverter {
    private record NegatableColor(int r, int g, int b) {}

    protected FilmType filmType;

    public DitheringColorConverter setFilmType(FilmType type) {
        this.filmType = type;
        return this;
    }

    public static MapColor[] getMapColors() {
        MapColor[] colors = new MapColor[64];
        for (int i = 0; i <= 63; i++){
            colors[i] = MapColor.byId(i);
        }
        return colors;
    }

    @Override
    public byte[] convert(Capture capture, NativeImage image) {

        if (capture.getFilmType() == FilmType.COLOR_POSITIVE || filmType == FilmType.COLOR_POSITIVE) {
            applySaturation(image, 2.0f);
        }

        return convert(image);
    }

    //AI slop for increasing saturation
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

    // --- Lightweight, AWT-Free HSB/RGB Conversion Methods ---

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

    @Override
    public byte[] convert(NativeImage image) {
        if (filmType == FilmType.COLOR_POSITIVE) {
            applySaturation(image, 2.0f);
        }
        int[][] pixels = convertToPixelArray(image);
        return convert(pixels);
    }

    public byte[] convert(int[][] pixels) {

        MapColor[] mapColors = Arrays.stream(getMapColors()).filter(Objects::nonNull).toArray(MapColor[]::new);

        int width = pixels[0].length;
        int height = pixels.length;

        byte[] bytes = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color imageColor = new Color(pixels[y][x], true);

                byte b = (byte) floydDither(mapColors, pixels, x, y, imageColor);

                if (imageColor.getAlpha() == 0)
                    b = (byte)MapColor.NONE.id;

                bytes[x + y * width] = b;
            }
        }

        return bytes;
    }

    private int floydDither(MapColor[] mapColors, int[][] pixels, int x, int y, Color imageColor) {
        int colorIndex = nearestColor(mapColors, imageColor);
        Color palletedColor = mapColorToRGBColor(mapColors, colorIndex);
        NegatableColor error = new NegatableColor(imageColor.getRed() - palletedColor.getRed(),
                imageColor.getGreen() - palletedColor.getGreen(), imageColor.getBlue() - palletedColor.getBlue());
        if (pixels[0].length > x + 1) {
            Color pixelColor = new Color(pixels[y][x + 1], true);
            pixels[y][x + 1] = applyError(pixelColor, error, 7.0 / 16.0);
        }
        if (pixels.length > y + 1) {
            if (x > 0) {
                Color pixelColor = new Color(pixels[y + 1][x - 1], true);
                pixels[y + 1][x - 1] = applyError(pixelColor, error, 3.0 / 16.0);
            }
            Color pixelColor = new Color(pixels[y + 1][x], true);
            pixels[y + 1][x] = applyError(pixelColor, error, 5.0 / 16.0);
            if (pixels[0].length > x + 1) {
                pixelColor = new Color(pixels[y + 1][x + 1], true);
                pixels[y + 1][x + 1] = applyError(pixelColor, error, 1.0 / 16.0);
            }
        }

        return colorIndex;
    }

    private int applyError(Color pixelColor, NegatableColor error, double quantConst) {
        int pR = Mth.clamp(pixelColor.getRed() + (int) ((double) error.r * quantConst), 0, 255);
        int pG = Mth.clamp(pixelColor.getGreen() + (int) ((double) error.g * quantConst), 0, 255);
        int pB = Mth.clamp(pixelColor.getBlue() + (int) ((double) error.b * quantConst), 0, 255);
        return new Color(pR, pG, pB, pixelColor.getAlpha()).getRGB();
    }

    private Color mapColorToRGBColor(MapColor[] colors, int color) {
        Color mcColor = new Color(colors[color >> 2].col);
        double[] mcColorVec = { mcColor.getRed(), mcColor.getGreen(), mcColor.getBlue() };
        double coeff = shadeCoeffs[color & 3];
        return new Color((int) (mcColorVec[0] * coeff), (int) (mcColorVec[1] * coeff), (int) (mcColorVec[2] * coeff));
    }

    private final double[] shadeCoeffs = { 0.71, 0.86, 1.0, 0.53 };

    private double[] applyShade(double[] color, int ind) {
        double coeff = shadeCoeffs[ind];
        return new double[] { color[0] * coeff, color[1] * coeff, color[2] * coeff };
    }

    private int nearestColor(MapColor[] colors, Color imageColor) {
        double[] imageVec = { (double) imageColor.getRed() / 255.0, (double) imageColor.getGreen() / 255.0,
                (double) imageColor.getBlue() / 255.0 };
        int best_color = 0;
        double lowest_distance = 10000;
        for (int k = 0; k < colors.length; k++) {
            Color mcColor = new Color(colors[k].col);
            double[] mcColorVec = { (double) mcColor.getRed() / 255.0, (double) mcColor.getGreen() / 255.0,
                    (double) mcColor.getBlue() / 255.0 };
            for (int shadeInd = 0; shadeInd < shadeCoeffs.length; shadeInd++) {
                double distance = distance(imageVec, applyShade(mcColorVec, shadeInd));
                if (distance < lowest_distance) {
                    lowest_distance = distance;
                    if (k == 0 && imageColor.getAlpha() == 255) {
                        best_color = 119;
                    } else {
                        best_color = k * shadeCoeffs.length + shadeInd;
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

    private int[][] convertToPixelArray(NativeImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = Color.BGRtoRGB(image.getPixelRGBA(x, y));
                result[y][x] = rgba;
            }
        }

        return result;
    }
}
