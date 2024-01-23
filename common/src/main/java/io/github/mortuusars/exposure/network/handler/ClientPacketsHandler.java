package io.github.mortuusars.exposure.network.handler;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.capture.LastExposures;
import io.github.mortuusars.exposure.camera.capture.component.BaseComponent;
import io.github.mortuusars.exposure.camera.capture.component.ExposureStorageSaveComponent;
import io.github.mortuusars.exposure.camera.capture.component.FileSaveComponent;
import io.github.mortuusars.exposure.camera.capture.component.ICaptureComponent;
import io.github.mortuusars.exposure.camera.capture.converter.DitheringColorConverter;
import io.github.mortuusars.exposure.camera.capture.converter.SimpleColorConverter;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.gui.screen.NegativeExposureScreen;
import io.github.mortuusars.exposure.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.packet.client.ApplyShaderS2CP;
import io.github.mortuusars.exposure.network.packet.client.ShowExposureS2CP;
import io.github.mortuusars.exposure.network.packet.client.StartExposureS2CP;
import io.github.mortuusars.exposure.util.ColorUtils;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ClientPacketsHandler {
    public static void applyShader(ApplyShaderS2CP packet) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (packet.shaderLocation().getPath().equals("none")) {
                mc.gameRenderer.shutdownEffect();
            } else {
                mc.gameRenderer.loadEffect(packet.shaderLocation());
            }
        });
    }

    public static void exposeScreenshot(int size) {
        Preconditions.checkState(size > 0, size + " size is invalid. Should be larger than 0.");
        if (size == Integer.MAX_VALUE)
            size = Math.min(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow()
                    .getHeight());

        int finalSize = size;
        Minecraft.getInstance().execute(() -> {
            String fileName = Util.getFilenameFormattedDateTime();
            Capture capture = new Capture(fileName)
                    .size(finalSize)
                    .cropFactor(1f)
                    .components(
                            new BaseComponent(true),
                            FileSaveComponent.withDefaultFolders(fileName),
                            new ICaptureComponent() {
                                @Override
                                public void end(Capture capture) {
                                    LogUtils.getLogger().info("Saved exposure screenshot: " + fileName);
                                }
                            })
                    .converter(new DitheringColorConverter());
            CaptureManager.enqueue(capture);
        });
    }

    public static void loadExposure(String exposureId, String path, int size, boolean dither) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (StringUtil.isNullOrEmpty(exposureId)) {
            if (player == null)
                throw new IllegalStateException("Cannot load exposure: path is null or empty and player is null.");
            exposureId = player.getName().getString() + player.getLevel().getGameTime();
        }

        String finalExposureId = exposureId;
        new Thread(() -> {
            Minecraft.getInstance().execute(() -> {
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

                    LogUtils.getLogger()
                            .info("Loaded exposure from file '" + path + "' with Id: '" + finalExposureId + "'.");
                    Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                            Component.translatable("command.exposure.load_from_file.success", finalExposureId)
                                    .withStyle(ChatFormatting.GREEN), false);
                } catch (IOException e) {
                    LogUtils.getLogger().error("Cannot load exposure:" + e);
                    Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(
                            Component.translatable("command.exposure.load_from_file.failure")
                                    .withStyle(ChatFormatting.RED), false);
                }
            });
        }).start();
    }

    public static void startExposure(StartExposureS2CP packet) {
        Minecraft.getInstance().execute(() -> {
            @Nullable LocalPlayer player = Minecraft.getInstance().player;
            Preconditions.checkState(player != null, "Player cannot be null.");

            ItemStack itemInHand = player.getItemInHand(packet.activeHand());
            if (!(itemInHand.getItem() instanceof CameraItem cameraItem) || !cameraItem.isActive(itemInHand))
                throw new IllegalStateException("Player should have active Camera in hand. " + itemInHand);

            cameraItem.exposeFrameClientside(player, packet.activeHand(), packet.exposureId(), packet.flashHasFired(), packet.lightLevel());
        });
    }

    public static void showExposure(ShowExposureS2CP packet) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                LogUtils.getLogger().error("Cannot show exposures. Player is null.");
                return;
            }

            boolean negative = packet.negative();

            @Nullable Screen screen;

            if (packet.latest()) {
                screen = createLatestScreen(player, negative);
            } else {
                if (negative) {
                    Either<String, ResourceLocation> idOrTexture = packet.isTexture() ?
                            Either.right(new ResourceLocation(packet.idOrPath())) : Either.left(packet.idOrPath());
                    screen = new NegativeExposureScreen(List.of(idOrTexture));
                } else {
                    ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
                    CompoundTag tag = new CompoundTag();
                    tag.putString(packet.isTexture() ? FrameData.TEXTURE : FrameData.ID, packet.idOrPath());
                    stack.setTag(tag);

                    screen = new PhotographScreen(List.of(new ItemAndStack<>(stack)));
                }
            }

            if (screen != null)
                Minecraft.getInstance().setScreen(screen);
        });
    }

    private static @Nullable Screen createLatestScreen(Player player, boolean negative) {
        Collection<String> exposureIds = LastExposures.get();
        if (exposureIds.size() == 0) {
            player.displayClientMessage(Component.translatable("command.exposure.show.latest.error.no_exposures"), false);
            return null;
        }

        if (negative) {
            List<Either<String, ResourceLocation>> exposures = new ArrayList<>();

            for (String exposureId : exposureIds) {
                exposures.add(Either.left(exposureId));
            }

            return new NegativeExposureScreen(exposures);
        } else {
            List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();

            for (String exposureId : exposureIds) {
                ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
                CompoundTag tag = new CompoundTag();
                tag.putString("Id", exposureId);
                stack.setTag(tag);

                photographs.add(new ItemAndStack<>(stack));
            }

            return new PhotographScreen(photographs);
        }
    }
}
