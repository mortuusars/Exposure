package io.github.mortuusars.exposure.client.viewfinder.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CompositionGuideButton extends ImageButton {
    private final Screen screen;
    private final ResourceLocation texture;
    private final List<CompositionGuide> guides;
    private int currentGuideIndex = 0;

    private long lastChangeTime;

    public CompositionGuideButton(Screen screen, int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, 118, 0, height, texture, 256, 256, button -> {}, Button.NO_TOOLTIP, Component.empty());
        this.screen = screen;
        this.texture = texture;
        guides = CompositionGuides.getGuides();

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        CompositionGuide guide = camera.getItem().getCompositionGuide(camera.getStack());

        for (int i = 0; i < guides.size(); i++) {
            if (guides.get(i).getId().equals(guide.getId())) {
                currentGuideIndex = i;
                break;
            }
        }
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int offset = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Button
        blit(poseStack, x, y, 118, height  * (offset - 1), width, height);
        // Icon
        blit(poseStack, x + 3, y + 1, 15, 100 + currentGuideIndex * 13, 15, 13);

        this.renderBg(poseStack, minecraft, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, List.of(Component.translatable("gui.exposure.viewfinder.composition_guide.tooltip"),
                ((MutableComponent) getMessage()).withStyle(ChatFormatting.GRAY)), Optional.empty(), mouseX, mouseY);
    }

    @Override
    public @NotNull Component getMessage() {
        return guides.get(currentGuideIndex).translate();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered) {
            cycleGuide(button == 1);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (System.currentTimeMillis() - lastChangeTime > 50)
            cycleGuide(delta < 0d);
        return true;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean pressed = super.keyPressed(pKeyCode, pScanCode, pModifiers);

        if (pressed)
            cycleGuide(Screen.hasShiftDown());

        return pressed;
    }

    private void cycleGuide(boolean reverse) {
        currentGuideIndex += reverse ? -1 : 1;
        if (currentGuideIndex < 0)
            currentGuideIndex = guides.size() - 1;
        else if (currentGuideIndex >= guides.size())
            currentGuideIndex = 0;

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty()) {
            SynchronizedCameraInHandActions.setCompositionGuide(guides.get(currentGuideIndex));

            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK);
            lastChangeTime = System.currentTimeMillis();
        }
    }
}
