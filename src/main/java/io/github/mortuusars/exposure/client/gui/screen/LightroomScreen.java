package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import io.github.mortuusars.exposure.util.Navigation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        printButton = new ImageButton(leftPos + 117, topPos + 89, 22, 22, 176, 17,
                22, MAIN_TEXTURE, 256, 256, this::onPrintButtonPressed, Component.empty());
        printButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.lightroom.print")));
        addRenderableWidget(printButton);
    }

    private void onPrintButtonPressed(Button button) {
        if (Minecraft.getInstance().gameMode != null)
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, LightroomMenu.PRINT_BUTTON_ID);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        printButton.active = canPressPrintButton();
        printButton.visible = !getMenu().isPrinting();

        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean canPressPrintButton() {
        return getMenu().getBlockEntity().canPrint();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(MAIN_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiGraphics.blit(MAIN_TEXTURE, leftPos - 27, topPos + 34, 0, 208, 28, 31);


        // PLACEHOLDER ICONS
        if (!getMenu().slots.get(LightroomBlockEntity.FILM_SLOT).hasItem())
            guiGraphics.blit(MAIN_TEXTURE, leftPos - 21, topPos + 41, 238, 0, 18, 18);
        if (!getMenu().slots.get(LightroomBlockEntity.PAPER_SLOT).hasItem())
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 7, topPos + 91, 238, 18, 18, 18);
        if (!getMenu().slots.get(LightroomBlockEntity.CYAN_SLOT).hasItem())
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 41, topPos + 91, 238, 36, 18, 18);
        if (!getMenu().slots.get(LightroomBlockEntity.MAGENTA_SLOT).hasItem())
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 59, topPos + 91, 238, 54, 18, 18);
        if (!getMenu().slots.get(LightroomBlockEntity.YELLOW_SLOT).hasItem())
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 77, topPos + 91, 238, 72, 18, 18);
        if (!getMenu().slots.get(LightroomBlockEntity.BLACK_SLOT).hasItem())
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 95, topPos + 91, 238, 90, 18, 18);

        if (getMenu().isPrinting()) {
            int progress = getMenu().getData().get(LightroomBlockEntity.CONTAINER_DATA_PROGRESS_ID);
            int time = getMenu().getData().get(LightroomBlockEntity.CONTAINER_DATA_PRINT_TIME_ID);
            int width = progress != 0 && time != 0 ? progress * 24 / time : 0;
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 116, topPos + 91, 176, 0, width + 1, 17);
        }

        ListTag frames = getMenu().getExposedFrames();
        if (frames.size() == 0) {
            guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 4, topPos + 15, 0, 136, 168, 68);
            return;
        }

        int selectedFrame = getMenu().getSelectedFrame();
        String leftFrame = getMenu().getFrameIdByIndex(selectedFrame - 1);
        String centerFrame = getMenu().getFrameIdByIndex(selectedFrame);
        String rightFrame = getMenu().getFrameIdByIndex(selectedFrame + 1);

        boolean colorFilm = getMenu().isColorFilm();

        if (colorFilm)
            RenderSystem.setShaderColor(1.2F, 0.96F, 0.75F, 1.0F);

        // Left film part
        guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 1, topPos + 15, 0, leftFrame.length() > 0 ? 68 : 0, 54, 68);
        // Center film part
        guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 55, topPos + 15, 55, rightFrame.length() > 0 ? 0 : 68, 64, 68);
        // Right film part
        if (rightFrame.length() > 0) {
            boolean hasMoreFrames = selectedFrame + 2 < frames.size();
            guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 119, topPos + 15, 120, hasMoreFrames ? 68 : 0, 56, 68);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (leftFrame.length() > 0)
            renderFrame(leftFrame, guiGraphics, leftPos + 6, topPos + 22, isOverLeftFrame(mouseX, mouseY) ? 0.8f : 0.25f, colorFilm);
        if (centerFrame.length() > 0)
            renderFrame(centerFrame, guiGraphics, leftPos + 61, topPos + 22, 0.9f, colorFilm);
        if (rightFrame.length() > 0)
            renderFrame(rightFrame, guiGraphics, leftPos + 116, topPos + 22, isOverRightFrame(mouseX, mouseY) ? 0.8f : 0.25f, colorFilm);

        if (colorFilm)
            RenderSystem.setShaderColor(1.2F, 0.96F, 0.75F, 1.0F);

        if (getMenu().getBlockEntity().isAdvancingFrameOnPrint()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 800);

            if (selectedFrame < getMenu().getTotalFrames() - 1)
                guiGraphics.blit(MAIN_TEXTURE, leftPos + 111, topPos + 44, 200, 0, 10, 10);
            else
                guiGraphics.blit(MAIN_TEXTURE, leftPos + 111, topPos + 44, 210, 0, 10, 10);

            guiGraphics.pose().popPose();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        boolean advancedTooltips = Minecraft.getInstance().options.advancedItemTooltips;
        int selectedFrame = getMenu().getSelectedFrame();
        List<Component> tooltipLines = new ArrayList<>();

        if (isOverLeftFrame(mouseX, mouseY)) {
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.previous_frame"));
            if (advancedTooltips) {
                String id = getMenu().getFrameIdByIndex(selectedFrame - 1);
                tooltipLines.add(Component.literal("Exposure Id: " + id).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else if (isOverCenterFrame(mouseX, mouseY)) {
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.current_frame", Integer.toString(getMenu().getSelectedFrame() + 1)));
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.zoom_in.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            if (advancedTooltips) {
                String id = getMenu().getFrameIdByIndex(selectedFrame);
                tooltipLines.add(Component.literal("Exposure Id: " + id).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else if (isOverRightFrame(mouseX, mouseY)) {
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.next_frame"));
            if (advancedTooltips) {
                String id = getMenu().getFrameIdByIndex(selectedFrame - 1);
                tooltipLines.add(Component.literal("Exposure Id: " + id).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipLines, Optional.empty(), mouseX, mouseY);
    }

    private boolean isOverLeftFrame(int mouseX, int mouseY) {
        ListTag frames = getMenu().getExposedFrames();
        int selectedFrame = getMenu().getSelectedFrame();
        return selectedFrame - 1 >= 0 && selectedFrame - 1 < frames.size() && isHovering(6, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private boolean isOverCenterFrame(int mouseX, int mouseY) {
        ListTag frames = getMenu().getExposedFrames();
        int selectedFrame = getMenu().getSelectedFrame();
        return selectedFrame >= 0 && selectedFrame < frames.size() && isHovering(61, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private boolean isOverRightFrame(int mouseX, int mouseY) {
        ListTag frames = getMenu().getExposedFrames();
        int selectedFrame = getMenu().getSelectedFrame();
        return selectedFrame + 1 >= 0 && selectedFrame + 1 < frames.size() && isHovering(116, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private void renderFrame(String exposureId, GuiGraphics guiGraphics, float x, float y, float alpha, boolean colorFilm) {
        if (exposureId.length() == 0)
            return;

        Exposure.getStorage().getOrQuery(exposureId).ifPresent(exposureData -> {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0);

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance()
                    .getBuilder());
            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, guiGraphics.pose(),
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 180, 130, 110,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));
            else
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, guiGraphics.pose(),
                        bufferSource, FRAME_SIZE, FRAME_SIZE, LightTexture.FULL_BRIGHT, 255, 255, 255,
                        Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));

            bufferSource.endBatch();

            guiGraphics.pose().popPose();
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.gameMode != null);

        if (minecraft.options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            changeFrame(Navigation.PREVIOUS);
            handled = true;
        } else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
            changeFrame(Navigation.NEXT);
            handled = true;
        }

        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            if (delta >= 0.0 && isOverCenterFrame((int) mouseX, (int) mouseY)) // Scroll Up
                enterFrameInspectMode();
        }

        return handled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Preconditions.checkState(minecraft != null);
            Preconditions.checkState(minecraft.gameMode != null);

            if (isOverCenterFrame((int) mouseX, (int) mouseY)) {
                enterFrameInspectMode();
                return true;
            }

            if (isOverLeftFrame((int) mouseX, (int) mouseY)) {
                changeFrame(Navigation.PREVIOUS);
                return true;
            }

            if (isOverRightFrame((int) mouseX, (int) mouseY)) {
                changeFrame(Navigation.NEXT);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void changeFrame(Navigation navigation) {
        if ((navigation == Navigation.PREVIOUS && getMenu().getSelectedFrame() == 0)
                || (navigation == Navigation.NEXT && getMenu().getSelectedFrame() == getMenu().getTotalFrames() - 1))
            return;

        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.player != null);
        Preconditions.checkState(minecraft.gameMode != null);
        int buttonId = navigation == Navigation.NEXT ? LightroomMenu.NEXT_FRAME_BUTTON_ID : LightroomMenu.PREVIOUS_FRAME_BUTTON_ID;
        minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, buttonId);
        minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, minecraft.player.level()
                .getRandom().nextFloat() * 0.4f + 0.8f);

        // Update block entity clientside to faster update advance frame arrows:
        getMenu().getBlockEntity().setSelectedFrame(getMenu().getBlockEntity().getSelectedFrame() + (navigation == Navigation.NEXT ? 1 : -1));
    }

    private void enterFrameInspectMode() {
        Minecraft.getInstance().setScreen(new FilmFrameInspectScreen(this, getMenu()));
        Objects.requireNonNull(Minecraft.getInstance().player)
                .playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 1.3f);
    }
}
