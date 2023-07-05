package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.network.packet.ServerboundCameraSetCompositionGuidePacket;
import io.github.mortuusars.exposure.network.packet.ServerboundCameraSetShutterSpeedPacket;
import io.github.mortuusars.exposure.network.packet.ServerboundCameraSetZoomPacket;
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
            ServerboundCameraSetZoomPacket.send(focalLength);
        }
    }

    public static void setCompositionGuide(CompositionGuide guide) {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setCompositionGuide(camera.getStack(), guide);
            ServerboundCameraSetCompositionGuidePacket.send(guide);
        }
    }

    public static void setShutterSpeed(ShutterSpeed shutterSpeed) {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeed);
            ServerboundCameraSetShutterSpeedPacket.send(shutterSpeed);
        }
    }
}
