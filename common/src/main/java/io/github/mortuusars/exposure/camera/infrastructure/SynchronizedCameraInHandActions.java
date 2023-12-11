package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.CameraSetCompositionGuideC2SP;
import io.github.mortuusars.exposure.network.packet.server.CameraSetFlashModeC2SP;
import io.github.mortuusars.exposure.network.packet.server.CameraSetShutterSpeedC2SP;
import io.github.mortuusars.exposure.network.packet.server.CameraSetZoomC2SP;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;

public class SynchronizedCameraInHandActions {
    //TODO: Refactor
    public static void setZoom(double focalLength) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setZoom(camera.getStack(), focalLength);
            Packets.sendToServer(new CameraSetZoomC2SP(focalLength));
        }
    }

    public static void setCompositionGuide(CompositionGuide guide) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setCompositionGuide(camera.getStack(), guide);
            Packets.sendToServer(new CameraSetCompositionGuideC2SP(guide));
        }
    }

    public static void setFlashMode(FlashMode flashMode) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setFlashMode(camera.getStack(), flashMode);
            Packets.sendToServer(new CameraSetFlashModeC2SP(flashMode));
        }
    }

    public static void setShutterSpeed(ShutterSpeed shutterSpeed) {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeed);
            Packets.sendToServer(new CameraSetShutterSpeedC2SP(shutterSpeed));
        }
    }
}
