package io.github.mortuusars.exposure.storage;

import io.github.mortuusars.exposure.network.ExposureSender;

public class ExposureStorage {
    public static final IExposureStorage CLIENT = new ClientsideExposureStorage();
    public static final IExposureStorage SERVER = new ServersideExposureStorage();

    public static void storeOnClient(String id, ExposureSavedData exposureSavedData) {
        CLIENT.put(id, exposureSavedData);
    }

    public static void sendToServer(String id, ExposureSavedData exposureSavedData) {
        ExposureSender.sendToServer(id, exposureSavedData);
    }

    public static void storeOnClientAndSendToServer(String id, ExposureSavedData exposureSavedData) {
        storeOnClient(id, exposureSavedData);
        sendToServer(id, exposureSavedData);
    }
}
