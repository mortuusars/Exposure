package io.github.mortuusars.exposure.camera.processing;

import net.minecraft.world.level.material.MaterialColor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

public class RGBToMapColorConverter {
    public static byte[] convert(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        MaterialColor[] mapColors = Arrays.stream(getMaterialColors()).filter(Objects::nonNull).toArray(MaterialColor[]::new);
        byte[] bytes = new byte[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color imageColor = new Color(image.getRGB(x, y), false);
                byte mapColorIndex = (byte)nearestColor(mapColors, imageColor);
                bytes[x + y * width] = mapColorIndex;
            }
        }

        return bytes;
    }

    public static MaterialColor[] getMaterialColors(){
        MaterialColor[] colors = new MaterialColor[64];
        for (int i = 0; i<= 63; i++){
            colors[i] = MaterialColor.byId(i);
        }
        return colors;
    }

    private static final double[] shadeCoeffs = { 0.71, 0.86, 1.0, 0.53 };

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
}
