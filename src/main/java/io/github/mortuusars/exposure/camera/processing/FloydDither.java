package io.github.mortuusars.exposure.camera.processing;

import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

public class FloydDither {
    private record NegatableColor(int r, int g, int b) {}

    public static byte[] dither(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = convertToPixelArray(image);
        MaterialColor[] mapColors = Arrays.stream(getMaterialColors()).filter(Objects::nonNull).toArray(MaterialColor[]::new);

        byte[] bytes = new byte[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color imageColor = new Color(pixels[y][x], false);
                byte b = (byte) floydDither(mapColors, pixels, x, y, imageColor);
                bytes[x + y * width] = b;
            }
        }

        return bytes;
    }
    private static int floydDither(MaterialColor[] mapColors, int[][] pixels, int x, int y, Color imageColor) {
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

    private static int applyError(Color pixelColor, NegatableColor error, double quantConst) {
        int pR = Mth.clamp(pixelColor.getRed() + (int) ((double) error.r * quantConst), 0, 255);
        int pG = Mth.clamp(pixelColor.getGreen() + (int) ((double) error.g * quantConst), 0, 255);
        int pB = Mth.clamp(pixelColor.getBlue() + (int) ((double) error.b * quantConst), 0, 255);
        return new Color(pR, pG, pB, pixelColor.getAlpha()).getRGB();
    }

    private static Color mapColorToRGBColor(MaterialColor[] colors, int color) {
        Color mcColor = new Color(colors[color >> 2].col);
        double[] mcColorVec = { mcColor.getRed(), mcColor.getGreen(), mcColor.getBlue() };
        double coeff = shadeCoeffs[color & 3];
        return new Color((int) (mcColorVec[0] * coeff), (int) (mcColorVec[1] * coeff), (int) (mcColorVec[2] * coeff));
    }

    public static MaterialColor[] getMaterialColors(){
        MaterialColor[] colors = new MaterialColor[64];
        for (int i = 0; i<= 63; i++){
            colors[i] = MaterialColor.byId(i);
        }
        return colors;
    }

    private static final double shadeCoeffs[] = { 0.71, 0.86, 1.0, 0.53 };

    private static double[] applyShade(double[] color, int ind) {
        double coeff = shadeCoeffs[ind];
        return new double[] { color[0] * coeff, color[1] * coeff, color[2] * coeff };
    }

    private static int nearestColor(MaterialColor[] colors, Color imageColor) {
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
                    // todo: handle shading with alpha values other than 255
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

    private static double distance(double[] vectorA, double[] vectorB) {
        return Math.sqrt(Math.pow(vectorA[0] - vectorB[0], 2) + Math.pow(vectorA[1] - vectorB[1], 2)
                + Math.pow(vectorA[2] - vectorB[2], 2));
    }

    private static int[][] convertToPixelArray(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                result[y][x] = rgb;
            }
        }


//        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        final int width = image.getWidth();
//        final int height = image.getHeight();
//
//        int[][] result = new int[height][width];
//        final int pixelLength = 4;
//        for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
//            int r = (pixels[pixel + 3] & 0xff) << 16;
//            int g = (pixels[pixel + 2] & 0xff) << 8;
//            int b = (pixels[pixel + 1] & 0xff);
//
////            int argb = 0;
////            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
////            argb += ((int) pixels[pixel + 1] & 0xff); // blue
////            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
////            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
//            result[row][col] = 0xFF << 24 | r << 16 | g << 8 | b;
//
//            col++;
//            if (col == width) {
//                col = 0;
//                row++;
//            }
//        }

        return result;
    }

//    private static BufferedImage convertToBufferedImage(Image image) {
//        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
//                BufferedImage.TYPE_4BYTE_ABGR);
//        Graphics2D g = newImage.createGraphics();
//        g.drawImage(image, 0, 0, null);
//        g.dispose();
//        return newImage;
//    }
}
