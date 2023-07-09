package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.screen.base.ExposureRenderScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class PhotographScreen extends ExposureRenderScreen {
    private final Photograph photograph;
    private float zoom = 0f;

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
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (exposureData == null) {
            loadExposure();

            if (exposureData == null)
                return;
        }

        float scale = (height - (height / 6f)) / exposureData.getHeight();
        scale += zoom;

        poseStack.pushPose();

        // Move to center
        poseStack.translate(width / 2f, height / 2f, 0);
        // Scale
        poseStack.scale(scale, scale, scale);
        // Set origin point to center (for scale)
        poseStack.translate(exposureData.getWidth() / -2d, exposureData.getHeight() / -2d, 0);

        // Paper (frame)
        fill(poseStack, -8, -8, exposureData.getWidth() + 8, exposureData.getHeight() + 8, 0xFFDDDDDD);
        renderExposure(poseStack,false);

        poseStack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float zoomChange = delta > 0.0 ? 0.15f : -0.15f;
        zoom = Mth.clamp(zoom + zoomChange, -0.5f, 2f);

        return true;
    }
}
