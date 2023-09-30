package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.FrameData;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LightroomScreen extends AbstractContainerScreen<LightroomMenu> {
    public static final ResourceLocation MAIN_TEXTURE = Exposure.resource("textures/gui/lightroom.png");
    public static final ResourceLocation FILM_OVERLAYS_TEXTURE = Exposure.resource("textures/gui/lightroom_film_overlays.png");
    public static final int FRAME_SIZE = 54;
    private Button printButton;

    public LightroomScreen(LightroomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        ExposureStorage.CLIENT.clear();
        ExposureClient.getExposureRenderer().clearData();
    }

    @Override
    protected void init() {
        imageWidth = 176;
        imageHeight = 210;
        super.init();
        inventoryLabelY = 116;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        printButton = new ImageButton(leftPos + 117, topPos + 89, 22, 22, 176, 17, 22, MAIN_TEXTURE, 256, 256,
                pButton -> {
                    if (Minecraft.getInstance().gameMode != null)
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.PRINT_BUTTON_ID);
                }, (button, poseStack, mouseX, mouseY) -> {
                    if (button.visible && button.isActive())
                        renderTooltip(poseStack, Component.translatable("gui.exposure.lightroom.print"), mouseX, mouseY);
                }, Component.empty());
        addRenderableWidget(printButton);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        printButton.active = canPressPrintButton();
        printButton.visible = !menu.isPrinting();

        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    private boolean canPressPrintButton() {
        return menu.getBlockEntity().canPrint();
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, MAIN_TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        blit(poseStack, leftPos - 27, topPos + 34, 0, 208, 28, 31);

        boolean colorFilm = menu.getSlot(LightroomBlockEntity.FILM_SLOT).getItem().getItem() instanceof DevelopedFilmItem developedFilmItem &&
                developedFilmItem.getType() == FilmType.COLOR;

        // PLACEHOLDER ICONS
        if (!menu.slots.get(LightroomBlockEntity.FILM_SLOT).hasItem())
            blit(poseStack, leftPos - 21, topPos + 41, 238, 0, 18, 18);
        if (!menu.slots.get(LightroomBlockEntity.CYAN_SLOT).hasItem())
            blit(poseStack, leftPos + 7, topPos + 91, 238, 36, 18, 18);
        if (!menu.slots.get(LightroomBlockEntity.MAGENTA_SLOT).hasItem())
            blit(poseStack, leftPos + 25, topPos + 91, 238, 54, 18, 18);
        if (!menu.slots.get(LightroomBlockEntity.YELLOW_SLOT).hasItem())
            blit(poseStack, leftPos + 43, topPos + 91, 238, 72, 18, 18);
        if (!menu.slots.get(LightroomBlockEntity.BLACK_SLOT).hasItem())
            blit(poseStack, leftPos + 61, topPos + 91, 238, 90, 18, 18);
        if (!menu.slots.get(LightroomBlockEntity.PAPER_SLOT).hasItem())
            blit(poseStack, leftPos + 95, topPos + 91, 238, 18, 18, 18);

        if (menu.isPrinting()) {
            int progress = menu.getData().get(LightroomBlockEntity.CONTAINER_DATA_PROGRESS_ID);
            int time = menu.getData().get(LightroomBlockEntity.CONTAINER_DATA_PRINT_TIME_ID);
            int width = progress != 0 && time != 0 ? progress * 24 / time : 0;
            blit(poseStack, leftPos + 116, topPos + 91, 176, 0, width + 1, 17);
        }

        List<FrameData> frames = menu.getExposedFrames();

        if (frames.size() == 0) {
            RenderSystem.setShaderTexture(0, FILM_OVERLAYS_TEXTURE);
            blit(poseStack, leftPos + 4, topPos + 15, 0, 136, 168, 68);
            return;
        }

        int currentFrame = menu.getCurrentFrame();
        @Nullable FrameData leftFrame = currentFrame - 1 >= 0 && currentFrame - 1 < frames.size() ? frames.get(currentFrame - 1) : null;
        @Nullable FrameData centerFrame = currentFrame >= 0 && currentFrame < frames.size() ? frames.get(currentFrame) : null;
        @Nullable FrameData rightFrame = currentFrame + 1 >= 0 && currentFrame + 1 < frames.size() ? frames.get(currentFrame + 1) : null;

        RenderSystem.setShaderTexture(0, FILM_OVERLAYS_TEXTURE);

        if (colorFilm)
            RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 1.0F);
        // Left film part
        blit(poseStack, leftPos + 1, topPos + 15, 0, leftFrame != null ? 68 : 0, 54, 68);
        // Center film part
        blit(poseStack, leftPos + 55, topPos + 15, 55, rightFrame != null ? 0 : 68, 64, 68);
        // Right film part
        if (rightFrame != null) {
            boolean hasMoreFrames = currentFrame + 2 < frames.size();
            blit(poseStack, leftPos + 119, topPos + 15, 120, hasMoreFrames ? 68 : 0, 56, 68);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        renderFrame(leftFrame, poseStack, leftPos + 6, topPos + 22, isOverLeftFrame(mouseX, mouseY) ? 0.8f : 0.25f, colorFilm);
        renderFrame(centerFrame, poseStack, leftPos + 61, topPos + 22, 0.9f, colorFilm);
        renderFrame(rightFrame, poseStack, leftPos + 116, topPos + 22, isOverRightFrame(mouseX, mouseY) ? 0.8f : 0.25f, colorFilm);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack poseStack, int x, int y) {
        super.renderTooltip(poseStack, x, y);

        if (isOverLeftFrame(x, y)) {
            renderTooltip(poseStack, Component.translatable("gui.exposure.lightroom.previous_frame"), x, y);
        }
        else if (isOverCenterFrame(x, y)) {
            renderTooltip(poseStack, Component.translatable("gui.exposure.lightroom.current_frame",
                    Integer.toString(menu.getCurrentFrame() + 1)), x, y);
        }
        else if (isOverRightFrame(x, y)) {
            renderTooltip(poseStack, Component.translatable("gui.exposure.lightroom.next_frame"), x, y);
        }
    }

    private boolean isOverLeftFrame(int mouseX, int mouseY) {
        List<FrameData> frames = menu.getExposedFrames();
        int currentFrame = menu.getCurrentFrame();
        return currentFrame - 1 >= 0 && currentFrame - 1 < frames.size() && isHovering(6, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private boolean isOverCenterFrame(int mouseX, int mouseY) {
        List<FrameData> frames = menu.getExposedFrames();
        int currentFrame = menu.getCurrentFrame();
        return currentFrame >= 0 && currentFrame < frames.size() && isHovering(61, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private boolean isOverRightFrame(int mouseX, int mouseY) {
        List<FrameData> frames = menu.getExposedFrames();
        int currentFrame = menu.getCurrentFrame();
        return currentFrame + 1 >= 0 && currentFrame + 1 < frames.size() && isHovering(116, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private void renderFrame(@Nullable FrameData frame, PoseStack poseStack, float x, float y, float alpha, boolean colorFilm) {
        if (frame == null)
            return;

        Exposure.getStorage().getOrQuery(frame.id).ifPresent(exposureData -> {
            poseStack.pushPose();
            poseStack.translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(frame.id, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 180, 130, 110,
                        Mth.clamp((int)Math.ceil(alpha * 255), 0, 255));
            else
                ExposureClient.getExposureRenderer().renderNegative(frame.id, exposureData, true, poseStack,
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 255, 255, 255,
                        Mth.clamp((int)Math.ceil(alpha * 255), 0, 255));

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
        Preconditions.checkState(minecraft.gameMode != null);

        if (minecraft.options.keyLeft.matches(keyCode, scanCode)|| keyCode == InputConstants.KEY_LEFT) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.PREVIOUS_FRAME_BUTTON_ID);
            handled = true;
        } else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.NEXT_FRAME_BUTTON_ID);
            handled = true;
        }

        return handled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Preconditions.checkState(minecraft != null);
            Preconditions.checkState(minecraft.gameMode != null);

            if (isOverLeftFrame((int) mouseX, (int) mouseY)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.PREVIOUS_FRAME_BUTTON_ID);
                return true;
            }

            if (isOverRightFrame((int) mouseX, (int) mouseY)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LightroomMenu.NEXT_FRAME_BUTTON_ID);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
