package io.github.mortuusars.exposure.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.block.entity.Lightroom;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.gui.screen.element.ChromaticProcessToggleButton;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.render.image.RenderedImageProvider;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.render.modifiers.IPixelModifier;
import io.github.mortuusars.exposure.util.ColorChannel;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LightroomScreen extends AbstractContainerScreen<LightroomMenu> {
    public static final ResourceLocation MAIN_TEXTURE = Exposure.resource("textures/gui/lightroom.png");
    public static final ResourceLocation FILM_OVERLAYS_TEXTURE = Exposure.resource("textures/gui/lightroom_film_overlays.png");
    public static final int FRAME_SIZE = 54;

    protected Player player;
    protected Button printButton;
    protected ChromaticProcessToggleButton processToggleButton;

    protected Map<Integer, Rect2i> slotPlaceholders = Collections.emptyMap();

    public LightroomScreen(LightroomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
    }

    @Override
    protected void init() {
        imageWidth = 176;
        imageHeight = 210;
        super.init();
        inventoryLabelY = 116;

        slotPlaceholders = Map.of(
                Lightroom.FILM_SLOT, new Rect2i(238, 0, 18, 18),
                Lightroom.PAPER_SLOT, new Rect2i(238, 18, 18, 18),
                Lightroom.CYAN_SLOT, new Rect2i(238, 36, 18, 18),
                Lightroom.MAGENTA_SLOT, new Rect2i(238, 54, 18, 18),
                Lightroom.YELLOW_SLOT, new Rect2i(238, 72, 18, 18),
                Lightroom.BLACK_SLOT, new Rect2i(238, 90, 18, 18)
        );

        printButton = new ImageButton(leftPos + 117, topPos + 89, 22, 22, 176, 17,
                22, MAIN_TEXTURE, 256, 256, this::onPrintButtonPressed,
                Component.translatable("gui.exposure.lightroom.print"));

        MutableComponent tooltip = Component.translatable("gui.exposure.lightroom.print");
        if (player.isCreative()) {
            tooltip.append("\n")
                    .append(Component.translatable("gui.exposure.lightroom.print.creative_tooltip"));
        }

        printButton.setTooltip(Tooltip.create(tooltip));
        addRenderableWidget(printButton);

        processToggleButton = new ChromaticProcessToggleButton(leftPos - 17, topPos + 91,
                this::onProcessToggleButtonPressed, () -> getMenu().getBlockEntity().getProcess());
        processToggleButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.lightroom.current_frame")));
        addRenderableWidget(processToggleButton);

        updateButtons();
    }

    protected void onPrintButtonPressed(Button button) {
        if (Minecraft.getInstance().gameMode != null) {
            if (Screen.hasShiftDown() && player.isCreative())
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, LightroomMenu.PRINT_CREATIVE_BUTTON_ID);
            else
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, LightroomMenu.PRINT_BUTTON_ID);
        }
    }

    protected void onProcessToggleButtonPressed(Button button) {
        if (Minecraft.getInstance().gameMode != null)
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, LightroomMenu.TOGGLE_PROCESS_BUTTON_ID);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateButtons();

        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void updateButtons() {
        printButton.active = getMenu().getBlockEntity().canPrint() || (player.isCreative() && Screen.hasShiftDown() && getMenu().getBlockEntity().canPrintInCreativeMode());
        printButton.visible = !getMenu().isPrinting();

        processToggleButton.active = true;
        processToggleButton.visible = getMenu().canChangeProcess();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(MAIN_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiGraphics.blit(MAIN_TEXTURE, leftPos - 27, topPos + 35, 0, 209, 28, 31);

        renderSlotPlaceholders(guiGraphics, mouseX, mouseY, partialTick);

        if (getMenu().isPrinting()) {
            int progress = getMenu().getData().get(LightroomBlockEntity.CONTAINER_DATA_PROGRESS_ID);
            int time = getMenu().getData().get(LightroomBlockEntity.CONTAINER_DATA_PRINT_TIME_ID);
            int width = progress != 0 && time != 0 ? progress * 24 / time : 0;
            guiGraphics.blit(MAIN_TEXTURE, leftPos + 116, topPos + 91, 176, 0, width, 17);
        }

        ListTag frames = getMenu().getExposedFrames();
        if (frames.isEmpty()) {
            guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 4, topPos + 15, 0, 136, 168, 68);
            return;
        }

        ItemStack filmStack = getMenu().getSlot(Lightroom.FILM_SLOT).getItem();
        if (!(filmStack.getItem() instanceof DevelopedFilmItem film))
            return;

        FilmType negative = film.getType();

        int selectedFrame = getMenu().getSelectedFrame();
        @Nullable CompoundTag leftFrame = getMenu().getFrameIdByIndex(selectedFrame - 1);
        @Nullable CompoundTag centerFrame = getMenu().getFrameIdByIndex(selectedFrame);
        @Nullable CompoundTag rightFrame = getMenu().getFrameIdByIndex(selectedFrame + 1);

        RenderSystem.setShaderColor(negative.filmR, negative.filmG, negative.filmB, negative.filmA);

        // Left film part
        guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 1, topPos + 15, 0, leftFrame != null ? 68 : 0, 54, 68);
        // Center film part
        guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 55, topPos + 15, 55, rightFrame != null ? 0 : 68, 64, 68);
        // Right film part
        if (rightFrame != null) {
            boolean hasMoreFrames = selectedFrame + 2 < frames.size();
            guiGraphics.blit(FILM_OVERLAYS_TEXTURE, leftPos + 119, topPos + 15, 120, hasMoreFrames ? 68 : 0, 56, 68);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack poseStack = guiGraphics.pose();

        if (leftFrame != null)
            renderFrame(leftFrame, poseStack, leftPos + 6, topPos + 22, FRAME_SIZE, isOverLeftFrame(mouseX, mouseY) ? 0.8f : 0.25f, negative);
        if (centerFrame != null)
            renderFrame(centerFrame, poseStack, leftPos + 61, topPos + 22, FRAME_SIZE, 0.9f, negative);
        if (rightFrame != null)
            renderFrame(rightFrame, poseStack, leftPos + 116, topPos + 22, FRAME_SIZE, isOverRightFrame(mouseX, mouseY) ? 0.8f : 0.25f, negative);

        RenderSystem.setShaderColor(negative.filmR, negative.filmG, negative.filmB, negative.filmA);

        if (getMenu().getBlockEntity().isAdvancingFrameOnPrint()) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 800);

            if (selectedFrame < getMenu().getTotalFrames() - 1)
                guiGraphics.blit(MAIN_TEXTURE, leftPos + 111, topPos + 44, 200, 0, 10, 10);
            else
                guiGraphics.blit(MAIN_TEXTURE, leftPos + 111, topPos + 44, 210, 0, 10, 10);

            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlotPlaceholders(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (int slotIndex : slotPlaceholders.keySet()) {
            Slot slot = getMenu().getSlot(slotIndex);
            if (!slot.hasItem()) {
                Rect2i placeholder = slotPlaceholders.get(slotIndex);
                guiGraphics.blit(MAIN_TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1,
                        placeholder.getX(), placeholder.getY(), placeholder.getWidth(), placeholder.getHeight());
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        boolean advancedTooltips = Minecraft.getInstance().options.advancedItemTooltips;
        int selectedFrame = getMenu().getSelectedFrame();
        List<Component> tooltipLines = new ArrayList<>();

        int hoveredFrameIndex = -1;

        if (isOverLeftFrame(mouseX, mouseY)) {
            hoveredFrameIndex = selectedFrame - 1;
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.previous_frame"));
        } else if (isOverCenterFrame(mouseX, mouseY)) {
            hoveredFrameIndex = selectedFrame;
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.current_frame", Integer.toString(getMenu().getSelectedFrame() + 1)));
        } else if (isOverRightFrame(mouseX, mouseY)) {
            hoveredFrameIndex = selectedFrame + 1;
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.next_frame"));
        }

        if (hoveredFrameIndex != -1) {
            addFrameInfoTooltipLines(tooltipLines, hoveredFrameIndex, advancedTooltips);
        }

        if (isOverCenterFrame(mouseX, mouseY)) {
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.zoom_in.tooltip"));
        }

        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipLines, Optional.empty(), mouseX, mouseY);
    }

    private void addFrameInfoTooltipLines(List<Component> tooltipLines, int frameIndex, boolean isAdvancedTooltips) {
        @Nullable CompoundTag frame = getMenu().getFrameIdByIndex(frameIndex);
        if (frame != null) {
            ColorChannel.fromString(frame.getString(FrameData.CHROMATIC_CHANNEL)).ifPresent(c ->
                    tooltipLines.add(Component.translatable("gui.exposure.channel." + c.getSerializedName())
                        .withStyle(Style.EMPTY.withColor(c.getRepresentationColor()))));

            if (isAdvancedTooltips) {
                Either<String, ResourceLocation> idOrTexture = FrameData.getIdOrTexture(frame);
                MutableComponent component = idOrTexture.map(
                                id -> !id.isEmpty() ? Component.translatable("gui.exposure.frame.id",
                                        Component.literal(id).withStyle(ChatFormatting.GRAY)) : Component.empty(),
                                texture -> Component.translatable("gui.exposure.frame.texture",
                                        Component.literal(texture.toString()).withStyle(ChatFormatting.GRAY)))
                        .withStyle(ChatFormatting.DARK_GRAY);
                tooltipLines.add(component);
            }
        }
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

    public void renderFrame(@Nullable CompoundTag frame, PoseStack poseStack, float x, float y, float size, float alpha, FilmType negative) {
        if (frame == null)
            return;

        poseStack.pushPose();
        poseStack.translate(x, y, 0);

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        IPixelModifier modifier = switch (negative) {
            case COLOR_POSITIVE -> ExposurePixelModifiers.EMPTY; //literally does nothing lmao
            default -> ExposurePixelModifiers.NEGATIVE_FILM;
        };

        ExposureClient.getExposureRenderer().render(RenderedImageProvider.fromFrame(frame),
                modifier,
                poseStack, bufferSource, 0, 0, size, size, LightTexture.FULL_BRIGHT,
                negative.frameR, negative.frameG, negative.frameB, Mth.clamp((int) Math.ceil(alpha * 255), 0, 255));
        bufferSource.endBatch();

        poseStack.popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.gameMode != null);

        if (minecraft.options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            changeFrame(PagingDirection.PREVIOUS);
            return true;
        } else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
            changeFrame(PagingDirection.NEXT);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
                changeFrame(PagingDirection.PREVIOUS);
                return true;
            }

            if (isOverRightFrame((int) mouseX, (int) mouseY)) {
                changeFrame(PagingDirection.NEXT);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void changeFrame(PagingDirection navigation) {
        if ((navigation == PagingDirection.PREVIOUS && getMenu().getSelectedFrame() == 0)
                || (navigation == PagingDirection.NEXT && getMenu().getSelectedFrame() == getMenu().getTotalFrames() - 1))
            return;

        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.player != null);
        Preconditions.checkState(minecraft.gameMode != null);
        int buttonId = navigation == PagingDirection.NEXT ? LightroomMenu.NEXT_FRAME_BUTTON_ID : LightroomMenu.PREVIOUS_FRAME_BUTTON_ID;
        minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, buttonId);
        player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, minecraft.player.level()
                .getRandom().nextFloat() * 0.4f + 0.8f);

        // Update block entity clientside to faster update advance frame arrows:
        getMenu().getBlockEntity().setSelectedFrame(getMenu().getBlockEntity().getSelectedFrameIndex() + (navigation == PagingDirection.NEXT ? 1 : -1));
    }

    private void enterFrameInspectMode() {
        Minecraft.getInstance().setScreen(new FilmFrameInspectScreen(this, getMenu()));
        player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 1.3f);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton)
                && hoveredSlot == null;
    }
}
