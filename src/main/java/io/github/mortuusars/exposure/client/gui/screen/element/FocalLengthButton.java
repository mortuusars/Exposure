package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.render.ViewfinderRenderer;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FocalLengthButton extends ImageButton {
    private final Screen screen;
    private final ResourceLocation texture;
    private final int secondaryFontColor;
    private final int mainFontColor;

    public FocalLengthButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(x, y, width, height, u, v, height, texture, 256, 256, button -> {}, Button.NO_TOOLTIP, Component.empty());
        this.screen = screen;
        this.texture = texture;
        secondaryFontColor = Config.Client.getSecondaryFontColor();
        mainFontColor = Config.Client.getMainFontColor();
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderButton(poseStack, mouseX, mouseY, partialTick);

        int focalLength = Math.round(Fov.fovToFocalLength(ViewfinderRenderer.getCurrentFov()));

        Font font = Minecraft.getInstance().font;
        MutableComponent text = Component.translatable("gui.exposure.viewfinder.focal_length", focalLength);
        int textWidth = font.width(text);
        int xPos = 17 + (29 - textWidth) / 2;

        font.draw(poseStack, text, x + xPos, y + 8, secondaryFontColor);
        font.draw(poseStack, text, x + xPos, y + 7, mainFontColor);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, Component.translatable("gui.exposure.viewfinder.focal_length.tooltip"), mouseX, mouseY);
    }
}
