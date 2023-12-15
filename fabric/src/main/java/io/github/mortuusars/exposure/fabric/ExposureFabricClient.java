package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.client.render.PhotographEntityRenderer;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Exposure.initClient();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ExposureFabricClientReloadListener());
        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
        TooltipComponentCallback.EVENT.register(data -> data instanceof PhotographTooltip photographTooltip ? photographTooltip : null);

        PacketsImpl.registerS2CPackets();
    }
}
