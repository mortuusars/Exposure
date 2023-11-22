package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class FilmFrameInspectScreen extends ZoomableScreen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/gui/widgets.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;
    public static final int BUTTON_SIZE = 16;

    private final LightroomScreen lightroomScreen;
    private final LightroomMenu lightroomMenu;

    private ImageButton previousButton;
    private ImageButton nextButton;

    public FilmFrameInspectScreen(LightroomScreen lightroomScreen, LightroomMenu lightroomMenu) {
        super(Component.empty());
        this.lightroomScreen = lightroomScreen;
        this.lightroomMenu = lightroomMenu;
        zoom.minZoom = zoom.defaultZoom / (float)Math.pow(zoom.step, 2f);
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
        if (minecraft.player != null)
            minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 0.7f);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 500); // Otherwise exposure will overlap buttons
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();

        if (zoom.targetZoom == zoom.minZoom/* && Math.abs(zoom.minZoom - zoom.get()) < 0.001f*/) {
            close();
            return;
        }

        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().translate(width / 2f, height / 2f, 0);
        guiGraphics.pose().scale(scale, scale, scale);

        RenderSystem.setShaderTexture(0, TEXTURE);

        guiGraphics.pose().translate(BG_SIZE / -2f, BG_SIZE / -2f, 0);

        GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

        boolean colorFilm = getLightroomMenu().isColorFilm();
        if (colorFilm)
            RenderSystem.setShaderColor(1.2F, 0.96F, 0.75F, 1.0F);

        GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.pose().translate(12, 12, 0);

        int currentFrame = getLightroomMenu().getSelectedFrame();
        String frame = getLightroomMenu().getFrameIdByIndex(currentFrame);
        renderFrame(frame, guiGraphics.pose(), 0, 0, 1f, colorFilm);

        guiGraphics.pose().popPose();

        previousButton.visible = currentFrame != 0;
        previousButton.active = currentFrame != 0;
        nextButton.visible = currentFrame != getLightroomMenu().getTotalFrames() - 1;
        nextButton.active = currentFrame != getLightroomMenu().getTotalFrames() - 1;
    }

    private void renderFrame(String exposureId, PoseStack poseStack, float x, float y, float alpha, boolean colorFilm) {
        if (exposureId.length() == 0)
            return;

        Exposure.getStorage().getOrQuery(exposureId).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 180, 130, 110,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));
            else
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
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

        if (minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT)
            lightroomScreen.changeFrame(Navigation.PREVIOUS);
        else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT)
            lightroomScreen.changeFrame(Navigation.NEXT);
        else
            return false;

        return true;
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
}
