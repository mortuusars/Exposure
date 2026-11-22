package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.neoforge.PlatformHelperClientImpl;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.client.render.GlassPhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.item.GlassTint;
import io.github.mortuusars.exposure.client.render.item.conditional.ProjectorActive;
import io.github.mortuusars.exposure.client.render.item.range.AlbumPhotos;
import io.github.mortuusars.exposure.client.render.item.range.Channels;
import io.github.mortuusars.exposure.client.render.item.range.Count;
import io.github.mortuusars.exposure.client.render.item.select.StackCameraAttachments;
import io.github.mortuusars.exposure.client.render.item.select.StackCameraStatus;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.network.neoforge.PacketsImpl;
import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;

@SuppressWarnings("unused")
public class NeoForgeClientEvents {
    @EventBusSubscriber(modid = Exposure.ID, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
            S2CPackets.getDefinitions().forEach(definition ->
                    event.register((CustomPacketPayload.Type<Packet>) definition.type(), PacketsImpl::handle));
            CommonPackets.getDefinitions().forEach(definition ->
                    event.register((CustomPacketPayload.Type<Packet>) definition.type(), PacketsImpl::handle));
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ExposureClient::init);
        }

        @SubscribeEvent
        public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
            event.register(Exposure.resource("glass_tint"), GlassTint.MAP_CODEC);
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
            event.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
            event.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
            event.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
            event.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
            event.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), PhotographFrameEntityRenderer::new);
            event.registerEntityRenderer(Exposure.EntityTypes.CLEAR_PHOTOGRAPH_FRAME.get(), GlassPhotographFrameEntityRenderer::new);
            event.registerEntityRenderer(Exposure.EntityTypes.CAMERA_STAND.get(), CameraStandEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(PhotographTooltip.class, PhotographClientTooltip::new);
        }

        @SubscribeEvent
        public static void addClientReloadListeners(AddClientReloadListenersEvent event) {
            event.addListener(Exposure.resource("reload_listener"), new ExposureClientReloadListener());
        }

        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterStandalone event) {
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_SMALL);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_MEDIUM);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_LARGE);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.CAMERA_STAND);
            PlatformHelperClientImpl.registerModel(event, ExposureClient.Models.CAMERA_STAND_MOUNT);
        }

        @SubscribeEvent
        public static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
            event.register(ExposureClient.SelectProperties.CAMERA_STATUS, StackCameraStatus.TYPE);
            event.register(ExposureClient.SelectProperties.CAMERA_ATTACHMENTS, StackCameraAttachments.TYPE);
        }

        @SubscribeEvent
        public static void registerRangeSelectProperties(RegisterRangeSelectItemModelPropertyEvent event) {
            event.register(ExposureClient.RangeSelectProperties.ALBUM_PHOTOS, AlbumPhotos.MAP_CODEC);
            event.register(ExposureClient.RangeSelectProperties.CHANNELS, Channels.MAP_CODEC);
            event.register(ExposureClient.RangeSelectProperties.COUNT, Count.MAP_CODEC);
        }

        @SubscribeEvent
        public static void registerConditionalSelectProperties(RegisterConditionalItemModelPropertyEvent event) {
            event.register(ExposureClient.ConditionalProperties.PROJECTOR_ACTIVE, ProjectorActive.MAP_CODEC);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            KeyboardHandler.registerKeymappings(key -> {
                event.register(key);
                return key;
            });
        }
    }

    @EventBusSubscriber(modid = Exposure.ID, value = Dist.CLIENT)
    public static class GameBus {
        @SubscribeEvent
        public static void onRenderGuiPost(RenderGuiEvent.Post event) {
            CameraStandTooltip.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }
}
