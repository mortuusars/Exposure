package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.ExposureCapture;
import io.github.mortuusars.exposure.camera.CaptureProperties;
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

    public static void exposeScreenshot(int size, boolean storeOnServer) {
        Preconditions.checkState(size > 0,  size + " size is invalid. Should be larger than 0.");
        if (size == Integer.MAX_VALUE)
            size = Math.min(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());

        ExposureCapture.enqueueCapture(new CaptureProperties("test", size, 1f, 0f, Collections.emptyList(),
                List.of(new ExposureFileSaver(ClientConfig.EXPOSURE_SAVE_PATH.get(), ClientConfig.EXPOSURE_SAVE_LEVEL_SUBFOLDER.get()))));
    }
}
