package io.github.mortuusars.exposure.storage.saver;

import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExposureStorageSaver implements IExposureSaver {
    @Override
    public void save(String id, byte[] materialColorPixels, int width, int height) {
        ExposureSavedData exposureSavedData =
                new ExposureSavedData(width, height, materialColorPixels);

        ExposureStorage.storeClientsideAndSendToServer(id, exposureSavedData);
    }
}
