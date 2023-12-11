package io.github.mortuusars.exposure.network;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.IExposureStorage;
import io.github.mortuusars.exposure.data.transfer.IExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.IExposureSender;

public enum PacketDirection {
    TO_SERVER,
    TO_CLIENT;

    public IExposureStorage getExposureStorage() {
        return this == TO_SERVER ? ExposureServer.getExposureStorage() : ExposureClient.getExposureStorage();
    }

    public IExposureSender getExposureSender() {
        return this == TO_SERVER ? ExposureServer.getExposureSender() : ExposureClient.getExposureSender();
    }

    public IExposureReceiver getExposureReceiver() {
        return this == TO_SERVER ? ExposureServer.getExposureReceiver() : ExposureClient.getExposureReceiver();
    }
}
