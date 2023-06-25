package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.client.ViewfinderRenderer;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
public class ClientEvents {
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
        if (isLookingThroughViewfinder()) {
            //TODO: other players
            Minecraft.getInstance().player.startUsingItem(InteractionHand.MAIN_HAND);
        }
    }

    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Opening event) {
        if (isLookingThroughViewfinder()) {
            //TODO: if not ViewfinderConfig screen
            Camera.getViewfinder().deactivate(Minecraft.getInstance().player);
        }
    }

    private static boolean isLookingThroughViewfinder() {
        return Camera.getViewfinder().isActive(Minecraft.getInstance().player);
    }
}
