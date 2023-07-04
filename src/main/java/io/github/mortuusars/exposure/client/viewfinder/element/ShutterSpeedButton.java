package io.github.mortuusars.exposure.client.viewfinder.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraOld;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.client.ClientOnlyLogic;
import io.github.mortuusars.exposure.config.ClientConfig;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShutterSpeedButton extends ImageButton {
    private final Screen screen;
    private final ResourceLocation texture;
    private final List<ShutterSpeed> shutterSpeeds;

    private int currentShutterSpeedIndex = 0;
    private long lastChangeTime;

    public ShutterSpeedButton(Screen screen, int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, 0, 0, height, texture, 256, 256, button -> {}, Button.NO_TOOLTIP, Component.empty());
        this.screen = screen;
        this.texture = texture;

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);

        shutterSpeeds = camera.getItem().getAllShutterSpeeds(camera.getStack());

        ShutterSpeed shutterSpeed = camera.getItem().getShutterSpeed(camera.getStack());
        if (!shutterSpeeds.contains(shutterSpeed))
            shutterSpeed = camera.getItem().getDefaultShutterSpeed(camera.getStack());

        for (int i = 0; i < shutterSpeeds.size(); i++) {
            if (shutterSpeed.equals(shutterSpeeds.get(i)))
                currentShutterSpeedIndex = i;
        }
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int offset = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Button
        blit(poseStack, x, y, 138, height  * (offset - 1), width, height);

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        ShutterSpeed shutterSpeed = camera.getItem().getShutterSpeed(camera.getStack());
        String text = shutterSpeed.toString();
        if (shutterSpeed.equals(camera.getItem().getDefaultShutterSpeed(camera.getStack())))
            text = text + "•";

        Font font = minecraft.font;
        int textWidth = font.width(text);
        int xPos = 18 + (19 - textWidth) / 2;

        font.draw(poseStack, text, x + xPos, y + 5, ClientConfig.getSecondaryFontColor());
        font.draw(poseStack, text, x + xPos, y + 4, ClientConfig.getMainFontColor());
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, Component.translatable("gui.exposure.viewfinder.shutter_speed.tooltip"), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered) {
            cycleShutterSpeed(button == 1);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        cycleShutterSpeed(delta < 0d);
        return true;
    }

    public void cycleShutterSpeed(boolean reverse) {
        if (System.currentTimeMillis() - lastChangeTime < 40)
            return;

        currentShutterSpeedIndex = Mth.clamp(currentShutterSpeedIndex + (reverse ? 1 : -1), 0, shutterSpeeds.size() - 1);

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            if (camera.getItem().getShutterSpeed(camera.getStack()) != shutterSpeeds.get(currentShutterSpeedIndex)) {
                camera.getItem().setShutterSpeed(camera.getStack(), shutterSpeeds.get(currentShutterSpeedIndex));

                ClientOnlyLogic.updateAndSyncCameraStack(camera.getStack(), camera.getHand());

                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK);
                lastChangeTime = System.currentTimeMillis();
            }
        }
    }
}
