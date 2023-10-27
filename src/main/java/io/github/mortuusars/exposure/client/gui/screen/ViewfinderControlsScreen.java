package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOverlay;
import io.github.mortuusars.exposure.client.gui.screen.element.*;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ViewfinderControlsScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder_camera_controls.png");

    private final Player player;
    private final ClientLevel level;
    private final long openedAtTimestamp;

    public ViewfinderControlsScreen() {
        super(Component.empty());

        player = Minecraft.getInstance().player;
        level = Minecraft.getInstance().level;
        assert level != null;
        openedAtTimestamp = level.getGameTime();

        passEvents = true;
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        refreshMovementKeysToKeepPlayerMoving();

        int leftPos = (width - 256) / 2;
        int topPos = Math.round(ViewfinderOverlay.opening.y + ViewfinderOverlay.opening.height - 256);

        CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);
        if (camera.isEmpty())
            throw new IllegalStateException("Active Camera cannot be empty here.");

        boolean hasFlashAttached = camera.getItem().getAttachment(camera.getStack(), CameraItem.FLASH_ATTACHMENT).isPresent();

        int sideButtonsWidth = 48;
        int buttonWidth = 15;

        int elementX = leftPos + 128 - (sideButtonsWidth + 1 + buttonWidth + 1 + (hasFlashAttached ? buttonWidth + 1 : 0) + sideButtonsWidth) / 2;
        int elementY = topPos + 238;

        FocalLengthButton focalLengthButton = new FocalLengthButton(this, elementX, elementY, 48, 18, 0, 0, TEXTURE);
        addRenderableOnly(focalLengthButton);
        elementX += focalLengthButton.getWidth();

        ImageButton separator1 = new ImageButton(elementX, elementY, 1, 18, 111, 0, TEXTURE, pButton -> {});
        addRenderableOnly(separator1);
        elementX += separator1.getWidth();

        CompositionGuideButton compositionGuideButton = new CompositionGuideButton(this, elementX, elementY, 15, 18, 48, 0, TEXTURE);
        addRenderableWidget(compositionGuideButton);
        elementX += compositionGuideButton.getWidth();

        ImageButton separator2 = new ImageButton(elementX, elementY, 1, 18, 111, 0, TEXTURE, pButton -> {});
        addRenderableOnly(separator2);
        elementX += separator2.getWidth();

        if (hasFlashAttached) {
            FlashModeButton flashModeButton = new FlashModeButton(this, elementX, elementY, 15, 18, 48, 0, TEXTURE);
            addRenderableWidget(flashModeButton);
            elementX += flashModeButton.getWidth();

            ImageButton separator3 = new ImageButton(elementX, elementY, 1, 18, 111, 0, TEXTURE, pButton -> {});
            addRenderableOnly(separator3);
            elementX += separator3.getWidth();
        }

        FrameCounterButton frameCounterButton = new FrameCounterButton(this, elementX, elementY, 48, 18, 63, 0, TEXTURE);
        addRenderableOnly(frameCounterButton);

        ShutterSpeedButton shutterSpeedButton = new ShutterSpeedButton(this, leftPos + 94, topPos + 226, 69, 12, 112, 0, TEXTURE);
        addRenderableWidget(shutterSpeedButton);
    }

    /**
     * When screen is opened - all keys are released. If we do not refresh them - player would stop moving (if they had).
     */
    private void refreshMovementKeysToKeepPlayerMoving() {
        Options opt = Minecraft.getInstance().options;
        long windowId = Minecraft.getInstance().getWindow().getWindow();
        Consumer<KeyMapping> update = keyMapping -> keyMapping.setDown(InputConstants.isKeyDown(windowId, keyMapping.getKey().getValue()));

        update.accept(opt.keyUp);
        update.accept(opt.keyDown);
        update.accept(opt.keyLeft);
        update.accept(opt.keyRight);
        update.accept(opt.keyShift);
        update.accept(opt.keyJump);
        update.accept(opt.keySprint);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!ViewfinderClient.isLookingThrough()) {
            this.onClose();
            return;
        }

        if (Minecraft.getInstance().options.hideGui)
            return;

        poseStack.pushPose();

        float viewfinderScale = ViewfinderOverlay.getScale();
        if (viewfinderScale != 1.0f) {
            poseStack.translate(width / 2f, height / 2f, 0);
            poseStack.scale(viewfinderScale, viewfinderScale, viewfinderScale);
            poseStack.translate(-width / 2f, -height / 2f, 0);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

//        blit(poseStack, leftPos, topPos, 0, 0, 256, 256);
        super.render(poseStack, mouseX, mouseY, partialTick);

        for(Widget widget : this.renderables) {
            if (widget instanceof AbstractWidget abstractWidget && abstractWidget.isHoveredOrFocused()) {
                abstractWidget.renderToolTip(poseStack, mouseX, mouseY);
                break;
            }
        }

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (!handled && button == 1 && Minecraft.getInstance().gameMode != null) {
            CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);
            if (!camera.isEmpty()) {
                Minecraft.getInstance().gameMode.useItem(player, camera.getHand());
                handled = true;
            }
        }

        return handled;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyShift.matches(keyCode, scanCode)) {
            if (level.getGameTime() - openedAtTimestamp >= 5)
                this.onClose();

            return false;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS) {
            ViewfinderClient.zoom(ZoomDirection.IN, true);
            return true;
        }
        else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS) {
            ViewfinderClient.zoom(ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!super.mouseScrolled(mouseX, mouseY, delta)) {
            ViewfinderClient.zoom(delta > 0d ? ZoomDirection.IN : ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }
}
