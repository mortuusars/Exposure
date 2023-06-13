package io.github.mortuusars.exposure.storage;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundQueryExposureDataPacket;
import io.github.mortuusars.exposure.network.packet.ServerboundSaveExposurePacket;
import org.jetbrains.annotations.Nullable;

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

        if(exposureData == null) {
            Packets.sendToServer(new ServerboundQueryExposureDataPacket(id));
        }

        return Optional.ofNullable(exposureData);
    }

    public static void set(String id, ExposureSavedData exposureData) {
        clientCache.put(id, exposureData);
    }
}
