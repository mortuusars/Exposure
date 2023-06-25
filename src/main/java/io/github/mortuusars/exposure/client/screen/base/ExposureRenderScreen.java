package io.github.mortuusars.exposure.client.screen.base;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExposureRenderScreen extends Screen {
    @Nullable
    protected ExposureSavedData exposureData;
    public ExposureRenderScreen(Component title) {
        super(title);
    }

    protected abstract String getExposureId();

    protected void loadExposure() {
        String exposureId = getExposureId();
        if (exposureId != null)
            exposureData = ExposureStorage.CLIENT.getOrQuery(exposureId).orElse(null);
    }

    protected void renderExposure(PoseStack poseStack, boolean negative) {
        renderExposure(poseStack, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), LightTexture.FULL_BRIGHT, negative);
    }

    protected void renderExposure(PoseStack poseStack, int packedLight, boolean negative) {
        renderExposure(poseStack, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), packedLight, negative);
    }

    protected void renderExposure(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int packedLight, boolean negative) {
        String exposureId = getExposureId();
        if (exposureData != null && exposureId != null) {
            if (negative)
                ExposureRenderer.renderNegative(poseStack, bufferSource, exposureId, exposureData, packedLight);
            else
                ExposureRenderer.render(poseStack, bufferSource, exposureId, exposureData, packedLight);

        }

        bufferSource.endBatch();
    }
}
