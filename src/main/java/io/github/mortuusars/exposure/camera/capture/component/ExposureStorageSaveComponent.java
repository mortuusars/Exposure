package io.github.mortuusars.exposure.camera.capture.component;

import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;

public class ExposureStorageSaveComponent implements ICaptureComponent {
    private final String exposureId;
    private final boolean sendToServer;

    public ExposureStorageSaveComponent(String exposureId, boolean sendToServer) {
        this.exposureId = exposureId;
        this.sendToServer = sendToServer;
    }

    @Override
    public void save(byte[] materialColorPixels, int width, int height, FilmType filmType) {
        ExposureSavedData exposureSavedData = new ExposureSavedData(width, height, materialColorPixels, filmType, false);

        ExposureStorage.storeOnClient(exposureId, exposureSavedData);
        if (sendToServer)
            ExposureStorage.sendToServer(exposureId, exposureSavedData);
    }
}
