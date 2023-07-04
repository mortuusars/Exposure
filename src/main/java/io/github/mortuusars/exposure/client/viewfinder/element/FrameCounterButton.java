package io.github.mortuusars.exposure.client.viewfinder.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraOld;
import io.github.mortuusars.exposure.config.ClientConfig;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FrameCounterButton extends ImageButton {
    private final Screen screen;
    private final ResourceLocation texture;

    public FrameCounterButton(Screen screen, int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, 0, 0, 0, texture, 256, 256, button -> {}, Button.NO_TOOLTIP, Component.empty());
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
        blit(poseStack, x, y, 49, 0, width, height);

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);

        String text = camera.getItem().getAttachments(camera.getStack()).getFilm().map(film -> {
            int exposedFrames = film.getItem().getFrames(film.getStack()).size();
            int totalFrames = film.getItem().getFrameCount();
            return exposedFrames + "/" + totalFrames;
        }).orElse("-");

        Font font = minecraft.font;
        int textWidth = font.width(text);
        int xPos = 16 + (27 - textWidth) / 2;

        font.draw(poseStack, text, x + xPos, y + 5, ClientConfig.getSecondaryFontColor());
        font.draw(poseStack, text, x + xPos, y + 4, ClientConfig.getMainFontColor());
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, Component.translatable("gui.exposure.viewfinder.film_frame_counter.tooltip"), mouseX, mouseY);
    }
}
