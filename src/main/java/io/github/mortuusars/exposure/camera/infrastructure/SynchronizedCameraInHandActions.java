package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.network.packet.CameraSetCompositionGuideServerboundPacket;
import io.github.mortuusars.exposure.network.packet.CameraSetShutterSpeedServerboundPacket;
import io.github.mortuusars.exposure.network.packet.CameraSetZoomServerboundPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SynchronizedCameraInHandActions {
    public static void setZoom(float focalLength) {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setZoom(camera.getStack(), focalLength);
            CameraSetZoomServerboundPacket.send(focalLength);
        }
    }

    public static void setCompositionGuide(CompositionGuide guide) {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setCompositionGuide(camera.getStack(), guide);
            CameraSetCompositionGuideServerboundPacket.send(guide);
        }
    }

    public static void setShutterSpeed(ShutterSpeed shutterSpeed) {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeed);
            CameraSetShutterSpeedServerboundPacket.send(shutterSpeed);
        }
    }
}
