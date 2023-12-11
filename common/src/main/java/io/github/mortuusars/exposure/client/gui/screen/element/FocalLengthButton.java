package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.client.gui.screen.IElementWithTooltip;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FocalLengthButton extends ImageButton implements IElementWithTooltip {
    private final int secondaryFontColor;
    private final int mainFontColor;

    public FocalLengthButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(x, y, width, height, u, v, height, texture, 256, 256, button -> {}, Component.empty());
        secondaryFontColor = Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR();
        mainFontColor = Config.Client.VIEWFINDER_FONT_MAIN_COLOR();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
        super.render(guiGraphics, mouseX, mouseY, pPartialTick);
    }

    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("gui.exposure.viewfinder.focal_length.tooltip"), mouseX, mouseY);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        int focalLength = (int)Math.round(Fov.fovToFocalLength(ViewfinderClient.getCurrentFov()));

        Font font = Minecraft.getInstance().font;
        MutableComponent text = Component.translatable("gui.exposure.viewfinder.focal_length", focalLength);
        int textWidth = font.width(text);
        int xPos = 17 + (29 - textWidth) / 2;

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 8, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 7, mainFontColor, false);
    }
}
