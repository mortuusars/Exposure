package io.github.mortuusars.exposure.client.gui.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographClientTooltip implements ClientTooltipComponent {
    public static final int SIZE = 72;

    protected final PhotographTooltip tooltip;
    protected final List<ItemAndStack<PhotographItem>> photographs;

    public PhotographClientTooltip(PhotographTooltip tooltip) {
        this.tooltip = tooltip;
        this.photographs = tooltip.photographs();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return SIZE;
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return SIZE + 2; // 2px bottom margin
    }

    @Override
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, int width, int height, GuiGraphics guiGraphics) {
        int photographsCount = photographs.size();
        int additionalPhotographs = Math.min(2, photographsCount - 1);

        float scale = SIZE;
        float nextPhotographOffset = ExposureClient.photographRenderer().getStackedPhotographOffset();
        scale *= 1f - (additionalPhotographs * nextPhotographOffset);
        PoseStack poseStack = new PoseStack();
        poseStack.translate(mouseX, mouseY, 0f);
        poseStack.scale(scale, scale, 1f);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        ExposureClient.photographRenderer().renderStackedPhotographs(photographs, poseStack, bufferSource,
                LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();

        // Stack count:
        if (photographsCount > 1) {
            guiGraphics.pose().pushMatrix();
            String count = Integer.toString(photographsCount);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            guiGraphics.pose().translate(
                    mouseX + scale - 2 - fontWidth * fontScale,
                    mouseY + scale - 2 - 8 * fontScale);
            guiGraphics.pose().scale(fontScale, fontScale);
            guiGraphics.drawString(font, count, 0, 0, 0xFFFFFFFF);
            guiGraphics.pose().popMatrix();
        }
    }
}
