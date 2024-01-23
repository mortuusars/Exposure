package io.github.mortuusars.exposure.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.render.ExposureRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhotographTooltip implements ClientTooltipComponent, TooltipComponent {
    public static final int SIZE = 72;
    @Nullable
    private final Either<String, ResourceLocation> idOrTexture;
    private final int photographs;

    public PhotographTooltip(@Nullable Either<String, ResourceLocation> idOrTexture, int photographs) {
        this.idOrTexture = idOrTexture;
        this.photographs = photographs;
    }

    public PhotographTooltip(@Nullable Either<String, ResourceLocation> idOrTexture) {
        this.idOrTexture = idOrTexture;
        this.photographs = 1;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE + 2; // 2px bottom margin
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
        int additionalPhotographs = Math.min(2, photographs - 1);

        poseStack.pushPose();
        poseStack.translate(mouseX, mouseY, blitOffset);
        float scale = SIZE / (float) ExposureRenderer.SIZE;
        float nextPhotographOffset = 0.03125f;
        scale *= 1f - (additionalPhotographs * nextPhotographOffset);
        poseStack.scale(scale, scale, 1f);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // Rendering paper bottom to top:
        for (int i = additionalPhotographs; i > 0; i--) {
            float posOffset = ExposureRenderer.SIZE * (nextPhotographOffset * i);
            int brightness = Mth.clamp((int)((1f - (0.2f * i)) * 255), 0, 255);

            float rotateOffset = ExposureRenderer.SIZE / 2f;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, 0);

            poseStack.translate(rotateOffset, rotateOffset, 0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(i * 90 + 90));
            poseStack.translate(-rotateOffset, -rotateOffset, 0);

            ExposureClient.getExposureRenderer().renderPaperTexture(poseStack,
                    bufferSource, 0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 255);

            poseStack.popPose();
        }

        if (idOrTexture != null) {
            ExposureClient.getExposureRenderer().renderOnPaper(idOrTexture, poseStack, bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, 255, 255, 255, 255, false);
        }
        else {
            ExposureClient.getExposureRenderer().renderPaperTexture(poseStack, bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
        }

        bufferSource.endBatch();


        poseStack.popPose();

        // Stack count:
        if (photographs > 1) {
            poseStack.pushPose();
            String count = Integer.toString(photographs);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            poseStack.translate(
                    mouseX + ExposureRenderer.SIZE * scale - 2 - fontWidth * fontScale,
                    mouseY + ExposureRenderer.SIZE * scale - 2 - 8 * fontScale,
                    10);
            poseStack.scale(fontScale, fontScale, fontScale);
            font.drawShadow(poseStack, count, 0, 0, 0xFFFFFFFF);
            poseStack.popPose();
        }
    }
}
