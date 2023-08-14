package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.client.gui.screen.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.client.render.PhotographEntityRenderer;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.client.render.ViewfinderRenderer;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
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
                                CameraInHand cameraInHand = Exposure.getCamera().getCameraInHand(player);
                                if (!cameraInHand.isEmpty() && Exposure.getCamera().isActive(player)
                                        && player.getItemInHand(cameraInHand.getHand()).equals(pStack)) {
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
        public static void textureStitch(TextureStitchEvent.Pre event) {
            if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
                event.addSprite(Exposure.resource("gui/misc/photograph_paper"));
            }
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
            Exposure.getCamera().clear();
        }

        public static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {

        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ExposureStorage.CLIENT.clear();
        }

        @SubscribeEvent
        public static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
            if (isLookingThroughViewfinder()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderHand(RenderHandEvent event) {
            if (isLookingThroughViewfinder() || !ViewfinderRenderer.fovRestored) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderPlayer(RenderPlayerEvent.Pre event) {
//            Player player = event.getEntity();
//            if (Exposure.getCamera().isActive(player)) {
//                player.xRotO += 50;
//                player.setXRot(player.getXRot() + 50);
//                player.yBodyRot = player.yHeadRot;
//                player.yBodyRotO = player.yHeadRotO;
//            }
        }

        @SubscribeEvent
        public static void renderPlayer(RenderPlayerEvent.Post event) {
//            Player player = event.getEntity();
//            if (Exposure.getCamera().isActive(player)) {
//                player.xRotO -= 50;
//                player.setXRot(player.getXRot() - 50);
//            }
        }

        @SubscribeEvent
        public static void onGuiOpen(ScreenEvent.Opening event) {
            if (isLookingThroughViewfinder() && !(event.getNewScreen() instanceof ViewfinderControlsScreen)) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    Exposure.getCamera().deactivate(player);
                    CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
                    if (!camera.isEmpty())
                        Packets.sendToServer(new UpdateActiveCameraPacket(player.getUUID(), false, camera.getHand()));
                }
            }
        }

        @SubscribeEvent
        public static void renderItemFrameItem(RenderItemInFrameEvent event) {
            ItemFramePhotographRenderer.render(event);
        }

        private static boolean isLookingThroughViewfinder() {
            return Exposure.getCamera().isActive(Minecraft.getInstance().player) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
        }
    }
}
