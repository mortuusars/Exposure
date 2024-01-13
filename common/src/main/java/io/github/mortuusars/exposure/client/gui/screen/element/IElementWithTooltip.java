package io.github.mortuusars.exposure.client.gui.screen.element;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public interface IElementWithTooltip {
    void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY);
}
