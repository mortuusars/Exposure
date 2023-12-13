package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.render.PhotographEntityRenderer;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.ItemInHandRenderer;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Exposure.initClient();

        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);

        PacketsImpl.registerS2CPackets();
    }
}
