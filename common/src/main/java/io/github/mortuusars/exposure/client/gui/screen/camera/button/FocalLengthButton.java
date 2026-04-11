package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class FocalLengthButton extends ImageButton {
    protected final int secondaryFontColor;
    protected final int mainFontColor;

    public FocalLengthButton(int x, int y, int width, int height, WidgetSprites sprites) {
        super(x, y, width, height, sprites, button -> {}, Component.empty());
        mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
        secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
    }

    @Override
    public void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick);

        int focalLength = (int)Math.round(Fov.fovToFocalLength(getCurrentFov()));

        Font font = Minecraft.getInstance().font;
        MutableComponent text = Component.translatable("gui.exposure.camera_controls.focal_length", focalLength);
        int textWidth = font.width(text);
        int xPos = 17 + (29 - textWidth) / 2;

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 8, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 7, mainFontColor, false);
    }

    protected double getCurrentFov() {
        return Minecrft.get().gameRenderer.getFov(
                Minecrft.get().gameRenderer.getMainCamera(),
                Minecrft.get().getDeltaTracker().getGameTimeDeltaTicks(),
                true);
    }
}
