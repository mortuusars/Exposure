package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.film.FrameData;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FilmFrameInspectScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;
    public static final int BUTTON_SIZE = 16;
    public static final int BUTTON_SPACING = 2;

    private final LightroomScreen lightroomScreen;
    private final LightroomMenu lightroomMenu;

    private float targetZoom = 2f;
    private float currentZoom = 1.05f;
    private float x = 0f;
    private float y = 0f;

    private boolean isClickedOnButton = false;

    public FilmFrameInspectScreen(LightroomScreen lightroomScreen, LightroomMenu lightroomMenu) {
        super(Component.empty());
        this.lightroomScreen = lightroomScreen;
        this.lightroomMenu = lightroomMenu;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private LightroomMenu getLightroomMenu() {
        return lightroomMenu;
    }

    @Override
    protected void init() {
        super.init();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    public void close() {
        Minecraft.getInstance().setScreen(lightroomScreen);
        Objects.requireNonNull(Minecraft.getInstance().player).playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 0.7f);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        int currentFrame = getLightroomMenu().getSelectedFrame();
        @Nullable FrameData frame = getLightroomMenu().getFrameByIndex(currentFrame);

        if (frame == null || (targetZoom == 1f && currentZoom <= 1.2f)) {
            close();
            return;
        }

        if (currentZoom < targetZoom)
            currentZoom = Mth.lerp(Math.min(0.4f * partialTick, 1f), currentZoom, targetZoom);
        else
            currentZoom = Mth.lerp(Math.min(0.75f * partialTick, 1f), currentZoom, targetZoom);

        boolean colorFilm = getLightroomMenu().isColorFilm();

        poseStack.pushPose();

        poseStack.translate(x, y, 0);
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale(currentZoom, currentZoom, currentZoom);

        RenderSystem.setShaderTexture(0, TEXTURE);
        float offsetX = BG_SIZE / 2f + BUTTON_SPACING;

        // Previous Frame button
        int pVOffset = 0;
        if (getLightroomMenu().getSelectedFrame() == 0)
            pVOffset = 32;
        else if (isOverPreviousButton(mouseX, mouseY))
            pVOffset = BUTTON_SIZE;

        poseStack.pushPose();
        poseStack.translate(-(offsetX + BUTTON_SIZE), BUTTON_SIZE / -2f, 0);
        poseStack.translate(BUTTON_SIZE / 2f, BUTTON_SIZE / 2f, 0);
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.translate(BUTTON_SIZE / -2f, BUTTON_SIZE / -2f, 0);
        blit(poseStack, 0, 0, 0, 156 + pVOffset, BUTTON_SIZE, BUTTON_SIZE);
        poseStack.popPose();

        // Next Frame button
        int nVOffset = 0;
        if (getLightroomMenu().getSelectedFrame() + 1 == getLightroomMenu().getTotalFrames())
            nVOffset = 32;
        else if (isOverNextButton(mouseX, mouseY))
            nVOffset = BUTTON_SIZE;
        poseStack.pushPose();
        poseStack.translate(offsetX, BUTTON_SIZE / -2f, 0);
        poseStack.translate(BUTTON_SIZE / 2f, BUTTON_SIZE / 2f, 0);
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.translate(BUTTON_SIZE / -2f, BUTTON_SIZE / -2f, 0);
        blit(poseStack, 0, 0, BUTTON_SIZE, 156 + nVOffset, BUTTON_SIZE, BUTTON_SIZE);
        poseStack.popPose();


        poseStack.translate(BG_SIZE / -2f, BG_SIZE / -2f, 0);

        GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

        if (colorFilm)
            RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 1.0F);

        GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

        poseStack.translate(12, 12, 0);
        renderFrame(frame, poseStack, 0, 0, 1f, colorFilm);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (isOverPreviousButton(mouseX, mouseY) && getLightroomMenu().getSelectedFrame() != 0) {
            renderTooltip(poseStack, Component.translatable("gui.exposure.lightroom.previous_frame"), mouseX, mouseY);
        }

        if (isOverNextButton(mouseX, mouseY) && getLightroomMenu().getSelectedFrame() + 1 < getLightroomMenu().getTotalFrames()) {
            renderTooltip(poseStack, Component.translatable("gui.exposure.lightroom.next_frame"), mouseX, mouseY);
        }
    }

    private boolean isMouseHoveringOver(double mouseX, double mouseY, double x, double y, double width, double height) {
        return (mouseX >= x && mouseX < x + width) && (mouseY >= y && mouseY < y + height);
    }

    private boolean isOverPreviousButton(double mouseX, double mouseY) {
        float centerPosX = x + width / 2f;
        float centerPosY = y + height / 2f;
        float offsetX = -(BG_SIZE / 2f + BUTTON_SPACING + BUTTON_SIZE) * currentZoom;
        float offsetY = -(BUTTON_SIZE / 2f) * currentZoom;

        return (mouseX > centerPosX + offsetX && mouseX <= centerPosX + offsetX + (BUTTON_SIZE * currentZoom))
                && (mouseY > centerPosY + offsetY && mouseY <= centerPosY + offsetY + (BUTTON_SIZE * currentZoom));
    }

    private boolean isOverNextButton(double mouseX, double mouseY) {
        float centerPosX = x + width / 2f;
        float centerPosY = y + height / 2f;
        float offsetX = (BG_SIZE / 2f + BUTTON_SPACING) * currentZoom;
        float offsetY = -(BUTTON_SIZE / 2f) * currentZoom;

        return (mouseX > centerPosX + offsetX && mouseX <= centerPosX + offsetX + (BUTTON_SIZE * currentZoom))
                && (mouseY > centerPosY + offsetY && mouseY <= centerPosY + offsetY + (BUTTON_SIZE * currentZoom));
    }

    private void renderFrame(@NotNull FrameData frame, PoseStack poseStack, float x, float y, float alpha, boolean colorFilm) {
        Exposure.getStorage().getOrQuery(frame.id).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(frame.id, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 180, 130, 110,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));
            else
                ExposureClient.getExposureRenderer().renderNegative(frame.id, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 255, 255, 255,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));

            bufferSource.endBatch();

            poseStack.popPose();
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.gameMode != null);

        if (minecraft.options.keyInventory.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_ESCAPE) {
            close();
            handled = true;
        } else if (minecraft.options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            lightroomScreen.changeFrame(Navigation.PREVIOUS);
            handled = true;
        } else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
            lightroomScreen.changeFrame(Navigation.NEXT);
            handled = true;
        } else if (keyCode - 48 >= 0 && keyCode - 48 <= 10) {
            int number = keyCode - 48;
            if (number == 0) {
                close();
                return true;
            }
            else {
                targetZoom = number + 1;
            }
        }

        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            ZoomDirection direction = delta >= 0.0 ? ZoomDirection.IN : ZoomDirection.OUT;
            float change = direction == ZoomDirection.IN ? 1f : -1f;
            targetZoom = Mth.clamp(targetZoom + change, 1f, 10f);
            handled = true;
        }

        return handled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left Click
            if (isOverPreviousButton(mouseX, mouseY)) {
                lightroomScreen.changeFrame(Navigation.PREVIOUS);
                isClickedOnButton = true;
                return true;
            }

            if (isOverNextButton(mouseX, mouseY)) {
                lightroomScreen.changeFrame(Navigation.NEXT);
                isClickedOnButton = true;
                return true;
            }
        }

        if (button == 1) { // Right Click
            targetZoom = 1f;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        isClickedOnButton = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (!handled && !isClickedOnButton && button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float)Mth.clamp(x + dragX, -centerX, centerX);
            y = (float)Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }


}
