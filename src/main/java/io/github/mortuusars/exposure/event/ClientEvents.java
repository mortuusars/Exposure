package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraHelper;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.client.gui.screen.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.client.renderer.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.client.renderer.PhotographEntityRenderer;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ExposureClient.init();
                MenuScreens.register(Exposure.MenuTypes.CAMERA.get(), CameraAttachmentsScreen::new);
                MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);

                ItemProperties.register(Exposure.Items.CAMERA.get(), new ResourceLocation("camera_state"),
                        (pStack, pLevel, pEntity, pSeed) -> {
                            if (pEntity instanceof Player player) {
                                CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);
                                if (!camera.isEmpty() && CameraInHand.isActive(player)
                                        && player.getItemInHand(camera.getHand()).equals(pStack)) {
                                    return 0.1f;
                                }
                            }
                            return 0f;
                        });

                ItemProperties.register(Exposure.Items.STACKED_PHOTOGRAPHS.get(), new ResourceLocation("count"),
                        (pStack, pLevel, pEntity, pSeed) -> {
                            if (pStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
                                return stackedPhotographsItem.getPhotographsCount(pStack);
                            }
                            return 0f;
                        });
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
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void onLevelClear(LevelEvent.Unload event) {
            ExposureClient.getExposureRenderer().clearData();
        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ExposureStorage.CLIENT.clear();
        }

        @SubscribeEvent
        public static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
            if (isLookingThroughViewfinder())
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void renderHand(RenderHandEvent event) {
            if (isLookingThroughViewfinder())
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onGuiOpen(ScreenEvent.Opening event) {
            if (isLookingThroughViewfinder() && !(event.getNewScreen() instanceof ViewfinderControlsScreen)) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null)
                    CameraHelper.deactivate(player, true);
            }
        }

        @SubscribeEvent
        public static void renderItemFrameItem(RenderItemInFrameEvent event) {
            ItemFramePhotographRenderer.render(event);
        }

        private static boolean isLookingThroughViewfinder() {
            return ViewfinderClient.isOpen();
        }
    }
}
