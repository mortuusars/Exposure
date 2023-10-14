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
import io.github.mortuusars.exposure.util.OnePerPlayerSoundsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FilmFrameInspectScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final int FRAME_SIZE = 54;

    private final LightroomScreen lightroomScreen;
    private final LightroomMenu lightroomMenu;

    private float targetZoom = 2f;
    private float currentZoom = 1.05f;

    private float x = 0f;
    private float y = 0f;

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
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        int currentFrame = getLightroomMenu().getCurrentFrame();
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
        poseStack.translate(78 / -2f, 78 / -2f, 0);


        RenderSystem.setShaderTexture(0, TEXTURE);

        GuiUtil.blit(poseStack, 0, 0, 78, 78, 0, 0, 256, 256, 0);

        if (colorFilm)
            RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 1.0F);

        GuiUtil.blit(poseStack, 0, 0, 78, 78, 0, 78, 256, 256, 0);

        poseStack.translate(12, 12, 0);
        renderFrame(frame, poseStack, 0, 0, 1f, colorFilm);


        poseStack.popPose();
    }

    private void renderFrame(@NotNull FrameData frame, PoseStack poseStack, float x, float y, float alpha, boolean colorFilm) {
        Exposure.getStorage().getOrQuery(frame.id).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance()
                    .getBuilder());
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
        }

        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            ZoomDirection direction = delta >= 0.0 ? ZoomDirection.IN : ZoomDirection.OUT;
            float change = direction == ZoomDirection.IN ? 1f : -1f;
            targetZoom = Mth.clamp(targetZoom + change, 1f, 8f);
            handled = true;
        }

        return handled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (button == 0) {
//            Preconditions.checkState(minecraft != null);
//            Preconditions.checkState(minecraft.gameMode != null);
//
//            if (isOverCenterFrame((int) mouseX, (int) mouseY)) {
//                enterFrameInspectMode();
//            }
//
//            if (isOverLeftFrame((int) mouseX, (int) mouseY)) {
//                minecraft.gameMode.handleInventoryButtonClick(getLightroomMenu().containerId, LightroomMenu.PREVIOUS_FRAME_BUTTON_ID);
//                return true;
//            }
//
//            if (isOverRightFrame((int) mouseX, (int) mouseY)) {
//                minecraft.gameMode.handleInventoryButtonClick(getLightroomMenu().containerId, LightroomMenu.NEXT_FRAME_BUTTON_ID);
//                return true;
//            }
//        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(pMouseX, pMouseY, button, dragX, dragY);

        if (!handled && button == 0 || button == 1) {
//            this.x += dragX;
//            this.y += dragY;

            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float)Mth.clamp(x + dragX, -centerX, centerX);
            y = (float)Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }
}
