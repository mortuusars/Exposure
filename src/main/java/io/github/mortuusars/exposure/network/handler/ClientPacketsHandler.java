package io.github.mortuusars.exposure.network.handler;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraCapture;
import io.github.mortuusars.exposure.camera.Capture;
import io.github.mortuusars.exposure.config.ClientConfig;
import io.github.mortuusars.exposure.network.packet.ClientboundApplyShaderPacket;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import io.github.mortuusars.exposure.storage.saver.ExposureFileSaver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collections;
import java.util.List;
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

    public static void exposeScreenshot(int width, int height, boolean storeOnServer) {
        if (width == Integer.MAX_VALUE && height == Integer.MAX_VALUE) {
            width = Minecraft.getInstance().getWindow().getWidth();
            height = Minecraft.getInstance().getWindow().getHeight();

            width = Math.min(width, height);
            height = width;
        }

        if (width != Integer.MAX_VALUE && height == Integer.MAX_VALUE) {
            height = width;
        }

        CameraCapture.enqueueCapture(new Capture("test", width, height, 1f, 0f, Collections.emptyList(),
                List.of(new ExposureFileSaver(ClientConfig.EXPOSURE_SAVE_PATH.get(), ClientConfig.EXPOSURE_SAVE_LEVEL_SUBFOLDER.get()))));
    }
}
