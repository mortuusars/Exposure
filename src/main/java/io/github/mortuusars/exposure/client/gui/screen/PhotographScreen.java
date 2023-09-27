package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.renderer.PhotographRenderer;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
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
    private final List<ItemAndStack<PhotographItem>> photographs;

    private int currentIndex = 0;
    private float zoom = 0f;
    private long lastCycledAt;
    private boolean playing = false;

    private double xPos;
    private double yPos;

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        super(Component.empty());
        Preconditions.checkState(photographs.size() > 0, "No photographs to display.");
        this.photographs = photographs;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft = Minecraft.getInstance();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        xPos = width / 2f;
        yPos = height / 2f;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (playing)
            cyclePhotograph(false);

        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        float phWidth = PhotographRenderer.SIZE;
        float phHeight = PhotographRenderer.SIZE;

        float scale = (height - (height / 6f)) / phHeight;
        scale += zoom;

        poseStack.pushPose();

        // Move to center
        poseStack.translate(xPos, yPos, 0);
        // Scale
        poseStack.scale(scale, scale, scale);
        // Set origin point to center (for scale)
        poseStack.translate(phWidth / -2d, phHeight / -2d, 0);

        ItemAndStack<PhotographItem> photograph = photographs.get(currentIndex);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
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
                cyclePhotograph(false);
                playing = false;
                handled = true;
            }
            else if (minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT) {
                cyclePhotograph(true);
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
        }

        return handled;
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

    private void cyclePhotograph(boolean backwards) {
        if (Util.getMillis() - lastCycledAt < 83) // 12 fps
            return;

        int prevIndex = currentIndex;

        currentIndex += backwards ? -1 : 1;
        if (currentIndex >= photographs.size())
            currentIndex = 0;
        else if (currentIndex < 0)
            currentIndex = photographs.size() - 1;

        if (prevIndex != currentIndex && minecraft != null && minecraft.player != null) {
            lastCycledAt = Util.getMillis();
            minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.8f,
                    minecraft.player.level.getRandom().nextFloat() * 0.2f + (backwards ? 0.9f : 1.1f));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float zoomChange = delta > 0.0 ? 0.05f : -0.05f;
        float modifier = Mth.map(zoom, -0.5f, 2f, 1f, 8f);
        zoom = Mth.clamp(zoom + (zoomChange * modifier), -0.5f, 2f);

        if (zoom > -0.5f & zoom < 2f) {
            if (zoom < 1.5f) {
                double moveDelta = (zoomChange * modifier) * -1;
                xPos = Mth.lerp(moveDelta, xPos, mouseX);
                yPos = Mth.lerp(moveDelta, yPos, mouseY);
            }
        }

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        boolean handled = super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        if (!handled && pButton == 1) {
            this.xPos += pDragX;
            this.yPos += pDragY;

            this.xPos = Mth.clamp(xPos, -128f * zoom, width + 128f * zoom);
            this.yPos = Mth.clamp(yPos, -128f * zoom, height + 128f * zoom);
            handled = true;
        }

        return handled;
    }
}
