package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.world.level.storage.ExposureRepository;
import io.github.mortuusars.exposure.world.level.storage.ExposureFrameHistory;
import net.minecraft.server.MinecraftServer;

public class ExposureServer {
    private static ExposureRepository repository;
    private static ExposureFrameHistory frameHistory;

    public static boolean debugHighlightEntitiesInFrame;

    public static void init(MinecraftServer server) {
        repository = new ExposureRepository(server);
        frameHistory = ExposureFrameHistory.loadOrCreate(server);
    }

    // --

    public static ExposureRepository exposureRepository() {
        return Preconditions.checkNotNull(repository, "Cannot get ExposureRepository: server is not initialized yet.");
    }

    public static ExposureFrameHistory frameHistory() {
        return Preconditions.checkNotNull(frameHistory, "Cannot get ExposureFrameHistory: server is not initialized yet.");
    }
}
