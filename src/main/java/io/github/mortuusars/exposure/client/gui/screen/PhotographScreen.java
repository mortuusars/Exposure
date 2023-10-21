package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.client.renderer.PhotographRenderer;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhotographScreen extends Screen {
    private static final float MIN_ZOOM = 0.25f;
    private static final float MAX_ZOOM = 2f;
    private static final float DEFAULT_ZOOM = 0.75f;
    private static final float ZOOM_IN_SPEED = 0.6f;
    private static final float ZOOM_OUT_SPEED = 0.8f;

    private final List<ItemAndStack<PhotographItem>> photographs;

    private int currentIndex = 0;

    private double x;
    private double y;
    private float zoomFactor;
    private float targetZoom = DEFAULT_ZOOM;
    private float currentZoom = 0.1f;

    private long lastCycledAt;
    private boolean playing = false;

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        super(Component.empty());
        Preconditions.checkState(photographs.size() > 0, "No photographs to display.");
        this.photographs = photographs;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft = Minecraft.getInstance();
        zoomFactor = (float) height / PhotographRenderer.SIZE;
//        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
//        if (playing)
//            cyclePhotograph(Navigation.NEXT);

        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        currentZoom = Mth.lerp(Math.min((currentZoom < targetZoom ? ZOOM_IN_SPEED : ZOOM_OUT_SPEED) * partialTick, 1f), currentZoom, targetZoom);
        float zoom = currentZoom * zoomFactor;

        poseStack.pushPose();

        poseStack.translate(x, y, 0);
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale(zoom, zoom, zoom);
        poseStack.translate(PhotographRenderer.SIZE / -2f, PhotographRenderer.SIZE / -2f, 0);


        ItemAndStack<PhotographItem> photograph = photographs.get(currentIndex);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        poseStack.pushPose();
        poseStack.translate(3, 3, -1);
        PhotographRenderer.renderTexture(PhotographRenderer.PHOTOGRAPH_TEXTURE, poseStack,
                bufferSource, 0, 0, PhotographRenderer.SIZE, PhotographRenderer.SIZE, 0, 0, 1, 1,
                LightTexture.FULL_BRIGHT, 200, 200, 200, 255);
        poseStack.popPose();

        @Nullable Either<String, ResourceLocation> idOrTexture = photograph.getItem().getIdOrTexture(photograph.getStack());
        PhotographRenderer.renderOnPaper(idOrTexture, poseStack, bufferSource, LightTexture.FULL_BRIGHT, false);
        bufferSource.endBatch();

        poseStack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);

        if (!handled) {
            if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
                cyclePhotograph(Navigation.NEXT);
                playing = false;
                handled = true;
            }
            else if (minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT) {
                cyclePhotograph(Navigation.PREVIOUS);
                playing = false;
                handled = true;
            }
            else if (minecraft.options.keyJump.matches(keyCode, scanCode)) {
                playing = !playing;
                handled = true;
            }
            else if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                this.onClose();
                handled = true;
            }
            else if (keyCode - 48 >= 0 && keyCode - 48 <= 10) { // 0 - 9
                int number = keyCode - 48;
                if (number == 0)
                    setZoom(DEFAULT_ZOOM);
                else
                    setZoom(Mth.map(number, 1, 9, MIN_ZOOM, MAX_ZOOM));
                handled = true;
            }
        }

        return handled;
    }

    public void setZoom(float zoom) {
        targetZoom = Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);
        if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT
                || minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT) {
            lastCycledAt = 0;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void cyclePhotograph(Navigation navigation) {
        if (Util.getMillis() - lastCycledAt < 83) // 12 fps
            return;

        int prevIndex = currentIndex;

        currentIndex += navigation == Navigation.NEXT ? 1 : -1;
        if (currentIndex >= photographs.size())
            currentIndex = 0;
        else if (currentIndex < 0)
            currentIndex = photographs.size() - 1;

        if (prevIndex != currentIndex && minecraft != null && minecraft.player != null) {
            lastCycledAt = Util.getMillis();
            minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.8f,
                    minecraft.player.level.getRandom().nextFloat() * 0.2f + (navigation == Navigation.NEXT ? 1.1f : 0.9f));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            float change = 0.1f + 0.3f * (targetZoom - MIN_ZOOM);
            setZoom(targetZoom + (delta >= 0.0 ? change : -change));
            handled = true;
        }

        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (!handled /*&& !isClickedOnButton*/ && button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float)Mth.clamp(x + dragX, -centerX, centerX);
            y = (float)Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }
}
