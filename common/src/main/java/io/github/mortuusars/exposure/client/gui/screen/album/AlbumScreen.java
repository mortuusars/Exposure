package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.menu.AlbumPlayerInventorySlot;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.AlbumSyncNoteC2SP;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AlbumScreen extends AbstractContainerScreen<AlbumMenu> {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/album.png");
    public static final int MAIN_FONT_COLOR = 0xFFB59774;
    public static final int SECONDARY_FONT_COLOR = 0xFFEFE4CA;
    public static final int SELECTION_COLOR = 0xFF8888FF;
    public static final int SELECTION_UNFOCUSED_COLOR = 0xFFBBBBFF;

    @NotNull
    private final Minecraft minecraft;
    @NotNull
    private final Player player;
    @NotNull
    private final MultiPlayerGameMode gameMode;

    private final Pager pager = new Pager(SoundEvents.BOOK_PAGE_TURN) {
        @Override
        public void onPageChanged(PagingDirection pagingDirection, int prevPage, int currentPage) {
            super.onPageChanged(pagingDirection, prevPage, currentPage);
            pressButton(pagingDirection == PagingDirection.PREVIOUS ? AlbumMenu.PREVIOUS_PAGE_BUTTON : AlbumMenu.NEXT_PAGE_BUTTON);
        }
    };

    private final List<Page> pages = new ArrayList<>();

    public AlbumScreen(AlbumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        minecraft = Minecraft.getInstance();
        player = Objects.requireNonNull(minecraft.player);
        gameMode = Objects.requireNonNull(minecraft.gameMode);
    }

    @Override
    protected void containerTick() {
        forEachPage(page -> page.noteWidget.ifLeft(TextBox::tick));
    }

    @Override
    protected void init() {
        this.imageWidth = 299;
        this.imageHeight = 188;
        super.init();

        titleLabelY = -999;
        inventoryLabelX = 69;
        inventoryLabelY = -999;

        pages.clear();

        MutableComponent addButtonTooltip = Component.translatable("gui.exposure.album.add_photograph");

        // LEFT:

        Rect2i lPageArea = new Rect2i(leftPos, topPos, 149, 188);
        Rect2i lPhotoArea = new Rect2i(leftPos + 25, topPos + 21, 108, 109);
        Rect2i lExposureArea = new Rect2i(leftPos + 31, topPos + 27, 96, 96);
        Rect2i lNoteArea = new Rect2i(leftPos + 22, topPos + 133, 114, 27);

        Button lAddPhotoButton = new ImageButton(lPhotoArea.getX(), lPhotoArea.getY(), lPhotoArea.getWidth(), lPhotoArea.getHeight(),
                299, 0, 109, TEXTURE, 512, 512, this::onButtonPress);
        lAddPhotoButton.setTooltip(Tooltip.create(addButtonTooltip));
        addRenderableWidget(lAddPhotoButton);

        Either<TextBox, TextBlock> lNoteWidget;
        if (getMenu().isAlbumEditable()) {
            TextBox textBox = new TextBox(font, lNoteArea.getX(), lNoteArea.getY(), lNoteArea.getWidth(), lNoteArea.getHeight(),
                    () -> getMenu().getPage(Side.LEFT).map(page -> page.getNote().left().orElseThrow()).orElse(""),
                    text -> onNoteChanged(Side.LEFT, text))
                    .setFontColor(MAIN_FONT_COLOR, MAIN_FONT_COLOR)
                    .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);
            textBox.horizontalAlignment = HorizontalAlignment.CENTER;
            addRenderableWidget(textBox);
            lNoteWidget = Either.left(textBox);
        } else {
            //TODO: MESSAGE
            TextBlock textBlock = new TextBlock(font, lNoteArea.getX(), lNoteArea.getY(),
                    lNoteArea.getWidth(), lNoteArea.getHeight(), Component.empty() /*message*/);
            textBlock.fontColor = MAIN_FONT_COLOR;
            textBlock.setTabOrderGroup(1);
            addRenderableWidget(textBlock);
            lNoteWidget = Either.right(textBlock);
        }

        ImageButton previousButton = new ImageButton(leftPos + 12, topPos + 164, 13, 15,
                149, 188, 15, TEXTURE, 512, 512,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        pages.add(new Page(Side.LEFT, lPageArea, lPhotoArea, lExposureArea, lNoteArea, lAddPhotoButton,
                AlbumMenu.LEFT_PAGE_PHOTO_BUTTON, lNoteWidget));


        // RIGHT:

        Rect2i rPageArea = new Rect2i(leftPos, topPos, 149, 188);
        Rect2i rPhotoArea = new Rect2i(leftPos + 166, topPos + 21, 108, 109);
        Rect2i rExposureArea = new Rect2i(leftPos + 172, topPos + 27, 96, 96);
        Rect2i rNoteArea = new Rect2i(leftPos + 163, topPos + 133, 114, 27);

        Button rAddPhotoButton = new ImageButton(rPhotoArea.getX(), rPhotoArea.getY(),
                rPhotoArea.getWidth(), rPhotoArea.getHeight(), 299, 0, 109,
                TEXTURE, 512, 512, this::onButtonPress);
        rAddPhotoButton.setTooltip(Tooltip.create(addButtonTooltip));
        addRenderableWidget(rAddPhotoButton);

        Either<TextBox, TextBlock> rNoteWidget;
        if (getMenu().isAlbumEditable()) {
            TextBox textBox = new TextBox(font, rNoteArea.getX(), rNoteArea.getY(), rNoteArea.getWidth(), rNoteArea.getHeight(),
                    () -> getMenu().getPage(Side.RIGHT).map(page -> page.getNote().left().orElseThrow()).orElse(""),
                    text -> onNoteChanged(Side.RIGHT, text))
                    .setFontColor(MAIN_FONT_COLOR, MAIN_FONT_COLOR)
                    .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);
            textBox.horizontalAlignment = HorizontalAlignment.CENTER;
            addRenderableWidget(textBox);
            rNoteWidget = Either.left(textBox);
        } else {
            //TODO: MESSAGE
            TextBlock textBlock = new TextBlock(font, rNoteArea.getX(), rNoteArea.getY(),
                    rNoteArea.getWidth(), rNoteArea.getHeight(), Component.empty() /*message*/);
            textBlock.fontColor = MAIN_FONT_COLOR;
            textBlock.setTabOrderGroup(4);
            addRenderableWidget(textBlock);
            rNoteWidget = Either.right(textBlock);
        }

        ImageButton nextButton = new ImageButton(leftPos + 274, topPos + 164, 13, 15,
                162, 188, 15, TEXTURE, 512, 512,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pages.add(new Page(Side.RIGHT, rPageArea, rPhotoArea, rExposureArea, rNoteArea, rAddPhotoButton,
                AlbumMenu.RIGHT_PAGE_PHOTO_BUTTON, rNoteWidget));

        int spreadsCount = (int) Math.ceil(getMenu().getPages().size() / 2f);
        pager.init(spreadsCount, false, previousButton, nextButton);

        forEachPage(page -> {
            page.addPhotoButton.visible = getMenu().isAlbumEditable();
            page.addPhotoButton.active = getMenu().isAlbumEditable();
        });
    }

    protected void onNoteChanged(Side side, String noteText) {
        getMenu().getPage(side).ifPresent(page -> {
            page.setNote(Either.left(noteText));
            int pageIndex = getMenu().getCurrentSpreadIndex() * 2 + side.getIndex();
            Packets.sendToServer(new AlbumSyncNoteC2SP(pageIndex, noteText));
        });
    }

    private void onButtonPress(Button button) {
        forEachPage(page -> {
            if (button == page.addPhotoButton)
                pressButton(page.photoButtonId);
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        pager.update();

        boolean isInAddingPhotographMode = getMenu().isInAddingPhotographMode();

        inventoryLabelY = isInAddingPhotographMode ? getMenu().getPlayerInventorySlots().get(0).y - 12 : -999;

        forEachPage(page -> {
            page.getNoteWidget().visible = !isInAddingPhotographMode;
            page.getNoteWidget().active = !isInAddingPhotographMode;
        });

        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isInAddingPhotographMode) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (Slot slot : getMenu().slots) {
                if (!slot.getItem().isEmpty() && !(slot.getItem().getItem() instanceof PhotographItem)) {
                    guiGraphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 350, 176, 376,
                            18, 18, 512, 512);
                }
            }
            RenderSystem.disableBlend();
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 15);
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (getMenu().isInAddingPhotographMode() && hoveredSlot != null && !hoveredSlot.getItem()
                .isEmpty() && !(hoveredSlot.getItem().getItem() instanceof PhotographItem))
            return; // Do not render tooltips for greyed-out items

        if (!getMenu().isInAddingPhotographMode()) {
            for (Page page : pages) {
                if (!page.addPhotoButton.visible && page.isMouseOver(page.photoArea, x, y)) {
                    getMenu().getPhotographSlot(page.side).ifPresent(slot -> {
                        ItemStack stack = slot.getItem();
                        List<Component> tooltip = this.getTooltipFromContainerItem(stack);
                        tooltip.add(Component.translatable("gui.exposure.album.left_click_to_view"));
                        tooltip.add(Component.translatable("gui.exposure.album.right_click_to_remove"));
                        guiGraphics.renderTooltip(this.font, tooltip,
                                (stack.getItem() instanceof PhotographItem ? Optional.empty() : stack.getTooltipImage()), x, y);
                    });

                    return;
                }

                if (getMenu().isAlbumEditable() && page.isMouseOver(page.noteArea, x, y)) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable("gui.exposure.album.note"));

                    if (!page.getNoteWidget().isFocused())
                        tooltip.add(Component.translatable("gui.exposure.album.left_click_to_edit"));

                    boolean hasText = page.noteWidget.left().map(box -> box.getText().length() > 0).orElse(false);
                    if (hasText)
                        tooltip.add(Component.translatable("gui.exposure.album.right_click_to_clear"));

                    guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), x, y);

                    return;
                }
            }
        }

        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltipLines = super.getTooltipFromContainerItem(stack);
        if (getMenu().isInAddingPhotographMode() && hoveredSlot != null && hoveredSlot.getItem() == stack
                && stack.getItem() instanceof PhotographItem) {
            tooltipLines.add(Component.empty());
            tooltipLines.add(Component.translatable("gui.exposure.album.left_click_to_add"));
        }
        return tooltipLines;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0,
                this.imageWidth, this.imageHeight, 512, 512);

        int currentSpreadIndex = getMenu().getCurrentSpreadIndex();
        drawPageNumbers(guiGraphics, currentSpreadIndex);

        for (Page page : pages) {
            getMenu().getPhotographSlot(page.side).ifPresent(slot -> {
                ItemStack photoStack = slot.getItem();

                page.addPhotoButton.visible = getMenu().isAlbumEditable() && !getMenu().isInAddingPhotographMode() && photoStack.isEmpty();
                page.addPhotoButton.active = getMenu().isAlbumEditable() && !getMenu().isInAddingPhotographMode() && photoStack.isEmpty();

                if (photoStack.getItem() instanceof PhotographItem photographItem) {
                    Rect2i area = page.photoArea;
                    guiGraphics.blit(TEXTURE, area.getX(), area.getY(), 0, 299, page.isMouseOver(page.photoArea, mouseX, mouseY) ? 327 : 218,
                            area.getWidth(), area.getHeight(), 512, 512);

                    @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(photoStack);
                    if (idOrTexture != null) {
                        Rect2i expArea = page.exposureArea;
                        ExposureClient.getExposureRenderer().renderSimple(idOrTexture, guiGraphics.pose(),
                                expArea.getX(), expArea.getY(), expArea.getWidth(), expArea.getHeight());
                    }
                }
            });
        }

        if (getMenu().isInAddingPhotographMode()) {
            @Nullable Side pageBeingAddedTo = getMenu().getSideBeingAddedTo();
            for (Page page : pages) {
                if (page.side == pageBeingAddedTo) {
                    guiGraphics.blit(TEXTURE, page.photoArea.getX(), page.photoArea.getY(), 10, 299, 109,
                            page.photoArea.getWidth(), page.photoArea.getHeight(), 512, 512);
                    break;
                }
            }

            AlbumPlayerInventorySlot firstSlot = getMenu().getPlayerInventorySlots().get(0);
            int x = firstSlot.x - 8;
            int y = firstSlot.y - 18;
            guiGraphics.blit(TEXTURE, leftPos + x, topPos + y, 10, 0, 376, 176, 100, 512, 512);
        }
    }

    protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex) {
        Font font = minecraft.font;

        String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
        String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);

        guiGraphics.drawString(font, leftPageNumber, leftPos + 71 + (8 - font.width(leftPageNumber) / 2),
                topPos + 167, SECONDARY_FONT_COLOR, false);

        guiGraphics.drawString(font, rightPageNumber, leftPos + 212 + (8 - font.width(rightPageNumber) / 2),
                topPos + 167, SECONDARY_FONT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!getMenu().isAlbumEditable())
            return super.mouseClicked(mouseX, mouseY, button);

        if (getMenu().isInAddingPhotographMode()) {
            AlbumPlayerInventorySlot firstSlot = getMenu().getPlayerInventorySlots().get(0);
            int x = firstSlot.x - 8;
            int y = firstSlot.y - 18;
            if (hoveredSlot == null) {
                if (isHovering(x, y, 188, 176, mouseX, mouseY))
                    return true;
                else if (!hasClickedOutside(mouseX, mouseY, leftPos, topPos, button)) {
                    pressButton(AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON);
                    return true;
                }
            }
        }
        else {
            for (Page page : pages) {
                if (button == InputConstants.MOUSE_BUTTON_RIGHT && page.isMouseOver(page.photoArea, mouseX, mouseY)) {
                    pressButton(page.photoButtonId);
                    return true;
                }

                if (button == InputConstants.MOUSE_BUTTON_RIGHT && page.isMouseOver(page.noteArea, mouseX, mouseY)) {
                    page.noteWidget.ifLeft(box -> {
                        box.setText(""); // Clears the note
                    });
                    return true;
                }

//                if (page.getNoteWidget().isFocused() && !page.isMouseOver(page.noteArea, mouseX, mouseY)) {
//                    setFocused(null);
//                    return true;
//                }
            }
        }

        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (!handled) {
            for (Page page : pages) {
                if (page.getNoteWidget().isFocused() && !page.isMouseOver(page.noteArea, mouseX, mouseY)) {
                    setFocused(null);
                    return true;
                }
            }
        }

        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (getMenu().isInAddingPhotographMode())
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        else
            return this.getFocused() != null && this.isDragging() && button == 0
                    && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void pressButton(int buttonId) {
        getMenu().clickMenuButton(player, buttonId);
        gameMode.handleInventoryButtonClick(getMenu().containerId, buttonId);

        if (buttonId == AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON)
            setFocused(null);

        if (buttonId == AlbumMenu.PREVIOUS_PAGE_BUTTON || buttonId == AlbumMenu.NEXT_PAGE_BUTTON) {
            for (Page page : pages) {
                page.noteWidget.ifLeft(TextBox::setCursorToEnd);
            }
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot == null && getMenu().isInAddingPhotographMode()) {
            pressButton(AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON);
            return;
        }

        super.slotClicked(slot, slotId, mouseButton, type);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB)
            return super.keyPressed(keyCode, scanCode, modifiers);

        for (Page page : pages) {
            AbstractWidget widget = page.noteWidget.map(box -> box, block -> block);
            if (widget.isFocused()) {
                if (keyCode == InputConstants.KEY_ESCAPE) {
                    this.setFocused(null);
                    return true;
                }

                if (keyCode == InputConstants.KEY_TAB) {
                    this.setFocused(null);
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }

                return widget.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        if (getMenu().isInAddingPhotographMode() && (minecraft.options.keyInventory.matches(keyCode, scanCode)
                || keyCode == InputConstants.KEY_ESCAPE)) {
            pressButton(AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON);
            return true;
        }

        return pager.handleKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Page page : pages) {
            if (page.noteWidget.map(box -> box, block -> block).isFocused())
                return super.keyReleased(keyCode, scanCode, modifiers);
        }

        return pager.handleKeyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void forEachPage(Consumer<Page> pageAction) {
        for (Page page : pages) {
            pageAction.accept(page);
        }
    }

    private class Page {
        public final Side side;
        public final Rect2i pageArea;
        public final Rect2i photoArea;
        public final Rect2i exposureArea;
        public final Button addPhotoButton;
        public final int photoButtonId;

        public final Rect2i noteArea;
        public final Either<TextBox, TextBlock> noteWidget;

        private Page(Side side, Rect2i pageArea, Rect2i photoArea, Rect2i exposureArea, Rect2i noteArea,
                     Button addPhotoButton, int photoButtonId, Either<TextBox, TextBlock> noteWidget) {
            this.side = side;
            this.pageArea = pageArea;
            this.photoArea = photoArea;
            this.exposureArea = exposureArea;
            this.photoButtonId = photoButtonId;
            this.addPhotoButton = addPhotoButton;
            this.noteArea = noteArea;
            this.noteWidget = noteWidget;
        }

        public boolean isMouseOver(Rect2i area, double mouseX, double mouseY) {
            return isHovering(area.getX() - leftPos, area.getY() - topPos,
                    area.getWidth(), area.getHeight(), mouseX, mouseY);
        }

        public AbstractWidget getNoteWidget() {
            return noteWidget.map(box -> box, block -> block);
        }
    }
}
