package io.github.mortuusars.exposure.client.util;

import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GuiUtil {
    public static void blit(GuiGraphics gui, Identifier texture, Rect2f rect) {
        int x = Math.round(rect.x);
        int y = Math.round(rect.y);
        gui.blit(texture, x, y, Math.round(rect.x + rect.width), Math.round(rect.y + rect.height),
                0f, 1f, 0f, 1f);
    }

    public static void drawRect(GuiGraphics gui, Rect2f rect, int color) {
        drawRect(gui, rect.x, rect.y, rect.width, rect.height, color);
    }

    public static void drawRect(GuiGraphics gui, float x, float y, float width, float height, int color) {
        gui.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
    }
    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int width, int color) {
        renderScrollingString(guiGraphics, font, text, x, y, x + width, y + font.lineHeight, color);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(guiGraphics, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    // Doesn't work in toast for some reason.
    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int fontWidth = font.width(text);
        int y = (minY + maxY - 9) / 2 + 1;
        int width = maxX - minX;
        if (fontWidth > width) {
            int remaining = fontWidth - width;
            double d = (double) Util.getMillis() / 400;
            double e = Math.max((double)remaining * 0.5, 3.0);
            double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, remaining);
            guiGraphics.enableScissor(minX, minY, maxX, maxY);
            guiGraphics.drawString(font, text, minX - (int)g, y, color, false);
            guiGraphics.disableScissor();
        } else {
            int l = Mth.clamp(centerX, minX + fontWidth / 2, maxX - fontWidth / 2);
            guiGraphics.drawCenteredString(font, text, l, y, color);
        }
    }
}
