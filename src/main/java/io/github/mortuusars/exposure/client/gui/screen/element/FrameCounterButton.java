package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class FrameCounterButton extends ImageButton {
    private final Screen screen;
    private final ResourceLocation texture;

    public FrameCounterButton(Screen screen, int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, 64, 0, height, texture, 256, 256, button -> {}, Button.NO_TOOLTIP, Component.empty());
        this.screen = screen;
        this.texture = texture;
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Button
        blit(poseStack, x, y, 64, isHoveredOrFocused() ? 18 : 0, width, height);

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);

        String text = camera.getItem().getFilm(camera.getStack()).map(film -> {
            int exposedFrames = film.getItem().getExposedFrames(film.getStack()).size();
            int totalFrames = film.getItem().getFrameCount(film.getStack());
            return exposedFrames + "/" + totalFrames;
        }).orElse("-");

        Font font = minecraft.font;
        int textWidth = font.width(text);
        int xPos = 15 + (27 - textWidth) / 2;

        font.draw(poseStack, text, x + xPos, y + 8, Config.Client.getSecondaryFontColor());
        font.draw(poseStack, text, x + xPos, y + 7, Config.Client.getMainFontColor());
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, List.of(Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip"),
                Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip.no_film").withStyle(Style.EMPTY.withColor(0xdd6357))), Optional.empty(), mouseX, mouseY);
    }
}
