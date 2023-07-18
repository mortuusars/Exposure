package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.screen.base.ExposureRenderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhotographScreen extends ExposureRenderScreen {
    private final Photograph photograph;
    @Nullable
    private final ResourceLocation texture;
    private float zoom = 0f;

    private double xPos;
    private double yPos;

    public PhotographScreen(Photograph photograph) {
        super(Component.empty());
        this.photograph = photograph;
        texture = photograph.getIdOrResource().right().orElse(null);

        loadExposure();
    }

    @Override
    protected void init() {
        super.init();

        xPos = width / 2f;
        yPos = height / 2f;
    }

    @Override
    protected String getExposureId() {
        return photograph.getIdOrResource().left().orElse(null);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        float imgWidth = exposureData != null ? exposureData.getWidth() : 256;
        float imgHeight = exposureData != null ? exposureData.getHeight() : 256;

        float scale = (height - (height / 6f)) / imgHeight;
        scale += zoom;

        poseStack.pushPose();

        // Move to center
        poseStack.translate(xPos, yPos, 0);
        // Scale
        poseStack.scale(scale, scale, scale);
        // Set origin point to center (for scale)
        poseStack.translate(imgWidth / -2d, imgHeight / -2d, 0);

        // Paper (frame)

        fill(poseStack, -8, -8, (int) (imgWidth + 8), (int) (imgHeight + 8), 0xFFDDDDDD);

        if (texture != null) {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            blit(poseStack, 0, 0, 0, 0, 256, 256);
        }
        else {
            if (exposureData == null)
                loadExposure();
            else
                renderExposure(poseStack, false);
        }

        poseStack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float zoomChange = delta > 0.0 ? 0.05f : -0.05f;
        float modifier = Mth.map(zoom, -0.5f, 2f, 1f, 8f);
        zoom = Mth.clamp(zoom + (zoomChange * modifier), -0.5f, 2f);

        if (zoom > -0.5f & zoom < 2f) {
            if (zoom < 1.5f) {
                double moveDelta = (zoomChange * modifier) * -1;
                xPos = Mth.lerp(moveDelta, xPos, mouseX);
                yPos = Mth.lerp(moveDelta, yPos, mouseY);
            }
        }

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        boolean handled = super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        if (!handled && pButton == 1) {
            this.xPos += pDragX;
            this.yPos += pDragY;

            this.xPos = Mth.clamp(xPos, -128f * zoom, width + 128f * zoom);
            this.yPos = Mth.clamp(yPos, -128f * zoom, height + 128f * zoom);
            handled = true;
        }

        return handled;
    }
}
