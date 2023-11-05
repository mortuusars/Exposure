package io.github.mortuusars.exposure.client.gui.screen.element;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.ShutterSpeed;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ShutterSpeedButton extends CycleButton {
    private final Screen screen;
    private final List<ShutterSpeed> shutterSpeeds;
    private final int secondaryFontColor;
    private final int mainFontColor;

    public ShutterSpeedButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(screen, x, y, width, height, u, v, height, texture);
        this.screen = screen;

        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        Preconditions.checkState(!camera.isEmpty(), "Player must hold an active camera at this point.");

        List<ShutterSpeed> speeds = new ArrayList<>(camera.getItem().getAllShutterSpeeds(camera.getStack()));
        Collections.reverse(speeds);
        shutterSpeeds = speeds;

        ShutterSpeed shutterSpeed = camera.getItem().getShutterSpeed(camera.getStack());
        if (!shutterSpeeds.contains(shutterSpeed)) {
            throw new IllegalStateException("Camera {" + camera.getStack() + "} has invalid shutter speed.");
        }

        int currentShutterSpeedIndex = 0;
        for (int i = 0; i < shutterSpeeds.size(); i++) {
            if (shutterSpeed.equals(shutterSpeeds.get(i)))
                currentShutterSpeedIndex = i;
        }

        setupButtonElements(shutterSpeeds.size(), currentShutterSpeedIndex);
        secondaryFontColor = Config.Client.getSecondaryFontColor();
        mainFontColor = Config.Client.getMainFontColor();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_DIAL_CLICK.get(),
                Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() * 0.05f + 0.9f + currentIndex * 0.01f, 0.7f));
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderButton(poseStack, mouseX, mouseY, partialTick);

        ShutterSpeed shutterSpeed = shutterSpeeds.get(currentIndex);
        String text = shutterSpeed.toString();

        if (shutterSpeed.equals(ShutterSpeed.DEFAULT))
            text = text + "•";

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = 35 - (textWidth / 2);

        font.draw(poseStack, text, x + xPos, y + 4, secondaryFontColor);
        font.draw(poseStack, text, x + xPos, y + 3, mainFontColor);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, Component.translatable("gui.exposure.viewfinder.shutter_speed.tooltip"), mouseX, mouseY);
    }

    @Override
    protected void onCycle() {
        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            if (camera.getItem().getShutterSpeed(camera.getStack()) != shutterSpeeds.get(currentIndex)) {
                SynchronizedCameraInHandActions.setShutterSpeed(shutterSpeeds.get(currentIndex));
            }
        }
    }
}
