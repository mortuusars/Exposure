package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.ExposureCapture;
import io.github.mortuusars.exposure.camera.CaptureProperties;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.network.packet.ApplyShaderClientboundPacket;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import io.github.mortuusars.exposure.storage.saver.ExposureFileSaver;
import io.github.mortuusars.exposure.storage.saver.ExposureStorageSaver;
import io.github.mortuusars.exposure.storage.saver.IExposureSaver;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientPacketsHandler {
    public static void updateActiveCamera(NetworkEvent.Context context, UpdateActiveCameraPacket packet) {
        if (Minecraft.getInstance().level == null) return;
        @Nullable Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.playerID());
        if (player == null) return;

        if (packet.isActive())
            Exposure.getCamera().activate(player, packet.hand());
        else
            Exposure.getCamera().deactivate(player);
    }

    public static void applyShader(ApplyShaderClientboundPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (packet.shaderLocation().getPath().equals("none")) {
            mc.gameRenderer.shutdownEffect();
        }
        else {
            mc.gameRenderer.loadEffect(packet.shaderLocation());
        }
    }

    public static void exposeScreenshot(int size) {
        Preconditions.checkState(size > 0,  size + " size is invalid. Should be larger than 0.");
        if (size == Integer.MAX_VALUE)
            size = Math.min(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());

        ExposureCapture.enqueueCapture(new CaptureProperties(Util.getFilenameFormattedDateTime(), size, 1f,
                0f, false, Collections.emptyList(), List.of(ExposureFileSaver.withDefaultFolders())));
    }

    public static void loadExposure(String id, String path, int size, boolean dither) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (StringUtil.isNullOrEmpty(id)) {
            if (player == null)
                throw new IllegalStateException("Cannot load exposure: id is null or empty and player is null.");
            id = player.getName().getString() + player.level.getGameTime();
        }

        String finalId = id;
        new Thread(() -> {
            try {
                BufferedImage read = ImageIO.read(new File(path));

                NativeImage image = new NativeImage(read.getWidth(), read.getHeight(), false);

                for (int x = 0; x < read.getWidth(); x++) {
                    for (int y = 0; y < read.getHeight(); y++) {
                        image.setPixelRGBA(x, y, ColorUtils.BGRtoRGB(read.getRGB(x, y)));
                    }
                }

                List<IExposureSaver> savers = new ArrayList<>();
                savers.add(new ExposureStorageSaver());
                if (Config.Client.EXPOSURE_SAVE_ON_EVERY_CAPTURE.get())
                    savers.add(ExposureFileSaver.withDefaultFolders());

                ExposureCapture.processAndSaveImage(image, new CaptureProperties(finalId, size, 1,
                    0, false, Collections.emptyList(), savers), dither);
                Exposure.LOGGER.info("Loaded exposure from file '" + path + "' with id: '" + finalId + "'.");
            } catch (IOException e) {
                Exposure.LOGGER.error("Cannot load exposure:" + e);
            }
        }).start();
    }
}
