package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.screen.base.ExposureRenderScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public class PhotographScreen extends ExposureRenderScreen {
    private final Photograph photograph;

    public PhotographScreen(Photograph photograph) {
        super(Component.empty());
        this.photograph = photograph;

        loadExposure();
    }

    @Override
    protected String getExposureId() {
        return photograph.getId();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (exposureData == null)
            return;

        float scale = (height - (height / 6f)) / exposureData.getHeight();
        poseStack = new PoseStack();

        poseStack.pushPose();

        // Move to center
        poseStack.translate(width / 2f, height / 2f, 0);
        // Scale
        poseStack.scale(scale, scale, scale);
        // Set origin point to center (for scale)
        poseStack.translate(exposureData.getWidth() / -2d, exposureData.getHeight() / -2d, 0);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

//        poseStack.translate(0, 0, 1000);

        // Paper (frame)
        fill(poseStack, -8, -8, exposureData.getWidth() + 8, exposureData.getHeight() + 8, 0xFFDDDDDD);

        renderExposure(poseStack, /*bufferSource, LightTexture.FULL_BRIGHT,*/ false);
        poseStack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
