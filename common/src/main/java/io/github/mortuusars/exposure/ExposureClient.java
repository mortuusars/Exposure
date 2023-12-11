package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.data.storage.ClientsideExposureStorage;
import io.github.mortuusars.exposure.data.storage.IExposureStorage;
import io.github.mortuusars.exposure.data.transfer.ExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.ExposureSender;
import io.github.mortuusars.exposure.data.transfer.IExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.IExposureSender;
import io.github.mortuusars.exposure.network.Packets;

public class ExposureClient {
    private static final IExposureStorage exposureStorage = new ClientsideExposureStorage();
    private static final ExposureRenderer exposureRenderer = new ExposureRenderer();

    private static IExposureSender exposureSender;
    private static IExposureReceiver exposureReceiver;

    public static void init() {
        exposureSender = new ExposureSender((packet, player) -> Packets.sendToServer(packet));
        exposureReceiver = new ExposureReceiver(exposureStorage);
    }

    public static IExposureStorage getExposureStorage() {
        return exposureStorage;
    }

    public static IExposureSender getExposureSender() {
        return exposureSender;
    }

    public static IExposureReceiver getExposureReceiver() {
        return exposureReceiver;
    }

    public static ExposureRenderer getExposureRenderer() {
        return exposureRenderer;
    }
}
