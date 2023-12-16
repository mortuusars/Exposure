package io.github.mortuusars.exposure.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.client.gui.screen.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.render.PhotographEntityRenderer;
import io.github.mortuusars.exposure.command.ClientCommands;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.Commands;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Exposure.initClient();

//        @Nullable CommandDispatcher<FabricClientCommandSource> activeDispatcher = ClientCommandManager.getActiveDispatcher();
//        if (activeDispatcher != null) {
//            activeDispatcher.register(
//                    ClientCommandManager.literal("exposure")
//                            .then(ClientCommandManager.literal("show")
//                                    .then(ClientCommandManager.literal("latest")
//                                            .executes(ClientCommands::showLatest))));
//        }
//
//        ClientCommands.register();


        MenuScreens.register(Exposure.MenuTypes.CAMERA.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);

        ModelLoadingPlugin.register(new ModelLoadingPlugin() {
            @Override
            public void onInitializeModelLoader(Context pluginContext) {
                pluginContext.addModels(new ModelResourceLocation("exposure", "camera_gui", "inventory"));
            }
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new ExposureFabricClientReloadListener());
        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
        TooltipComponentCallback.EVENT.register(data -> data instanceof PhotographTooltip photographTooltip ? photographTooltip : null);

        PacketsImpl.registerS2CPackets();
    }
}
