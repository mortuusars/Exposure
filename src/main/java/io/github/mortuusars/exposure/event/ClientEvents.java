package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.ZoomDirection;
import io.github.mortuusars.exposure.storage.ExposureStorage;
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
        if (Viewfinder.isActive())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        if (Viewfinder.isActive())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Pre event) {
//        LocalPlayer player = Minecraft.getInstance().player;
//        ((LocalPlayer) event.getEntity()).lerpHeadTo(20, 20);
//        event.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, player.position());

//        player.lerpHeadTo(player.yHeadRotO, 10);
//        player.startUsingItem(InteractionHand.MAIN_HAND);

//        Minecraft.getInstance().player
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDelta() == 0D || !Viewfinder.isActive())
            return;

        event.setCanceled(true);

        Viewfinder.modifyZoom(event.getScrollDelta() < 0d ? ZoomDirection.IN : ZoomDirection.OUT);
    }

    @SubscribeEvent
    public static void modifyFov(ViewportEvent.ComputeFov event) {
        //TODO: Restore fov smoothly, but only after exiting viewfinder, to not mess with other mods.

        if (!Viewfinder.isActive()) {
            if (event.usedConfiguredFov())
                Viewfinder.currentFov = (float) event.getFOV();
            return;
        }

        Viewfinder.currentFov += (float) ((Viewfinder.targetFov - Viewfinder.currentFov) * 0.025f * event.getPartialTick());

        event.setFOV(Viewfinder.currentFov);
    }
}
