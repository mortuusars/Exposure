package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.client.render.GlassPhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.fabric.resources.ExposureFabricClientReloadListener;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.network.fabric.FabricS2CPacketHandler;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExposureClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Exposure.ID, ConfigurationScreen::new);

        ColorProviderRegistry.ITEM.register(CameraItem::getGlassTintColor, Exposure.Items.CAMERA.get());

        KeyboardHandler.registerKeymappings(KeyBindingHelper::registerKeyBinding);

        MenuScreens.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);

        ModelLoadingPlugin.register(pluginContext ->
                ExposureClient.Models.MODELS.forEach(model -> pluginContext.addModels(model.id())));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ExposureFabricClientReloadListener());

        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), PhotographFrameEntityRenderer::new);
        EntityRendererRegistry.register(Exposure.EntityTypes.CLEAR_PHOTOGRAPH_FRAME.get(), GlassPhotographFrameEntityRenderer::new);
        EntityRendererRegistry.register(Exposure.EntityTypes.CAMERA_STAND.get(), CameraStandEntityRenderer::new);

        TooltipComponentCallback.EVENT.register(data -> data instanceof PhotographTooltip photographTooltip
                ? new PhotographClientTooltip(photographTooltip) : null);

        HudRenderCallback.EVENT.register(CameraStandTooltip::render);

        FabricS2CPacketHandler.register();
    }
}
