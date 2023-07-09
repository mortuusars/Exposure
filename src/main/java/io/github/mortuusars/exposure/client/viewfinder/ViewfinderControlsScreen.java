package io.github.mortuusars.exposure.client.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.viewfinder.ZoomDirection;
import io.github.mortuusars.exposure.client.viewfinder.element.CompositionGuideButton;
import io.github.mortuusars.exposure.client.viewfinder.element.FocalLengthButton;
import io.github.mortuusars.exposure.client.viewfinder.element.FrameCounterButton;
import io.github.mortuusars.exposure.client.viewfinder.element.ShutterSpeedButton;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ViewfinderControlsScreen extends Screen {
    public static final ResourceLocation OVERLAY_TEXTURE = Exposure.resource("textures/misc/viewfinder_controls_overlay.png");
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/misc/viewfinder_controls_widgets.png");

    private final Player player;
    private final ClientLevel level;
    private final long openedAtTimestamp;

    private int leftPos;
    private int topPos;

    public ViewfinderControlsScreen() {
        super(Component.empty());

        player = Minecraft.getInstance().player;
        level = Minecraft.getInstance().level;
        assert level != null;
        openedAtTimestamp = level.getGameTime();

        passEvents = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - 256) / 2;
        topPos = Math.round(ViewfinderRenderer.opening.y + ViewfinderRenderer.opening.height - 256);

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        if (camera.isEmpty())
            throw new IllegalStateException("Active Camera cannot be empty here.");

        int leftSideButtonPos = 18;

        addRenderableOnly(new FocalLengthButton(this,leftPos + leftSideButtonPos, topPos + 237, 49, 19,
                WIDGETS_TEXTURE));

        leftSideButtonPos += 49 - 2;

        addRenderableOnly(new FrameCounterButton(this,leftPos + leftSideButtonPos, topPos + 237, 49, 19,
                WIDGETS_TEXTURE));

        addRenderableWidget(new ShutterSpeedButton(this,leftPos + 197, topPos + 237,
                41, 19, WIDGETS_TEXTURE));

        addRenderableWidget(new CompositionGuideButton(this,leftPos + 179, topPos + 237,
                20, 19, WIDGETS_TEXTURE));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!ViewfinderRenderer.shouldRender()) {
            this.onClose();
            return;
        }

        poseStack.pushPose();

        float viewfinderScale = ViewfinderRenderer.getScale();
        if (viewfinderScale != 1.0f) {
            poseStack.translate(width / 2f, height / 2f, 0);
            poseStack.scale(viewfinderScale, viewfinderScale, viewfinderScale);
            poseStack.translate(-width / 2f, -height / 2f, 0);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, OVERLAY_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        blit(poseStack, leftPos, topPos, 0, 0, 256, 256);
        super.render(poseStack, mouseX, mouseY, partialTick);

        for(Widget widget : this.renderables) {
            if (widget instanceof AbstractWidget abstractWidget && abstractWidget.isHoveredOrFocused()) {
                abstractWidget.renderToolTip(poseStack, mouseX, mouseY);
                break;
            }
        }


        Minecraft.getInstance().font.draw(poseStack, "X: " + mouseX + ", Y: " + mouseY, 10, 10, 0xFFc1b4a3);

        poseStack.popPose();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (!handled && button == 1) {
            CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
            if (!camera.isEmpty()) {
                camera.getItem().useCamera(player, camera.getHand());
                handled = true;
            }
        }

        return handled;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keySprint.matches(keyCode, scanCode)) {
            if (level.getGameTime() - openedAtTimestamp >= 5)
                this.onClose();

            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!super.mouseScrolled(mouseX, mouseY, delta)) {
            ViewfinderRenderer.zoom(delta > 0d ? ZoomDirection.IN : ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }
}
