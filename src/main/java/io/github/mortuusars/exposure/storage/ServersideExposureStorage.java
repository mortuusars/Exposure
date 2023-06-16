package io.github.mortuusars.exposure.storage;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServersideExposureStorage implements IExposureStorage {
    private static final String EXPOSURE_DIR = "exposures";

//    private static final Map<String, byte[]> exposurePartsHolder = new HashMap<>();

    public ServersideExposureStorage() {
        createStorageDirectory();
    }

    @Override
    public Optional<ExposureSavedData> getOrQuery(String id) {
        ExposureSavedData loadedExposureData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .get(ExposureSavedData::load, getSaveId(id));

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

    //    public void receiveExposurePart(String id, int width, int height, int offset, byte[] partBytes) {
//        byte[] exposureBytes = exposurePartsHolder.compute(id, (key, data) ->
//                data == null ? new byte[width * height] : data);
//
//        System.arraycopy(partBytes, 0, exposureBytes, offset, partBytes.length);
//        exposurePartsHolder.put(id, exposureBytes);
//
//        if (offset + partBytes.length >= exposureBytes.length) {
//            put(id, new ExposureSavedData(width, height, exposureBytes));
//            exposurePartsHolder.remove(id);
//        }
//    }
}
