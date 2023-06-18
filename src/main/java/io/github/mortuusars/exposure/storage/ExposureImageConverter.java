package io.github.mortuusars.exposure.storage;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.material.MaterialColor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExposureImageConverter {
//    private static final Map<Integer, Byte> SHADED_MATERIAL_COLORS;
//
//    static {
//        SHADED_MATERIAL_COLORS = new HashMap<>();
//
//        for (MaterialColor color : getColors()) {
//            for (MaterialColor.Brightness brightness : MaterialColor.Brightness.values()) {
//                int rgb = color.calculateRGBColor(brightness);
//                byte id = color.getPackedId(brightness);
//                SHADED_MATERIAL_COLORS.put(rgb, id);
//            }
//        }
//    }

    private static final List<Pair<Integer, Byte>> COLORS;

    static {
        COLORS = new ArrayList<>();

        for (MaterialColor color : getColors()) {
            for (MaterialColor.Brightness brightness : MaterialColor.Brightness.values()) {
                int rgb = color.calculateRGBColor(brightness);
                byte id = color.getPackedId(brightness);
                COLORS.add(Pair.of(rgb, id));
            }
        }
    }

    public static byte[] toMaterialColors(BufferedImage image) {
        byte[] pixels = new byte[image.getWidth() * image.getHeight()];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);

                byte matColor = 0;
                for (Pair<Integer, Byte> pair : COLORS) {
                    if (pair.getFirst().equals(rgb))
                        matColor = pair.getSecond();
                }

                pixels[x + y * image.getWidth()] = matColor;
            }
        }

        return pixels;
    }

    public static byte[] convert(BufferedImage image) {
        MaterialColor[] colors = getColors();

        byte[] pixels = new byte[image.getWidth() * image.getHeight()];

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                pixels[x + y * image.getWidth()] = (byte) nearestColor(colors, new Color(rgb));
            }
        }

        return pixels;
    }

    private static MaterialColor[] getColors(){
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
