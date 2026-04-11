package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.client.render.GlassPhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.item.GlassTint;
import io.github.mortuusars.exposure.client.render.item.conditional.ProjectorActive;
import io.github.mortuusars.exposure.client.render.item.range.AlbumPhotos;
import io.github.mortuusars.exposure.client.render.item.range.Channels;
import io.github.mortuusars.exposure.client.render.item.range.Count;
import io.github.mortuusars.exposure.client.render.item.select.StackCameraAttachments;
import io.github.mortuusars.exposure.client.render.item.select.StackCameraStatus;
import io.github.mortuusars.exposure.fabric.resources.ExposureFabricClientReloadListener;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.network.fabric.FabricS2CPacketHandler;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExposureClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Exposure.ID, ConfigurationScreen::new);

        ItemTintSources.ID_MAPPER.put(Exposure.resource("glass_tint"), GlassTint.MAP_CODEC);

        KeyboardHandler.registerKeymappings(KeyBindingHelper::registerKeyBinding);

        MenuScreens.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);

        SelectItemModelProperties.ID_MAPPER.put(ExposureClient.SelectProperties.CAMERA_STATUS, StackCameraStatus.TYPE);
        SelectItemModelProperties.ID_MAPPER.put(ExposureClient.SelectProperties.CAMERA_ATTACHMENTS, StackCameraAttachments.TYPE);

        RangeSelectItemModelProperties.ID_MAPPER.put(ExposureClient.RangeSelectProperties.ALBUM_PHOTOS, AlbumPhotos.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(ExposureClient.RangeSelectProperties.CHANNELS, Channels.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(ExposureClient.RangeSelectProperties.COUNT, Count.MAP_CODEC);

        ConditionalItemModelProperties.ID_MAPPER.put(ExposureClient.ConditionalProperties.PROJECTOR_ACTIVE, ProjectorActive.MAP_CODEC);

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
