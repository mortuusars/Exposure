package io.github.mortuusars.exposure.network.handler;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import net.minecraftforge.network.NetworkEvent;

public class ServerPacketsHandler {
    public static void updateActiveCamera(NetworkEvent.Context context, UpdateActiveCameraPacket packet) {
        if (packet.isActive())
            Exposure.getCamera().activate(context.getSender(), packet.hand());
        else
            Exposure.getCamera().deactivate(context.getSender());
    }
}
