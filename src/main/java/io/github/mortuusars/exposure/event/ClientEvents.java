package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.ZoomDirection;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
public class ClientEvents {
//    public static float fov = 5f;
//    public static float Viewfinder.targetFov = 5f;

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
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDelta() == 0D || !Viewfinder.isActive())
            return;

        event.setCanceled(true);

        Viewfinder.modifyZoom(event.getScrollDelta() < 0d ? ZoomDirection.IN : ZoomDirection.OUT);

//        float step = 10f * ( 1f - Mth.clamp((90 - Viewfinder.currentFov) / 90, 0.3f, 1f));
//        float inertia = Math.abs((Viewfinder.targetFov - Viewfinder.currentFov)) * 0.8f;
//
//        float change = step + inertia;
//
//        Viewfinder.targetFov = Mth.clamp(Viewfinder.targetFov += event.getScrollDelta() < 0d ? +change : -change, 10f, 90f);

//        Viewfinder.targetFov = event.getScrollDelta() < 0d ? Math.min(Viewfinder.targetFov + change, 90f) : Math.max(Viewfinder.targetFov - change * mod, 10f);

//        Exposure.LOGGER.info("CHANGE: " + change);
//        Exposure.LOGGER.info("Desired: " + Viewfinder.targetFov);
//        Exposure.LOGGER.info("FOV: " + Viewfinder.currentFov);


        double sensorWidth = 36.0; // Sensor width in millimeters

        // Formula to calculate the lens focal length based on FOV and sensor width
        double lensFocalLength = sensorWidth / (2.0 * Math.tan(Math.toRadians(Viewfinder.targetFov / 2.0)));

        float zoomPercent = Mth.map(Viewfinder.targetFov, 10f, 90f, 0f, 1f);


//        double sensitivity = 0.2d * (zoomPercent * zoomPercent);
//        Minecraft.getInstance().options.sensitivity().set(sensitivity);


//        Exposure.LOGGER.info(lensFocalLength + "mm");
//        Exposure.LOGGER.info("Sens:" + sensitivity * 2 * 100);
        Exposure.LOGGER.info(event.getScrollDelta() + "");
    }

    @SubscribeEvent
    public static void modifyFov(ViewportEvent.ComputeFov event) {
        if (!Viewfinder.isActive()) {
            Viewfinder.currentFov = (float) event.getFOV();
            return;
        }

        Viewfinder.currentFov += (float) ((Viewfinder.targetFov - Viewfinder.currentFov) * 0.05f * event.getPartialTick());

        event.setFOV(Viewfinder.currentFov);
    }
}
