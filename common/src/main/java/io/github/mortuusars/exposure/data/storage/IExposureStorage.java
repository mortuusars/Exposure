package io.github.mortuusars.exposure.data.storage;

import java.util.Optional;

public interface IExposureStorage {
    Optional<ExposureSavedData> getOrQuery(String id);
    void put(String id, ExposureSavedData data);
    void clear();
}
