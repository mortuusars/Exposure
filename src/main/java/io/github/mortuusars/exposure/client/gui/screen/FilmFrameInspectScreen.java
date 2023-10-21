package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.film.FrameData;
import io.github.mortuusars.exposure.client.renderer.PhotographRenderer;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FilmFrameInspectScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/gui/widgets.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;
    public static final int BUTTON_SIZE = 16;

    private final LightroomScreen lightroomScreen;
    private final LightroomMenu lightroomMenu;

    private final ZoomHandler zoom;
    private float zoomFactor;
    private float x;
    private float y;

    private ImageButton previousButton;
    private ImageButton nextButton;

    public FilmFrameInspectScreen(LightroomScreen lightroomScreen, LightroomMenu lightroomMenu) {
        super(Component.empty());
        this.lightroomScreen = lightroomScreen;
        this.lightroomMenu = lightroomMenu;
        this.zoom = new ZoomHandler();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private LightroomMenu getLightroomMenu() {
        return lightroomMenu;
    }

    @Override
    protected void init() {
        super.init();
        zoomFactor = (float) height / BG_SIZE;
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        previousButton = new ImageButton(0, (int) (height / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                0, 0, BUTTON_SIZE, WIDGETS_TEXTURE, this::buttonPressed);
        nextButton = new ImageButton(width - BUTTON_SIZE, (int) (height / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                16, 0, BUTTON_SIZE, WIDGETS_TEXTURE, this::buttonPressed);

        addRenderableWidget(previousButton);
        addRenderableWidget(nextButton);
    }

    private void buttonPressed(Button button) {
        if (button == previousButton)
            lightroomScreen.changeFrame(Navigation.PREVIOUS);
        else if (button == nextButton)
            lightroomScreen.changeFrame(Navigation.NEXT);
    }

    public void close() {
        Minecraft.getInstance().setScreen(lightroomScreen);
        Objects.requireNonNull(Minecraft.getInstance().player).playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 0.7f);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        int currentFrame = getLightroomMenu().getSelectedFrame();
        @Nullable FrameData frame = getLightroomMenu().getFrameByIndex(currentFrame);

        zoom.update(partialTick);
        float scale = zoom.get() * zoomFactor;

        if (zoom.getTargetZoom() == zoom.getMinZoom() && Math.abs(zoom.getMinZoom() - zoom.get()) < 0.1f) {
            close();
            return;
        }

        boolean colorFilm = getLightroomMenu().isColorFilm();

        poseStack.pushPose();

        poseStack.translate(x, y, 0);
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale(scale, scale, scale);

        RenderSystem.setShaderTexture(0, TEXTURE);

        poseStack.translate(BG_SIZE / -2f, BG_SIZE / -2f, 0);

        GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

        if (colorFilm)
            RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 1.0F);

        GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

        poseStack.translate(12, 12, 0);
        if (frame != null)
            renderFrame(frame, poseStack, 0, 0, 1f, colorFilm);

        poseStack.popPose();

        previousButton.visible = currentFrame != 0;
        previousButton.active = currentFrame != 0;
        nextButton.visible = currentFrame != getLightroomMenu().getTotalFrames() - 1;
        nextButton.active = currentFrame != getLightroomMenu().getTotalFrames() - 1;

        poseStack.pushPose();
        poseStack.translate(0, 0, 500); // Otherwise exposure will overlap buttons
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
    }

    private void renderFrame(@NotNull FrameData frame, PoseStack poseStack, float x, float y, float alpha, boolean colorFilm) {
        Exposure.getStorage().getOrQuery(frame.id).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(frame.id, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 180, 130, 110,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));
            else
                ExposureClient.getExposureRenderer().renderNegative(frame.id, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 255, 255, 255,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));

            bufferSource.endBatch();

            poseStack.popPose();
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        Preconditions.checkState(minecraft != null);

        if (minecraft.options.keyInventory.matches(keyCode, scanCode))
            close();
        else if (minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT)
            lightroomScreen.changeFrame(Navigation.PREVIOUS);
        else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT)
            lightroomScreen.changeFrame(Navigation.NEXT);
        else if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS)
            zoom(ZoomDirection.IN);
        else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS)
            zoom(ZoomDirection.OUT);
        else
            return false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            zoom(delta >= 0.0 ? ZoomDirection.IN : ZoomDirection.OUT);
            return true;
        }

        return true;
    }

    private void zoom(ZoomDirection direction) {
        float change = 0.1f + 0.3f * (zoom.getTargetZoom() - zoom.getMinZoom());
        zoom.add(direction == ZoomDirection.IN ? change : -change);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled)
            return true;

        if (button == 1) { // Right Click
            zoom.set(0f);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (!handled && button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float)Mth.clamp(x + dragX, -centerX, centerX);
            y = (float)Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }
}
