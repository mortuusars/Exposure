package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.capture.component.BaseComponent;
import io.github.mortuusars.exposure.camera.capture.component.ExposureStorageSaveComponent;
import io.github.mortuusars.exposure.camera.capture.component.FileSaveComponent;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringConverter;
import io.github.mortuusars.exposure.camera.capture.converter.SimpleConverter;
import io.github.mortuusars.exposure.network.packet.ApplyShaderClientboundPacket;
import io.github.mortuusars.exposure.util.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class ClientPacketsHandler {
    public static void applyShader(ApplyShaderClientboundPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (packet.shaderLocation().getPath().equals("none")) {
            mc.gameRenderer.shutdownEffect();
        } else {
            mc.gameRenderer.loadEffect(packet.shaderLocation());
        }
    }

    public static void exposeScreenshot(int size) {
        Preconditions.checkState(size > 0, size + " size is invalid. Should be larger than 0.");
        if (size == Integer.MAX_VALUE)
            size = Math.min(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());

        Capture capture = new Capture()
                .size(size)
                .cropFactor(1f)
                .components(
                        new BaseComponent(),
                        FileSaveComponent.withDefaultFolders(Util.getFilenameFormattedDateTime()))
                .converter(new DitheringConverter());
        CaptureManager.enqueue(capture);
    }

    public static void loadExposure(String exposureId, String path, int size, boolean dither) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (StringUtil.isNullOrEmpty(exposureId)) {
            if (player == null)
                throw new IllegalStateException("Cannot load exposure: exposureId is null or empty and player is null.");
            exposureId = player.getName().getString() + player.level.getGameTime();
        }

        String finalExposureId = exposureId;
        new Thread(() -> {
            try {
                BufferedImage read = ImageIO.read(new File(path));

                NativeImage image = new NativeImage(read.getWidth(), read.getHeight(), false);

                for (int x = 0; x < read.getWidth(); x++) {
                    for (int y = 0; y < read.getHeight(); y++) {
                        image.setPixelRGBA(x, y, ColorUtils.BGRtoRGB(read.getRGB(x, y)));
                    }
                }

                Capture capture = new Capture()
                        .size(size)
                        .cropFactor(1f)
                        .components(new ExposureStorageSaveComponent(finalExposureId, true))
                        .converter(dither ? new DitheringConverter() : new SimpleConverter());
                capture.processImage(image);

                Exposure.LOGGER.info("Loaded exposure from file '" + path + "' with exposureId: '" + finalExposureId + "'.");
                Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                        Component.translatable("command.exposure.load_from_file.success", finalExposureId)
                                .withStyle(ChatFormatting.GREEN), false);
            } catch (IOException e) {
                Exposure.LOGGER.error("Cannot load exposure:" + e);
                Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                        Component.translatable("command.exposure.load_from_file.failure")
                                .withStyle(ChatFormatting.RED), false);
            }
        }).start();
    }
}
