package io.github.mortuusars.exposure.storage;

import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.QueryExposureDataServerboundPacket;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClientsideExposureStorage implements IExposureStorage {
    private final Map<String, @NotNull ExposureSavedData> CACHE = new HashMap<>();
    private final List<String> QUERIED_IDS = new ArrayList<>();

    @Override
    public Optional<ExposureSavedData> getOrQuery(String id) {
        ExposureSavedData exposureData = CACHE.get(id);

        if(exposureData == null && !QUERIED_IDS.contains(id)) {
            Packets.sendToServer(new QueryExposureDataServerboundPacket(id));
            QUERIED_IDS.add(id);
        }

        return Optional.ofNullable(exposureData);
    }

    @Override
    public void put(String id, ExposureSavedData data) {
        CACHE.put(id, data);
        QUERIED_IDS.remove(id);
    }

    @Override
    public void clear() {
        CACHE.clear();
        QUERIED_IDS.clear();
    }
}
