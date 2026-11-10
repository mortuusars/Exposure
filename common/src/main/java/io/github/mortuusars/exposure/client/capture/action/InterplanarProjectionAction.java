package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ServerboundInterplanarProjectionFinishedPacket;
import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.Optional;

public class InterplanarProjectionAction implements CaptureAction {
    private final CameraId cameraId;

    public InterplanarProjectionAction(CameraId cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public void onSuccess() {
        Packets.sendToServer(new ServerboundInterplanarProjectionFinishedPacket(cameraId, true, Optional.empty()));
    }

    @Override
    public void onFailure(TranslatableError error) {
        Packets.sendToServer(new ServerboundInterplanarProjectionFinishedPacket(cameraId, false, Optional.of(error)));
    }
}
