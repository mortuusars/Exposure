package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.data.storage.IExposureStorage;
import io.github.mortuusars.exposure.data.storage.ServersideExposureStorage;
import io.github.mortuusars.exposure.data.transfer.ExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.ExposureSender;
import io.github.mortuusars.exposure.data.transfer.IExposureReceiver;
import io.github.mortuusars.exposure.data.transfer.IExposureSender;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public class ExposureServer {
    private static IExposureStorage exposureStorage;
    private static IExposureSender exposureSender;
    private static IExposureReceiver exposureReceiver;
    public static void init(MinecraftServer server) {
        exposureStorage = new ServersideExposureStorage(() -> server.overworld().getDataStorage(),
                () -> server.getWorldPath(LevelResource.ROOT));
        exposureSender = new ExposureSender((packet, player) -> Packets.sendToClient(packet, ((ServerPlayer) player)));
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
}
