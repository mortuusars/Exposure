package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

public interface IElementWithTooltip {
    void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY);
}
