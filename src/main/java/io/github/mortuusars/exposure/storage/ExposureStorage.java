package io.github.mortuusars.exposure.storage;

import io.github.mortuusars.exposure.network.ExposureSender;

public class ExposureStorage {
    public static final IExposureStorage CLIENT = new ClientsideExposureStorage();
    public static final IExposureStorage SERVER = new ServersideExposureStorage();

    public static void storeClientsideAndSendToServer(String id, ExposureSavedData exposureSavedData) {
        CLIENT.put(id, exposureSavedData);
        ExposureSender.sendToServer(id, exposureSavedData);
    }
}
