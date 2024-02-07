package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class ExposureFabricClientReloadListener extends ExposureClientReloadListener implements IdentifiableResourceReloadListener {
    public static final ResourceLocation ID = Exposure.resource("clear_client_exposures_cache");
    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
