package io.github.mortuusars.exposure.storage;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ServersideExposureStorage implements IExposureStorage {
    private static final String EXPOSURE_DIR = "exposures";

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
        if (createStorageDirectory())
            ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().set(getSaveId(id), data);
        else
            Exposure.LOGGER.error("Exposure is not saved.");
    }

    private String getSaveId(String id) {
        return EXPOSURE_DIR + "/" + id;
    }

    private boolean createStorageDirectory() {
        try {
            Path path = ServerLifecycleHooks.getCurrentServer().getWorldPath(LevelResource.ROOT).resolve("data/" + EXPOSURE_DIR);
            //noinspection ResultOfMethodCallIgnored
            path.toFile().mkdirs();
            return Files.exists(path);
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Failed to create exposure storage directory: " + e);
            return false;
        }
    }

    @Override
    public void clear() { }
}
