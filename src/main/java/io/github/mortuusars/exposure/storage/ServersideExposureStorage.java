package io.github.mortuusars.exposure.storage;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.Optional;

public class ServersideExposureStorage implements IExposureStorage {
    private static final String EXPOSURE_DIR = "exposures";

    public ServersideExposureStorage() {
        createStorageDirectory();
    }

    @Override
    public Optional<ExposureSavedData> getOrQuery(String id) {
        ExposureSavedData loadedExposureData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .get(ExposureSavedData::load, getSaveId(id));

        if (loadedExposureData == null)
            Exposure.LOGGER.error("Exposure '" + id + "' was not loaded. File does not exist or some error occurred.");

        return Optional.ofNullable(loadedExposureData);
    }

    @Override
    public void put(String id, ExposureSavedData data) {
        data.setDirty();
        ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().set(getSaveId(id), data);
    }

    private String getSaveId(String id) {
        return EXPOSURE_DIR + "/" + id;
    }

    private boolean createStorageDirectory() {
        try {
            File file = ServerLifecycleHooks.getCurrentServer().getWorldPath(LevelResource.ROOT).resolve("data/" + EXPOSURE_DIR).toFile();
            return file.mkdirs();
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Failed to create exposure storage directory: " + e);
            return false;
        }
    }

    @Override
    public void clear() { }
}
