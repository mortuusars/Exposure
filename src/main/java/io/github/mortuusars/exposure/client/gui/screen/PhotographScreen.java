package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.capture.component.FileSaveComponent;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.client.renderer.PhotographRenderer;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PhotographScreen extends Screen {
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/gui/widgets.png");
    public static final int BUTTON_SIZE = 16;

    private final List<ItemAndStack<PhotographItem>> photographs;
    private final List<String> savedExposures = new ArrayList<>();
    private int currentIndex = 0;
    private long lastCycledAt;

    private final ZoomHandler zoom;
    private float zoomFactor;
    private float x;
    private float y;

    private ImageButton previousButton;
    private ImageButton nextButton;

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        super(Component.empty());
        Preconditions.checkState(photographs.size() > 0, "No photographs to display.");
        this.photographs = photographs;
        this.zoom = new ZoomHandler();

        // Query all photographs:
        for (ItemAndStack<PhotographItem> photograph : photographs) {
            @Nullable Either<String, ResourceLocation> idOrTexture = photograph.getItem()
                    .getIdOrTexture(photograph.getStack());
            if (idOrTexture != null)
                idOrTexture.ifLeft(id -> Exposure.getStorage().getOrQuery(id));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft = Minecraft.getInstance();
        zoomFactor = (float) height / PhotographRenderer.SIZE;
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        if (photographs.size() > 1) {
            previousButton = new ImageButton(0, (int) (height / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                    0, 0, BUTTON_SIZE, WIDGETS_TEXTURE, this::buttonPressed);
            nextButton = new ImageButton(width - BUTTON_SIZE, (int) (height / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                    16, 0, BUTTON_SIZE, WIDGETS_TEXTURE, this::buttonPressed);

            addRenderableWidget(previousButton);
            addRenderableWidget(nextButton);
        }
    }

    private void buttonPressed(Button button) {
        if (button == previousButton)
            cyclePhotograph(Navigation.PREVIOUS);
        else if (button == nextButton)
            cyclePhotograph(Navigation.NEXT);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        zoom.update(partialTick);
        float scale = zoom.get() * zoomFactor;

        poseStack.pushPose();

        poseStack.translate(x, y, 0);
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(PhotographRenderer.SIZE / -2f, PhotographRenderer.SIZE / -2f, 0);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance()
                .getBuilder());

        // Rendering paper bottom to top:
        for (int i = Math.min(2, photographs.size() - 1); i > 0; i--) {
            float posOffset = 4 * i;
            int brightness = Mth.clamp(255 - 50 * i, 0, 255);

            float rotateOffset = PhotographRenderer.SIZE / 2f;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, 0);

            poseStack.translate(rotateOffset, rotateOffset, 0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(i * 90 + 90));
            poseStack.translate(-rotateOffset, -rotateOffset, 0);

            PhotographRenderer.renderTexture(PhotographRenderer.PHOTOGRAPH_TEXTURE, poseStack,
                    bufferSource, 0, 0, PhotographRenderer.SIZE, PhotographRenderer.SIZE, 0, 0, 1, 1,
                    LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 255);

            poseStack.popPose();
        }

        ItemAndStack<PhotographItem> photograph = photographs.get(currentIndex);
        @Nullable Either<String, ResourceLocation> idOrTexture = photograph.getItem().getIdOrTexture(photograph.getStack());
        PhotographRenderer.renderOnPaper(idOrTexture, poseStack, bufferSource, LightTexture.FULL_BRIGHT, false);
        bufferSource.endBatch();

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, 500); // Otherwise exposure will overlap buttons
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();

        trySaveToFile(photograph, idOrTexture);
    }

    private void trySaveToFile(ItemAndStack<PhotographItem> photograph, @Nullable Either<String, ResourceLocation> idOrTexture) {
        if (!Config.Client.EXPOSURE_SAVING.get() || idOrTexture == null || Minecraft.getInstance().player == null)
            return;

        CompoundTag tag = photograph.getStack().getTag();
        if (tag == null
                || !tag.contains("PhotographerId", Tag.TAG_INT_ARRAY)
                || !tag.getUUID("PhotographerId").equals(Minecraft.getInstance().player.getUUID())) {
            return;
        }

        idOrTexture.ifLeft(id -> {
            if (savedExposures.contains(id))
                return;

            Exposure.getStorage().getOrQuery(id).ifPresent(exposure -> {
                savedExposures.add(id);
                new Thread(() -> FileSaveComponent.withDefaultFolders(id)
                        .save(exposure.getPixels(), exposure.getWidth(), exposure.getHeight(), exposure.getType()), "ExposureSaving").start();
            });
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        if (minecraft.options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT)
            cyclePhotograph(Navigation.PREVIOUS);
        else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT)
            cyclePhotograph(Navigation.NEXT);
        else if (minecraft.options.keyInventory.matches(keyCode, scanCode))
            this.onClose();
        else if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS)
            zoom(ZoomDirection.IN);
        else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS)
            zoom(ZoomDirection.OUT);
        else
            return false;

        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);
        if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT
                || minecraft.options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            lastCycledAt = 0;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void cyclePhotograph(Navigation navigation) {
        if (Util.getMillis() - lastCycledAt < 50)
            return;

        int prevIndex = currentIndex;

        currentIndex += navigation == Navigation.NEXT ? 1 : -1;
        if (currentIndex >= photographs.size())
            currentIndex = 0;
        else if (currentIndex < 0)
            currentIndex = photographs.size() - 1;

        if (prevIndex != currentIndex && minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.8f,
                    minecraft.player.level.getRandom()
                            .nextFloat() * 0.2f + (navigation == Navigation.NEXT ? 1.1f : 0.9f));
            lastCycledAt = Util.getMillis();
        }
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (!handled && button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float) Mth.clamp(x + dragX, -centerX, centerX);
            y = (float) Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }
}
