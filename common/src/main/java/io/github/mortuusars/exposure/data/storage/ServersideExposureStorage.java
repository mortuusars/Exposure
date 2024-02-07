package io.github.mortuusars.exposure.data.storage;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    }

    public List<String> getAllIds() {
        // Save exposures that are in cache and waiting to be saved:
        levelStorageSupplier.get().save();

        Path path = worldPathSupplier.get().resolve("data/" + EXPOSURE_DIR);
        File folder = path.toFile();

        @Nullable File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return Collections.emptyList();

        List<String> ids = new ArrayList<>();

        for (File file : listOfFiles) {
            if (file != null && file.isFile())
                ids.add(com.google.common.io.Files.getNameWithoutExtension(file.getName()));
        }

        return ids;
    }

    @Override
    public void clear() {
        LogUtils.getLogger().warn("Clearing Server Exposure Storage is not implemented.");
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
}
