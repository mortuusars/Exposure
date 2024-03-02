package io.github.mortuusars.exposure.gui.component;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.render.PhotographRenderer;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhotographTooltip implements ClientTooltipComponent, TooltipComponent {
    public static final int SIZE = 72;
    private final List<ItemAndStack<PhotographItem>> photographs;

    public PhotographTooltip(ItemStack photographStack) {
        Preconditions.checkArgument(photographStack.getItem() instanceof PhotographItem,
                photographStack + " is not a PhotographItem.");

        this.photographs = List.of(new ItemAndStack<>(photographStack));
    }

    public PhotographTooltip(ItemAndStack<StackedPhotographsItem> stackedPhotographs) {
        this.photographs = stackedPhotographs.getItem().getPhotographs(stackedPhotographs.getStack());
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
        int photographsCount = photographs.size();
        int additionalPhotographs = Math.min(2, photographsCount - 1);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(mouseX, mouseY, 5);
        float scale = SIZE / (float) ExposureClient.getExposureRenderer().getSize();
        float nextPhotographOffset = PhotographRenderer.getStackedPhotographOffset() / ExposureClient.getExposureRenderer().getSize();
        scale *= 1f - (additionalPhotographs * nextPhotographOffset);
        guiGraphics.pose().scale(scale, scale, 1f);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        PhotographRenderer.renderStackedPhotographs(photographs, guiGraphics.pose(), bufferSource,
                LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();

        guiGraphics.pose().popPose();

        // Stack count:
        if (photographsCount > 1) {
            guiGraphics.pose().pushPose();
            String count = Integer.toString(photographsCount);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            guiGraphics.pose().translate(
                    mouseX + ExposureClient.getExposureRenderer().getSize() * scale - 2 - fontWidth * fontScale,
                    mouseY + ExposureClient.getExposureRenderer().getSize() * scale - 2 - 8 * fontScale,
                    10);
            guiGraphics.pose().scale(fontScale, fontScale, fontScale);
            guiGraphics.drawString(font, count, 0, 0, 0xFFFFFFFF);
            guiGraphics.pose().popPose();
        }
    }
}
