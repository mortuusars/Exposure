package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ExposureFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Exposure.init();

        ServerLifecycleEvents.SERVER_STARTING.register(Exposure::initServer);

        PacketsImpl.registerC2SPackets();
    }
}
