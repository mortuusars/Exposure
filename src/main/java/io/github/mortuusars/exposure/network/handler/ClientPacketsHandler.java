package io.github.mortuusars.exposure.network.handler;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.ClientboundApplyShaderPacket;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class ClientPacketsHandler {
    public static void updateActiveCamera(NetworkEvent.Context context, UpdateActiveCameraPacket packet) {
        Player player = Objects.requireNonNull(Minecraft.getInstance().level).getPlayerByUUID(packet.playerID());

        if (packet.isActive())
            Exposure.getCamera().activate(player, packet.hand());
        else
            Exposure.getCamera().deactivate(player);
    }

    public static void applyShader(ClientboundApplyShaderPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (packet.shaderLocation().getPath().equals("none")) {
            mc.gameRenderer.shutdownEffect();
        }
        else {
            mc.gameRenderer.loadEffect(packet.shaderLocation());
        }
    }
}
