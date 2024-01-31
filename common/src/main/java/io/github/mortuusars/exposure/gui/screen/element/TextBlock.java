package io.github.mortuusars.exposure.gui.screen.element;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.gui.screen.element.textbox.HorizontalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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

    private final Screen screen;
    private final Font font;
    private final Function<Style, Boolean> componentClickedHandler;

    private List<FormattedCharSequence> renderedLines;
    private List<FormattedCharSequence> tooltipLines;

    public TextBlock(Screen screen,  Font font, int x, int y, int width, int height, Component message, Function<Style, Boolean> componentClickedHandler) {
        super(x, y, width, height, message);
        this.screen = screen;
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return getMessage().copy();
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < renderedLines.size(); i++) {
            FormattedCharSequence line = renderedLines.get(i);

            int x = this.x + alignment.align(getWidth(), font.width(line));
            if (drawShadow)
                font.drawShadow(poseStack, line, x, y + font.lineHeight * i, fontColor);
            else
                font.draw(poseStack, line, x, y + font.lineHeight * i, fontColor);
        }

        if (isHovered) {
            Style style = getClickedComponentStyleAt(mouseX, mouseY);
            if (style != null)
                renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
        }

        if (!tooltipLines.isEmpty() && isMouseOver(mouseX, mouseY))
            screen.renderTooltip(poseStack, tooltipLines, mouseX, mouseY);
    }

    protected void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int mouseX, int mouseY) {
        if (style != null && style.getHoverEvent() != null) {
            HoverEvent hoverevent = style.getHoverEvent();
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = (HoverEvent.ItemStackInfo)hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (hoverevent$itemstackinfo != null) {
                screen.renderTooltip(poseStack, screen.getTooltipFromItem(hoverevent$itemstackinfo.getItemStack()), hoverevent$itemstackinfo.getItemStack().getTooltipImage(), mouseX, mouseY);
            } else {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = (HoverEvent.EntityTooltipInfo)hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (hoverevent$entitytooltipinfo != null) {
                    if (Minecraft.getInstance().options.advancedItemTooltips) {
                        screen.renderComponentTooltip(poseStack, hoverevent$entitytooltipinfo.getTooltipLines(), mouseX, mouseY);
                    }
                } else {
                    Component component = (Component)hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (component != null) {
                        screen.renderTooltip(poseStack, font.split(component, Math.max(this.width / 2, 200)), mouseX, mouseY);
                    }
                }
            }
        }

    }

    public @Nullable Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        if (renderedLines.isEmpty())
            return null;

        int x = Mth.floor(mouseX - this.x);
        int y = Mth.floor(mouseY - this.y);

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
