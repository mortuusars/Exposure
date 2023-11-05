package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.network.packet.CameraSetCompositionGuideServerboundPacket;
import io.github.mortuusars.exposure.network.packet.CameraSetFlashModeServerboundPacket;
import io.github.mortuusars.exposure.network.packet.CameraSetShutterSpeedServerboundPacket;
import io.github.mortuusars.exposure.network.packet.CameraSetZoomServerboundPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SynchronizedCameraInHandActions {
    //TODO: Refactor
    public static void setZoom(double focalLength) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setZoom(camera.getStack(), focalLength);
            CameraSetZoomServerboundPacket.send(focalLength);
        }
    }

    public static void setCompositionGuide(CompositionGuide guide) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setCompositionGuide(camera.getStack(), guide);
            CameraSetCompositionGuideServerboundPacket.send(guide);
        }
    }

    public static void setFlashMode(FlashMode flashMode) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setFlashMode(camera.getStack(), flashMode);
            CameraSetFlashModeServerboundPacket.send(flashMode);
        }
    }

    public static void setShutterSpeed(ShutterSpeed shutterSpeed) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeed);
            CameraSetShutterSpeedServerboundPacket.send(shutterSpeed);
        }
    }
}
