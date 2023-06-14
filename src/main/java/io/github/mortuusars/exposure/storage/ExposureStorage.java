package io.github.mortuusars.exposure.storage;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundQueryExposureDataPacket;
import io.github.mortuusars.exposure.network.packet.ServerboundSaveExposurePacket;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExposureStorage {
    private static final Map<String, ExposureSavedData> clientCache = new HashMap<>();
    private static final List<String> queriedExposures = new ArrayList<>();

    public static String getSaveNameFromId(String id) {
        return "exposure_" + id;
    }

    public static String getIdFromSaveName(String name) {
        Preconditions.checkArgument(name != null && name.length() > 0, "name cannot be null or empty.");
        return name.substring(8, name.length() - 1);
    }

    public static void saveServerside(String id, ExposureSavedData exposureData) {
        Packets.sendToServer(new ServerboundSaveExposurePacket(id, exposureData));
    }

    public static void save(String id, ExposureSavedData exposureData) {
        clientCache.put(id, exposureData);
        saveServerside(id, exposureData);
    }

    public static Optional<ExposureSavedData> get(String id) {
        ExposureSavedData exposureData = clientCache.get(id);

        if (exposureData != null) {
            BufferedImage img = new BufferedImage(exposureData.getWidth(), exposureData.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

            for (int y = 0; y < exposureData.getHeight(); y++) {
                for (int x = 0; x < exposureData.getWidth(); x++) {

                    img.setRGB(x, y, MaterialColor.getColorFromPackedId(exposureData.getPixel(x, y)));

                }
            }
        }

        if(exposureData == null && !queriedExposures.contains(id)) {
            Packets.sendToServer(new ServerboundQueryExposureDataPacket(id));
        }

        return Optional.ofNullable(exposureData);
    }

    public static void set(String id, ExposureSavedData exposureData) {
        clientCache.put(id, exposureData);

        if (exposureData != null) {
            BufferedImage img = new BufferedImage(exposureData.getWidth(), exposureData.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < exposureData.getHeight(); y++) {
                for (int x = 0; x < exposureData.getWidth(); x++) {

                    img.setRGB(x, y, exposureData.getPixel(x, y));

                }
            }

            File outputFile = new File("exposures/" + id + "asdasdasd.png"); //TODO: world subfolder
            try {
                outputFile.mkdirs();
                ImageIO.write(img, "png", outputFile);
            } catch (IOException e) {
                Exposure.LOGGER.error(e.toString());
            }
        }
    }
}
