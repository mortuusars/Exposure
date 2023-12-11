package io.github.mortuusars.exposure.data.storage;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public class ServersideExposureStorage implements IExposureStorage {
    private static final String EXPOSURE_DIR = "exposures";

    private final Supplier<DimensionDataStorage> levelStorageSupplier;
    private final Supplier<Path> worldPathSupplier;

    public ServersideExposureStorage(Supplier<DimensionDataStorage> levelStorageSupplier, Supplier<Path> worldPathSupplier) {
        this.levelStorageSupplier = levelStorageSupplier;
        this.worldPathSupplier = worldPathSupplier;
    }

    @Override
    public Optional<ExposureSavedData> getOrQuery(String id) {
        DimensionDataStorage dataStorage = levelStorageSupplier.get();
        @Nullable ExposureSavedData loadedExposureData = dataStorage.get(ExposureSavedData::load, getSaveId(id));

        if (loadedExposureData == null)
            LogUtils.getLogger().error("Exposure '" + id + "' was not loaded. File does not exist or some error occurred.");

        return Optional.ofNullable(loadedExposureData);
    }

    @Override
    public void put(String id, ExposureSavedData data) {
        if (createStorageDirectory()) {
            DimensionDataStorage dataStorage = levelStorageSupplier.get();
            dataStorage.set(getSaveId(id), data);
            data.setDirty();
        }
        else
            LogUtils.getLogger().error("Exposure is not saved.");
    }

    private String getSaveId(String id) {
        return EXPOSURE_DIR + "/" + id;
    }

    private boolean createStorageDirectory() {
        try {
            Path path = worldPathSupplier.get().resolve("data/" + EXPOSURE_DIR);
            return Files.exists(path) || path.toFile().mkdirs();
        }
        catch (Exception e) {
            LogUtils.getLogger().error("Failed to create exposure storage directory: " + e);
            return false;
        }
    }

    @Override
    public void clear() {
        LogUtils.getLogger().warn("Clearing Server Exposure Storage is not implemented.");
    }
}
