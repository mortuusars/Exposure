package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.screen.CameraScreen;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderRenderer;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(Exposure.MenuTypes.CAMERA.get(), CameraScreen::new));
        }
    }

    public static class ForgeBus {
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
            Player player = event.getEntity();
            if (Exposure.getCamera().isActive(player))
                player.startUsingItem(player.getMainHandItem()
                        .getItem() instanceof CameraItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        }

        @SubscribeEvent
        public static void onGuiOpen(ScreenEvent.Opening event) {
            if (isLookingThroughViewfinder() && !(event.getNewScreen() instanceof ViewfinderControlsScreen)) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    Exposure.getCamera().deactivate(player);
                    Packets.sendToServer(new UpdateActiveCameraPacket(player.getUUID(), false, player.getMainHandItem()
                            .getItem() instanceof CameraItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                }
            }
        }

        private static boolean isLookingThroughViewfinder() {
            return Exposure.getCamera().isActive(Minecraft.getInstance().player);
        }
    }
}
