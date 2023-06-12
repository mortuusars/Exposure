package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundSaveMapDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MapDataPhotoStore {
    public static final int MAP_SIZE = 128;

    @SuppressWarnings("UnnecessaryLocalVariable")
    public String[][] saveImage(BufferedImage image, String id) {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            throw new IllegalStateException("Cannot save image: Minecraft.getInstance().level is null.");

        Player player = Minecraft.getInstance().player;
        if (player == null)
            throw new IllegalStateException("Cannot save image: Minecraft.getInstance().player is null.");

        int rows = (int)Math.ceil( Math.max(image.getWidth(), image.getHeight()) / (float) MAP_SIZE);
        int columns = rows;

        int xStart = (image.getWidth() - (MAP_SIZE * rows)) / 2;
        int yStart = (image.getHeight() - (MAP_SIZE * columns)) / 2;

        MaterialColor[] mapColors = getColors();

        String[][] parts = new String[columns][rows];

        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {

                int partX = Math.min(row * MAP_SIZE, image.getWidth());
                int partY = Math.min(column * MAP_SIZE, image.getHeight());
                int partWidth = Math.min(MAP_SIZE, image.getWidth() - partX);
                int partHeight = Math.min(MAP_SIZE, image.getHeight() - partX);
                BufferedImage part = image.getSubimage(partX, partY, partWidth, partHeight);

                MapItemSavedData mapData = createMapData(player);

                for (int x = 0; x < MAP_SIZE; x++) {
                    for (int y = 0; y < MAP_SIZE; y++) {
                        byte color = x >= xStart && y >= yStart && x < image.getWidth() + xStart && y < image.getHeight() + yStart ?
                                (byte) nearestColor(mapColors, new Color(part.getRGB(x, y)))
                                : 0;

                        mapData.colors[x + y * 128] = color;
                    }
                }

                mapData.setDirty();

                String mapId = id + "_" + row + column;

                level.setMapData(mapId, mapData);
                CompoundTag mapTag = new CompoundTag();
                mapData.save(mapTag);
                Packets.sendToServer(new ServerboundSaveMapDataPacket(mapTag, mapId));

                parts[column][row] = mapId;
            }
        }

        return parts;
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
}
