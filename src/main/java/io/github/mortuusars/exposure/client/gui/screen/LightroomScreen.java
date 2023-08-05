package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LightroomScreen extends AbstractContainerScreen<LightroomMenu> {
    public static final ResourceLocation MAIN_TEXTURE = Exposure.resource("textures/gui/lightroom.png");
    public static final ResourceLocation FILM_OVERLAYS_TEXTURE = Exposure.resource("textures/gui/lightroom_film_overlays.png");
    public static final ResourceLocation ZOOM_CONTAINER_TEXTURE = Exposure.resource("textures/gui/lightroom_zoom_container.png");
    public static final ResourceLocation ZOOM_FILM_OVERLAY_TEXTURE = Exposure.resource("textures/gui/lightroom_zoom_film_overlay.png");

    private ExposureFrame[] visibleFrames = new ExposureFrame[3];
    private boolean zoomedIn = false;

    public LightroomScreen(LightroomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        ExposureStorage.CLIENT.clear();
    }

    @Override
    protected void init() {
        imageWidth = 176;
        imageHeight = 210;
        super.init();
        inventoryLabelY = 116;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        addRenderableWidget(new Button( leftPos + 62, topPos + 87, 48, 20, Component.literal("PRINT"), pButton ->
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.PRINT_BUTTON_ID)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, MAIN_TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (!menu.getSlot(LightroomBlockEntity.FILM_SLOT).hasItem()) {
            zoomedIn = false;
            return;
        }

        updateVisibleFrames();

        if (zoomedIn && visibleFrames[1] == null)
            zoomedIn = false;

        if (visibleFrames[0] == null && visibleFrames[1] == null && visibleFrames[2] == null) {

        }
        else if (zoomedIn) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 999);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, ZOOM_CONTAINER_TEXTURE);
            this.blit(poseStack, leftPos + 16, topPos + 5, 0, 0, 146, 154);
            RenderSystem.setShaderTexture(0, ZOOM_FILM_OVERLAY_TEXTURE);
            this.blit(poseStack, leftPos + 21, topPos + 10, 0, 0, 136, 144);

            Exposure.getStorage().getOrQuery(visibleFrames[1].id).ifPresent(exposureData -> {
                poseStack.pushPose();
                poseStack.translate(leftPos + 25, topPos + 18, 0);
                ExposureClient.getExposureRenderer().render(visibleFrames[1].id, exposureData, true, poseStack, 128, 128);
                poseStack.popPose();
            });
            poseStack.popPose();
        }
        else {
            RenderSystem.setShaderTexture(0, FILM_OVERLAYS_TEXTURE);
            this.blit(poseStack, leftPos + 7, topPos + 16, visibleFrames[0] != null ? 53 : 0, 0, 52, 64);
            this.blit(poseStack, leftPos + 59, topPos + 16, visibleFrames[2] != null ? 0 : 57, 65, 56, 64);
            if (visibleFrames[2] != null) {
                ItemStack filmItemStack = menu.slots.get(LightroomBlockEntity.FILM_SLOT).getItem();
                boolean hasMoreFrames = filmItemStack.getItem() instanceof DevelopedFilmItem developedFilm &&
                        menu.getData().get(LightroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID) < developedFilm.getExposedFrames(filmItemStack).size() - 2;
                this.blit(poseStack, leftPos + 115, topPos + 16, hasMoreFrames ? 55 : 0, 130, 54, 64);
            }
            this.blit(poseStack, leftPos + 63, topPos + 23, 0, 195, 50, 50);

            if (visibleFrames[0] != null)
                renderFrame(visibleFrames[0], poseStack, leftPos + 11, topPos + 24, 0.5f);

            if (visibleFrames[1] != null)
                renderFrame(visibleFrames[1], poseStack, leftPos + 64, topPos + 24, 1f);

            if (visibleFrames[2] != null)
                renderFrame(visibleFrames[2], poseStack, leftPos + 117, topPos + 24, 0.5f);
        }
    }

    private void renderFrame(ExposureFrame frame, PoseStack poseStack, float x, float y, float alpha) {
        Exposure.getStorage().getOrQuery(frame.id).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            ExposureClient.getExposureRenderer().render(frame.id, exposureData, true, poseStack,
                    bufferSource, LightTexture.FULL_BRIGHT, 48, 48, 255, 255, 255,
                    Mth.clamp((int)Math.ceil(alpha * 255), 0, 255));
            bufferSource.endBatch();

            poseStack.popPose();
        });
    }

    private void updateVisibleFrames() {
        clearVisibleFrames();

        Slot filmSlot = menu.slots.get(LightroomBlockEntity.FILM_SLOT);
        if (!filmSlot.hasItem() || !(filmSlot.getItem().getItem() instanceof DevelopedFilmItem filmItem))
            return;

        List<ExposureFrame> exposedFrames = filmItem.getExposedFrames(filmSlot.getItem());
        if (exposedFrames.size() == 0)
            return;

        int currentFrameId = menu.getData().get(LightroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID);
        if (currentFrameId >= 0 && currentFrameId < exposedFrames.size())
            visibleFrames[1] = exposedFrames.get(currentFrameId);

        int previousFrameId = currentFrameId - 1;
        if (previousFrameId >= 0 && previousFrameId < exposedFrames.size())
            visibleFrames[0] = exposedFrames.get(previousFrameId);

        int nextFrameId = currentFrameId + 1;
        if (nextFrameId >= 0 && nextFrameId < exposedFrames.size())
            visibleFrames[2] = exposedFrames.get(nextFrameId);
    }

    private void clearVisibleFrames() {
        visibleFrames = new ExposureFrame[3];
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.gameMode != null);

        if (minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.PREVIOUS_FRAME_BUTTON_ID);
            handled = true;
        } else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.NEXT_FRAME_BUTTON_ID);
            handled = true;
        } else if (minecraft.options.keyJump.matches(keyCode, scanCode)) {
            zoomedIn = !zoomedIn;
            handled = true;
        }

        return handled;
    }
}
