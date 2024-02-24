package io.github.mortuusars.exposure.gui.component;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.render.ExposureRenderer;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
        int additionalPhotographs = Math.min(2, photographs - 1);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(mouseX, mouseY, 0);
        float scale = SIZE / (float) ExposureRenderer.SIZE;
        float nextPhotographOffset = 0.03125f;
        scale *= 1f - (additionalPhotographs * nextPhotographOffset);
        guiGraphics.pose().scale(scale, scale, 1f);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // Rendering paper bottom to top:
        for (int i = additionalPhotographs; i > 0; i--) {
            float posOffset = ExposureRenderer.SIZE * (nextPhotographOffset * i);
            int brightness = Mth.clamp((int)((1f - (0.2f * i)) * 255), 0, 255);

            float rotateOffset = ExposureRenderer.SIZE / 2f;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(posOffset, posOffset, 0);

            guiGraphics.pose().translate(rotateOffset, rotateOffset, 0);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(i * 90 + 90));
            guiGraphics.pose().translate(-rotateOffset, -rotateOffset, 0);

            ExposureClient.getExposureRenderer().renderPaperTexture(guiGraphics.pose(),
                    bufferSource, 0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 255);

            guiGraphics.pose().popPose();
        }

        if (idOrTexture != null) {
            ExposureClient.getExposureRenderer().renderOnPaper(idOrTexture, ExposurePixelModifiers.EMPTY, guiGraphics.pose(), bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, 255, 255, 255, 255, false);
        }
        else {
            ExposureClient.getExposureRenderer().renderPaperTexture(guiGraphics.pose(), bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
        }

        bufferSource.endBatch();


        guiGraphics.pose().popPose();

        // Stack count:
        if (photographs > 1) {
            guiGraphics.pose().pushPose();
            String count = Integer.toString(photographs);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            guiGraphics.pose().translate(
                    mouseX + ExposureRenderer.SIZE * scale - 2 - fontWidth * fontScale,
                    mouseY + ExposureRenderer.SIZE * scale - 2 - 8 * fontScale,
                    10);
            guiGraphics.pose().scale(fontScale, fontScale, fontScale);
            guiGraphics.drawString(font, count, 0, 0, 0xFFFFFFFF);
            guiGraphics.pose().popPose();
        }
    }
}
