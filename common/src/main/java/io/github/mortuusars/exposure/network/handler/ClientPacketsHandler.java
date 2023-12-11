package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.capture.component.BaseComponent;
import io.github.mortuusars.exposure.camera.capture.component.ExposureStorageSaveComponent;
import io.github.mortuusars.exposure.camera.capture.component.FileSaveComponent;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringColorConverter;
import io.github.mortuusars.exposure.camera.capture.converter.SimpleColorConverter;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.packet.client.ApplyShaderS2CP;
import io.github.mortuusars.exposure.network.packet.client.ShowExposureS2CP;
import io.github.mortuusars.exposure.network.packet.client.StartExposureS2CP;
import io.github.mortuusars.exposure.util.ColorUtils;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ClientPacketsHandler {
    public static void applyShader(ApplyShaderS2CP packet) {
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

        Capture capture = new Capture(Util.getFilenameFormattedDateTime())
                .size(size)
                .cropFactor(1f)
                .components(
                        new BaseComponent(true),
                        FileSaveComponent.withDefaultFolders(Util.getFilenameFormattedDateTime()))
                .converter(new DitheringColorConverter());
        CaptureManager.enqueue(capture);
    }

    public static void loadExposure(String exposureId, String path, int size, boolean dither) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (StringUtil.isNullOrEmpty(exposureId)) {
            if (player == null)
                throw new IllegalStateException("Cannot load exposure: path is null or empty and player is null.");
            exposureId = player.getName().getString() + player.level().getGameTime();
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

                Capture capture = new Capture(finalExposureId)
                        .size(size)
                        .cropFactor(1f)
                        .components(new ExposureStorageSaveComponent(finalExposureId, true))
                        .converter(dither ? new DitheringColorConverter() : new SimpleColorConverter());
                capture.processImage(image);

                LogUtils.getLogger().info("Loaded exposure from file '" + path + "' with Id: '" + finalExposureId + "'.");
                Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                        Component.translatable("command.exposure.load_from_file.success", finalExposureId)
                                .withStyle(ChatFormatting.GREEN), false);
            } catch (IOException e) {
                LogUtils.getLogger().error("Cannot load exposure:" + e);
                Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                        Component.translatable("command.exposure.load_from_file.failure")
                                .withStyle(ChatFormatting.RED), false);
            }
        }).start();
    }

    public static void showExposure(ShowExposureS2CP packet) {
        if (packet.negative()) {
            Minecraft.getInstance().setScreen(new NegativeExposureScreen(
                    packet.isTexture() ? Either.right(new ResourceLocation(packet.path())) : Either.left(packet.path())));
        }
        else {
            ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            CompoundTag tag = new CompoundTag();
            tag.putString(packet.isTexture() ? FrameData.TEXTURE : FrameData.ID, packet.path());
            stack.setTag(tag);

            ClientGUI.openPhotographScreen(List.of(new ItemAndStack<>(stack)));
        }
    }

    public static void startExposure(StartExposureS2CP packet) {
        @Nullable LocalPlayer player = Minecraft.getInstance().player;
        Preconditions.checkState(player != null, "Player cannot be null.");

        ItemStack itemInHand = player.getItemInHand(packet.activeHand());
        if (!(itemInHand.getItem() instanceof CameraItem cameraItem) || !cameraItem.isActive(itemInHand))
            throw new IllegalStateException("Player should have active Camera in hand. " + itemInHand);

        cameraItem.exposeFrameClientside(player, packet.activeHand(), packet.exposureId(), packet.flashHasFired(), packet.lightLevel());
    }
}
