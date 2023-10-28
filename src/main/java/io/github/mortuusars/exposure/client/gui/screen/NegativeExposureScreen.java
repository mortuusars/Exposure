package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.renderer.ExposureRenderer;
import io.github.mortuusars.exposure.client.renderer.PhotographRenderer;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class NegativeExposureScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/gui/widgets.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;
    public static final int BUTTON_SIZE = 16;
    private final String exposureId;

    private float zoom = 0.5f;
    private float x;
    private float y;

    public NegativeExposureScreen(String exposureId) {
        super(Component.empty());
        this.exposureId = exposureId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        Exposure.getStorage().getOrQuery(exposureId).ifPresent(exposureData -> {
            int width = exposureData.getWidth();
            int height = exposureData.getHeight();
            boolean colorFilm = exposureData.getType() == FilmType.COLOR;

            poseStack.pushPose();
            poseStack.translate(Math.round(x + this.width / 2f), Math.round(y + this.height / 2f), 0);
            poseStack.scale(zoom, zoom, zoom);
            poseStack.translate(-Math.round(width / 2f), -Math.round(height / 2f), 0);

            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderTexture(0, TEXTURE);

                poseStack.pushPose();
                float scale = Math.max((float) width / (FRAME_SIZE), (float) height / (FRAME_SIZE));
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-12, -12, 0);

                GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);
                if (colorFilm)
                    RenderSystem.setShaderColor(1.2F, 0.96F, 0.75F, 1.0F);
                GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

                poseStack.popPose();
            }

            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
                        width, height, LightTexture.FULL_BRIGHT, 180, 130, 110, 255);
            else
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
                        width, height, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

            poseStack.popPose();
        });
    }

    private void renderFrame(String exposureId, PoseStack poseStack, float x, float y, float alpha, boolean colorFilm) {
        if (exposureId.length() == 0)
            return;

        Exposure.getStorage().getOrQuery(exposureId).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 180, 130, 110,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));
            else
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
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

        if (minecraft.options.keyInventory.matches(keyCode, scanCode))
            onClose();
        else if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS)
            zoom(ZoomDirection.IN);
        else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS)
            zoom(ZoomDirection.OUT);
        else
            return false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            zoom(delta >= 0.0 ? ZoomDirection.IN : ZoomDirection.OUT);
            return true;
        }

        return true;
    }

    private void zoom(ZoomDirection direction) {
        zoom = direction == ZoomDirection.IN ? zoom * 1.25f : zoom / 1.25f;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled)
            return true;

        if (button == 1) { // Right Click
//            zoom.set(0f);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (!handled && button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float)Mth.clamp(x + dragX, -centerX, centerX);
            y = (float)Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }
}
