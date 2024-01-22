package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TextBlock extends AbstractWidget {
    public int fontColor = 0xFF000000;
    public boolean drawShadow = false;
    public HorizontalAlignment alignment = HorizontalAlignment.LEFT;

    private final Font font;
    private final Function<Style, Boolean> componentClickedHandler;

    private List<FormattedCharSequence> renderedLines;
    private List<FormattedCharSequence> tooltipLines;

    public TextBlock(Font font, int x, int y, int width, int height, Component message, Function<Style, Boolean> componentClickedHandler) {
        super(x, y, width, height, message);
        this.font = font;
        this.componentClickedHandler = componentClickedHandler;

        makeLines();
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        makeLines();
    }

    protected void makeLines() {
        Component text = getMessage();
        List<FormattedCharSequence> lines = font.split(text, getWidth());

        int availableLines = Math.min(lines.size(), height / font.lineHeight);

        List<FormattedCharSequence> visibleLines = new ArrayList<>();
        for (int i = 0; i < availableLines; i++) {
            FormattedCharSequence line = lines.get(i);

            if (i == availableLines - 1 && availableLines < lines.size()) {
                line = FormattedCharSequence.composite(line,
                        Component.literal("...").withStyle(text.getStyle()).getVisualOrderText());
            }

            visibleLines.add(line);
        }

        List<FormattedCharSequence> hiddenLines = Collections.emptyList();
        if (availableLines < lines.size()) {
            hiddenLines = new ArrayList<>(lines.stream()
                    .skip(availableLines)
                    .toList());

            hiddenLines.set(0, FormattedCharSequence.composite(
                    FormattedCharSequence.forward("...", text.getStyle()), hiddenLines.get(0)));
        }

        this.renderedLines = visibleLines;
        this.tooltipLines = hiddenLines;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Style style = getClickedComponentStyleAt(mouseX, mouseY);
        return button == 0 && style != null && componentClickedHandler.apply(style);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return getMessage().copy();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < renderedLines.size(); i++) {
            FormattedCharSequence line = renderedLines.get(i);

            int x = getX() + alignment.align(getWidth(), font.width(line));
            guiGraphics.drawString(font, line, x, getY() + font.lineHeight * i, fontColor, drawShadow);
        }

        if (isHovered()) {
            Style style = getClickedComponentStyleAt(mouseX, mouseY);
            if (style != null)
                guiGraphics.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
        }

        if (!tooltipLines.isEmpty() && isMouseOver(mouseX, mouseY))
            guiGraphics.renderTooltip(font, tooltipLines, DefaultTooltipPositioner.INSTANCE, mouseX, mouseY);
    }

    public @Nullable Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        if (renderedLines.isEmpty())
            return null;

        int x = Mth.floor(mouseX - getX());
        int y = Mth.floor(mouseY - getY());

        if (x < 0 || y < 0 || x > getWidth() || y > getHeight())
            return null;

        int hoveredLine = y / font.lineHeight;

        if (hoveredLine >= renderedLines.size())
            return null;

        FormattedCharSequence line = renderedLines.get(hoveredLine);
        int lineStart = alignment.align(getWidth(), font.width(line));

        if (x < lineStart)
            return null;


        return font.getSplitter().componentStyleAtWidth(line, x - lineStart);
    }
}
