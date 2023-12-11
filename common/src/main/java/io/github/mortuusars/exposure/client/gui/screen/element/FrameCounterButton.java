package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.gui.screen.IElementWithTooltip;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FrameCounterButton extends ImageButton implements IElementWithTooltip {
    private final int secondaryFontColor;
    private final int mainFontColor;

    public FrameCounterButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(x, y, width, height, u, v, height, texture, 256, 256, button -> {}, Component.empty());
        secondaryFontColor = Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR();
        mainFontColor = Config.Client.VIEWFINDER_FONT_MAIN_COLOR();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
        super.render(guiGraphics, mouseX, mouseY, pPartialTick);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, pPartialTick);

        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);

        String text = camera.isEmpty() ? "-" : camera.getItem().getFilm(camera.getStack()).map(film -> {
            int exposedFrames = film.getItem().getExposedFrames(film.getStack()).size();
            int totalFrames = film.getItem().getMaxFrameCount(film.getStack());
            return exposedFrames + "/" + totalFrames;
        }).orElse("-");

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = 15 + (27 - textWidth) / 2;

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 8, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 7, mainFontColor, false);
    }

    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<Component> components = new ArrayList<>();
        components.add(Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip"));

        CameraInHand camera = CameraInHand.getActive(Minecraft.getInstance().player);
        if (!camera.isEmpty() && camera.getItem().getFilm(camera.getStack()).isEmpty()) {
            components.add(Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip.no_film")
                    .withStyle(Style.EMPTY.withColor(0xdd6357)));
        }

        guiGraphics.renderTooltip(Minecraft.getInstance().font, components, Optional.empty(), mouseX, mouseY);
    }
}
