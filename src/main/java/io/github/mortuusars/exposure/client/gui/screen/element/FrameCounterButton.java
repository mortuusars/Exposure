package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FrameCounterButton extends ImageButton {
    private final Screen screen;
    private final int secondaryFontColor;
    private final int mainFontColor;

    public FrameCounterButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(x, y, width, height, u, v, height, texture, 256, 256, button -> {
        }, Button.NO_TOOLTIP, Component.empty());
        this.screen = screen;
        secondaryFontColor = Config.Client.getSecondaryFontColor();
        mainFontColor = Config.Client.getMainFontColor();
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        super.renderButton(poseStack, mouseX, mouseY, pPartialTick);

        CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);

        String text = camera.isEmpty() ? "-" : camera.getItem().getFilm(camera.getStack()).map(film -> {
            int exposedFrames = film.getItem().getExposedFrames(film.getStack()).size();
            int totalFrames = film.getItem().getMaxFrameCount(film.getStack());
            return exposedFrames + "/" + totalFrames;
        }).orElse("-");

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = 15 + (27 - textWidth) / 2;

        font.draw(poseStack, text, x + xPos, y + 8, secondaryFontColor);
        font.draw(poseStack, text, x + xPos, y + 7, mainFontColor);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        List<Component> components = new ArrayList<>();
        components.add(Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip"));

        CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);
        if (!camera.isEmpty() && camera.getItem().getFilm(camera.getStack()).isEmpty()) {
            components.add(Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip.no_film")
                    .withStyle(Style.EMPTY.withColor(0xdd6357)));
        }

        screen.renderTooltip(poseStack, components, Optional.empty(), mouseX, mouseY);
    }
}
