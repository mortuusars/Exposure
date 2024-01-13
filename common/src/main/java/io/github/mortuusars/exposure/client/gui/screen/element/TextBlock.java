package io.github.mortuusars.exposure.client.gui.screen.element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextBlock extends AbstractWidget {
    public int fontColor = 0xFF000000;
    public boolean drawShadow = false;
    public boolean centerText = false;

    private final Font font;
    private final Component message;

    public TextBlock(Font font, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.font = font;
        this.message = message;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<FormattedCharSequence> lines = font.split(message, width);

        int availableLines = Math.min(lines.size(), height / font.lineHeight);

        for (int i = 0; i < availableLines; i++) {
            FormattedCharSequence line = lines.get(i);

            if (i == availableLines - 1 && availableLines < lines.size()) {
                line = FormattedCharSequence.composite(line,
                        Component.literal("...").withStyle(message.getStyle()).getVisualOrderText());
            }

            int x = getX() + (centerText ? getWidth() / 2 - font.width(line) / 2 : 0);
            guiGraphics.drawString(font, line, x, getY() + font.lineHeight * i, fontColor, drawShadow);
        }

        if (isMouseOver(mouseX, mouseY) && availableLines < lines.size()) {
            lines = new ArrayList<>(lines.stream()
                    .skip(availableLines)
                    .toList());

            lines.set(0, FormattedCharSequence.composite(FormattedCharSequence.forward("...", message.getStyle()), lines.get(0)));
            @Nullable List<FormattedCharSequence> leftoverText = lines;

            guiGraphics.renderTooltip(font, leftoverText, DefaultTooltipPositioner.INSTANCE, mouseX, mouseY);
        }
    }
}
