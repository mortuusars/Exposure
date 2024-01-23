package io.github.mortuusars.exposure.gui.screen.element;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

public interface IElementWithTooltip {
    void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY);
}
