package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import io.github.mortuusars.exposure.client.MouseHandler;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.client.gui.screen.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.client.render.PhotographEntityRenderer;
import io.github.mortuusars.exposure.command.ClientCommands;
import io.github.mortuusars.exposure.item.CameraItemClientExtensions;
import io.github.mortuusars.exposure.item.forge.CameraItemForgeClientExtensions;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                Exposure.initClient();
                MenuScreens.register(Exposure.MenuTypes.CAMERA.get(), CameraAttachmentsScreen::new);
                MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
            });
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Exposure.EntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(PhotographTooltip.class, photographTooltip -> photographTooltip);
        }

        @SubscribeEvent
        public static void registerResourceReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new ExposureClientReloadListener());
        }
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            ClientCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onLevelClear(LevelEvent.Unload event) {
            ExposureClient.getExposureRenderer().clearData();
        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ExposureClient.getExposureStorage().clear();
        }

//        @SubscribeEvent
//        public static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
//            if (ViewfinderClient.isLookingThrough())
//                event.setCanceled(true);
//        }

//        @SubscribeEvent
//        public static void renderHand(RenderHandEvent event) {
//            if (ViewfinderClient.isLookingThrough())
//                event.setCanceled(true);
//        }

//        @SubscribeEvent
//        public static void onGuiOpen(ScreenEvent.Opening event) {
//            if (ViewfinderClient.isOpen() && !(event.getNewScreen() instanceof ViewfinderControlsScreen)) {
//                LocalPlayer player = Minecraft.getInstance().player;
//                if (player != null)
//                    CameraInHand.deactivate(player);
//            }
//        }

        @SubscribeEvent
        public static void mouseScroll(InputEvent.MouseScrollingEvent event) {
            if (ViewfinderClient.handleMouseScroll(event.getScrollDelta() > 0d ? ZoomDirection.IN : ZoomDirection.OUT))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void computeFOV(ViewportEvent.ComputeFov event) {
            if (!event.usedConfiguredFov())
                return;

            double prevFov = event.getFOV();
            double modifiedFov = ViewfinderClient.modifyFov(prevFov);
            if (prevFov != modifiedFov)
                event.setFOV(modifiedFov);
        }

        @SubscribeEvent
        public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
            if (MouseHandler.handleMouseButtonPress(event.getButton(), event.getAction(), event.getModifiers()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void renderItemFrameItem(RenderItemInFrameEvent event) {
            ItemFramePhotographRenderer.render(event.getItemFrameEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }

        @SubscribeEvent
        public static void onRenderTick(TickEvent.RenderTickEvent event) {
            if (!event.phase.equals(TickEvent.Phase.END))
                return;

            CaptureManager.onRenderTickEnd();
        }
    }
}
