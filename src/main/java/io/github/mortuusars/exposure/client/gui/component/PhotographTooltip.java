package io.github.mortuusars.exposure.client.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

public class PhotographTooltip implements ClientTooltipComponent, TooltipComponent {
    private final ItemAndStack<PhotographItem> photograph;
    private static final int SIZE = 86;

    public PhotographTooltip(ItemAndStack<PhotographItem> photograph) {
        this.photograph = photograph;
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
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, @NotNull PoseStack poseStack, @NotNull ItemRenderer itemRenderer, int blitOffset) {
        photograph.getItem().getIdOrResource(photograph.getStack())
                .ifPresent(idOrResource -> {
                    poseStack.pushPose();
                    poseStack.translate(mouseX, mouseY, blitOffset);
                    float scale = SIZE / (float) PhotographRenderer.SIZE;
                    poseStack.scale(scale, scale, scale);
                    PhotographRenderer.render(idOrResource, poseStack);
                    poseStack.popPose();
                });
    }
}
