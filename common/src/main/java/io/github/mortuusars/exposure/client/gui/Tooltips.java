package io.github.mortuusars.exposure.client.gui;

import com.google.common.base.Preconditions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Tooltips {
    public static <T> Map<T, Tooltip> createMap(List<T> values, Function<T, Component> convertFunc) {
        Preconditions.checkArgument(!values.isEmpty(), "values list must not be empty.");
        Map<T, Tooltip> map = new HashMap<>();
        for (T value : values) {
            map.put(value, Tooltip.create(convertFunc.apply(value)));
        }
        return map;
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Font font, List<Component> components, int x, int y) {
        guiGraphics.renderTooltip(
                font,
                components.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
                x,
                y,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    public static void renderFormattedTooltip(GuiGraphics guiGraphics, Font font, List<FormattedCharSequence> components, int x, int y) {
        guiGraphics.renderTooltip(
                font,
                components.stream().map(ClientTooltipComponent::create).toList(),
                x,
                y,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }
}
