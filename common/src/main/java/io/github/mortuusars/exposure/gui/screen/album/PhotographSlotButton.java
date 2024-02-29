package io.github.mortuusars.exposure.gui.screen.album;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.render.PhotographRenderProperties;
import io.github.mortuusars.exposure.render.PhotographRenderer;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class PhotographSlotButton extends ImageButton {
    protected final Screen screen;
    protected final Rect2i exposureArea;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final ResourceLocation resourceLocation;
    protected final int textureWidth;
    protected final int textureHeight;
    protected final OnPress onRightButtonPress;
    protected final Supplier<ItemStack> photograph;
    protected final boolean isEditable;
    protected boolean hasPhotograph;

    public PhotographSlotButton(Screen screen, Rect2i exposureArea, int x, int y, int width, int height, int xTexStart, int yTexStart,
                                int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight,
                                OnPress onLeftButtonPress, OnPress onRightButtonPress, Supplier<ItemStack> photographGetter, boolean isEditable) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onLeftButtonPress,
                Component.translatable("item.exposure.photograph"));
        this.screen = screen;
        this.exposureArea = exposureArea;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.resourceLocation = resourceLocation;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.onRightButtonPress = onRightButtonPress;
        this.photograph = photographGetter;
        this.isEditable = isEditable;
    }

    public ItemStack getPhotograph() {
        return photograph.get();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ItemStack photograph = getPhotograph();

        if (photograph.getItem() instanceof PhotographItem) {
            hasPhotograph = true;

            PhotographRenderProperties renderProperties = PhotographRenderProperties.get(photograph);

            // Paper
            renderTexture(poseStack, renderProperties.getAlbumPaperTexture(),
                    x, y, 0, 0, 0, width, height, width, height);

            // Exposure
            poseStack.pushPose();
            float scale = exposureArea.getWidth() / (float) ExposureClient.getExposureRenderer().getSize();
            poseStack.translate(exposureArea.getX(), exposureArea.getY(), 1);
            poseStack.scale(scale, scale, scale);
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            PhotographRenderer.render(photograph, false, false, poseStack,
                    bufferSource, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
            bufferSource.endBatch();
            poseStack.popPose();

            // Paper overlay
            if (renderProperties.hasAlbumPaperOverlayTexture()) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 2);
                renderTexture(poseStack, renderProperties.getAlbumPaperOverlayTexture(),
                        x, y, 0, 0, 0, width, height, width, height);
                poseStack.popPose();
            }
        }
        else {
            hasPhotograph = false;
        }

        // Album pins
        int xTex = xTexStart + (hasPhotograph ? getWidth() : 0);
        renderTexture(poseStack, resourceLocation, x, y, xTex, yTexStart, yDiffTex, width, height, textureWidth, textureHeight);
    }

    public void renderTexture(PoseStack poseStack, ResourceLocation resourceLocation, int x, int y,
                              int xTexStart, int yTexStart, int yDiffTex, int width, int height,
                              int textureWidth, int textureHeight) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, resourceLocation);
        int i = yTexStart;
        if (!this.isActive()) {
            i += yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            i += yDiffTex;
        }

        RenderSystem.enableDepthTest();
        blit(poseStack, x, y, xTexStart, i, width, height, textureWidth, textureHeight);
    }

    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        if (isEditable && !hasPhotograph) {
            screen.renderTooltip(poseStack, Component.translatable("gui.exposure.album.add_photograph"), mouseX, mouseY);
            return;
        }

        ItemStack photograph = getPhotograph();
        if (photograph.isEmpty())
            return;

        List<Component> itemTooltip = screen.getTooltipFromItem(photograph);
        itemTooltip.add(Component.translatable("gui.exposure.album.left_click_or_scroll_up_to_view"));
        if (isEditable)
            itemTooltip.add(Component.translatable("gui.exposure.album.right_click_to_remove"));

        // Photograph image in tooltip is not rendered here

        if (isFocused())
            screen.renderTooltip(poseStack, Lists.transform(itemTooltip, Component::getVisualOrderText), mouseX, mouseY);
        else
            screen.renderTooltip(poseStack, itemTooltip, Optional.empty(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !clicked(mouseX, mouseY))
            return false;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.onPress.onPress(this);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            this.onRightButtonPress.onPress(this);
        } else
            return false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && clicked(mouseX, mouseY) && hasPhotograph) {
            this.onPress.onPress(this);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible && Screen.hasShiftDown() &&
                (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER || keyCode == InputConstants.KEY_SPACE)) {
            onRightButtonPress.onPress(this);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
