package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundTakeImagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MapPhotography {
    public void saveImage(BufferedImage bufferedImage, String id) {

//        CompletableFuture.runAsync(() -> {
            try {
                LocalPlayer player = Minecraft.getInstance().player;
                ClientLevel level = player.clientLevel;

                bufferedImage = crop(bufferedImage, bufferedImage.getHeight(), bufferedImage.getHeight());

                    int quad = bufferedImage.getHeight() / 2;

                    for (int i = 1; i <= 4; i++) {
                        BufferedImage quadrant;

                        if (i == 1)
                            quadrant = bufferedImage.getSubimage(0, 0, quad, quad);
                        else if (i == 2)
                            quadrant = bufferedImage.getSubimage(quad, 0, quad, quad);
                        else if (i == 3)
                            quadrant = bufferedImage.getSubimage(0, quad, quad, quad);
                        else
                            quadrant = bufferedImage.getSubimage(quad, quad, quad, quad);

                        MapItemSavedData mapData = render(quadrant, createMapData(player), true);

//                MapItemSavedData mapData = render(bufferedImage, createMapData(player), true);

                        String id1 = id + "_" + i;
                        level.setMapData(id1, mapData);

                CompoundTag mapTag = new CompoundTag();
                mapData.save(mapTag);

                Packets.sendToServer(new ServerboundTakeImagePacket(mapTag, id1));
                    }
            }
            catch (Exception e) {
                Exposure.LOGGER.error(e.toString());
            }
//        });
    }

    private MapItemSavedData createMapData(Player player) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("dimension", player.getLevel().dimension().location().toString());
        nbt.putInt("xCenter", (int) player.getX());
        nbt.putInt("zCenter", (int) player.getZ());
        nbt.putBoolean("locked", true);
        nbt.putBoolean("unlimitedTracking", false);
        nbt.putBoolean("trackingPosition", false);
        nbt.putByte("scale", (byte) 3);
        return MapItemSavedData.load(nbt);
    }

    public static MapItemSavedData render(BufferedImage image, MapItemSavedData state, boolean dither) {
        Image resizedImage = image.getScaledInstance(128, 128, Image.SCALE_DEFAULT);
        BufferedImage resized = convertToBufferedImage(resizedImage);
        int width = resized.getWidth();
        int height = resized.getHeight();
        int[][] pixels = convertPixelArray(resized);
        MaterialColor[] mapColors = getColors();
        Color imageColor;
        mapColors = Arrays.stream(mapColors).filter(Objects::nonNull).toArray(MaterialColor[]::new);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageColor = new Color(pixels[j][i], true);
                if (dither)
                    state.colors[i + j * width] = (byte) floydDither(mapColors, pixels, i, j, imageColor);
                else
                    state.colors[i + j * width] = (byte) nearestColor(mapColors, imageColor);
            }
        }
        return state;
    }

    private static int floydDither(MaterialColor[] mapColors, int[][] pixels, int x, int y, Color imageColor) {
        // double[] imageVec = { (double) imageColor.getRed() / 255.0, (double)
        // imageColor.getGreen() / 255.0,
        // (double) imageColor.getBlue() / 255.0 };
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

    private static class NegatableColor {
        public final int r;
        public final int g;
        public final int b;

        public NegatableColor(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    private static Color mapColorToRGBColor(MaterialColor[] colors, int color) {
        Color mcColor = new Color(colors[color >> 2].col);
        double[] mcColorVec = { (double) mcColor.getRed(), (double) mcColor.getGreen(), (double) mcColor.getBlue() };
        double coeff = shadeCoeffs[color & 3];
        return new Color((int) (mcColorVec[0] * coeff), (int) (mcColorVec[1] * coeff), (int) (mcColorVec[2] * coeff));
    }

    public static MaterialColor[] getColors(){
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

    private static int[][] convertPixelArray(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();

        int[][] result = new int[height][width];
        final int pixelLength = 4;
        for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
            argb += ((int) pixels[pixel + 1] & 0xff); // blue
            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    private static BufferedImage convertToBufferedImage(Image image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public BufferedImage crop(BufferedImage bufferedImage,int targetWidth, int targetHeight) throws IOException {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        // Coordinates of the image's middle
        int xc = (width - targetWidth) / 2;
        int yc = (height - targetHeight) / 2;

        // Crop
        BufferedImage croppedImage = bufferedImage.getSubimage(xc, yc, targetWidth, targetHeight);
        return croppedImage;
    }
}
