package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.AlbumSyncNoteC2SP;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.inventory.AlbumMenu;
import io.github.mortuusars.exposure.world.inventory.slot.AlbumPlayerInventorySlot;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class AlbumScreen extends AbstractContainerScreen<AlbumMenu> {
    public static final WidgetSprites SIGN_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("album/sign"), Exposure.resource("album/sign_disabled"), Exposure.resource("album/sign_highlighted"));

    protected final Pager pager = new Pager()
            .setChangeSound(new SoundEffect(() -> SoundEvents.BOOK_PAGE_TURN))
            .onPageChanged((oldPage, newPage) -> clickButton(PagingDirection.fromChange(oldPage, newPage).ordinal()));

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final List<Page> pages = new ArrayList<>();

    @Nullable
    protected Button enterSignModeButton;

    public AlbumScreen(AlbumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = 298;
        this.imageHeight = 188;
        super.init();

        titleLabelY = -999;
        inventoryLabelX = 69;
        inventoryLabelY = -999; // Inventory label will be moved into position when inventory is shown

        pages.clear();

        // LEFT:
        Page leftPage = createPage(Side.LEFT, 0);
        pages.add(leftPage);

        ImageButton previousPageButton = new ImageButton(leftPos + 12, topPos + 164, 13, 15,
                AlbumGUI.PREVIOUS_PAGE_BUTTON_SPRITES, button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        previousPageButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.previous_page")));
        addRenderableWidget(previousPageButton);

        // RIGHT:
        Page rightPage = createPage(Side.RIGHT, 140);
        pages.add(rightPage);

        ImageButton nextPageButton = new ImageButton(leftPos + 273, topPos + 164, 13, 15,
                AlbumGUI.NEXT_PAGE_BUTTON_SPRITES, button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        nextPageButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.next_page")));
        addRenderableWidget(nextPageButton);

        // MISC:
        if (getMenu().isAlbumEditable()) {
            enterSignModeButton = new ImageButton(leftPos - 23, topPos + 17, 22, 22,
                    SIGN_BUTTON_SPRITES, b -> enterSignMode(), Component.translatable("gui.exposure.album.sign"));
            enterSignModeButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.sign")));
            addRenderableWidget(enterSignModeButton);
        }

        int spreadsCount = (int) Math.ceil(getMenu().getPages().size() / 2f);
        pager.setPagesCount(spreadsCount)
                .setPreviousPageButton(previousPageButton)
                .setNextPageButton(nextPageButton);
    }

    @Override
    protected void containerTick() {
        forEachPage(page -> page.noteWidget.ifLeft(TextBox::tick));
    }

    protected Page createPage(Side side, int xOffset) {
        int x = leftPos + xOffset;
        int y = topPos;

        Rect2i page = new Rect2i(x, y, 149, 188);
        Rect2i photo = new Rect2i(x + 25, y + 21, 108, 108);
        Rect2i note = new Rect2i(x + 22, y + 133, 114, 27);

        PhotographSlotWidget photographWidget = new PhotographSlotWidget(this, photo.getX(), photo.getY(),
                photo.getWidth(), photo.getHeight(), () -> getMenu().getPhotograph(side)) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return !isInAddingMode() && super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean isHovered() {
                return !isInAddingMode() && super.isHovered();
            }
        };

        photographWidget
                .editable(getMenu().isAlbumEditable())
                .primaryAction(widget -> {
                    if (!widget.inspectPhotograph() && widget.getPhotograph().isEmpty() && widget.isEditable()) {
                        clickButton(side == Side.LEFT ? AlbumMenu.LEFT_PAGE_PHOTO_BUTTON : AlbumMenu.RIGHT_PAGE_PHOTO_BUTTON);
                        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(
                                SoundEvents.UI_BUTTON_CLICK, 1f));
                    }
                })
                .secondaryAction(widget -> {
                    if (widget.isEditable() && !widget.getPhotograph().isEmpty()) {
                        clickButton(side == Side.LEFT ? AlbumMenu.LEFT_PAGE_PHOTO_BUTTON : AlbumMenu.RIGHT_PAGE_PHOTO_BUTTON);
                        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(
                                Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.7f, 1.1f));
                    }
                });

        addRenderableWidget(photographWidget);

        Either<TextBox, TextBlock> noteWidget;
        if (getMenu().isAlbumEditable()) {
            TextBox textBox = new TextBox(font, note.getX(), note.getY(), note.getWidth(), note.getHeight(),
                    () -> getMenu().getPage(side).map(AlbumPage::note).orElse(""),
                    text -> onNoteChanged(side, text))
                    .setFontColor(Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR))
                    .setSelectionColor(
                            Config.getColor(Config.Client.ALBUM_SELECTION_COLOR),
                            Config.getColor(Config.Client.ALBUM_SELECTION_UNFOCUSED_COLOR));
            textBox.horizontalAlignment = HorizontalAlignment.CENTER;
            addRenderableWidget(textBox);
            noteWidget = Either.left(textBox);
        } else {
            TextBlock textBlock = new TextBlock(font, note.getX(), note.getY(),
                    note.getWidth(), note.getHeight(), getNoteComponent(side), this::handleComponentClicked);
            textBlock.fontColor = Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR);
            textBlock.alignment = HorizontalAlignment.CENTER;
            textBlock.drawShadow = false;

            //  TextBlock is rendered manually to not be a part of TAB navigation.
            //  addRenderableWidget(textBlock);

            noteWidget = Either.right(textBlock);
        }

        return new Page(side, page, photo, note, photographWidget, noteWidget);
    }

    protected void onNoteChanged(Side side, String noteText) {
        int pageIndex = getMenu().getCurrentSpreadIndex() * 2 + side.getIndex();
        getMenu().updatePage(pageIndex, page -> page.setNote(noteText));
        Packets.sendToServer(new AlbumSyncNoteC2SP(pageIndex, noteText));
    }

    // RENDER

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateWidgetVisibility();

        inventoryLabelY = isInAddingMode() ? getMenu().getPlayerInventorySlots().getFirst().y - 12 : -999;

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        for (Page page : pages) {
            AbstractWidget noteWidget = page.getNoteWidget();
            if (noteWidget instanceof TextBlock textBlock) {
                textBlock.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        if (isInAddingMode()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (Slot slot : getMenu().slots) {
                if (!slot.getItem().isEmpty() && !(slot.getItem().getItem() instanceof PhotographItem)) {
                    guiGraphics.blit(AlbumGUI.TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 350, 176, 188,
                            18, 18, 512, 512);
                }
            }
            RenderSystem.disableBlend();
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void updateWidgetVisibility() {
        // Note should be hidden when adding photograph because it's drawn over the slots. Blit offset does not help.
        forEachPage(page -> page.getNoteWidget().visible = !isInAddingMode());

        for (Page page : pages) {
            page.photographWidget.visible = !getMenu().getPhotograph(page.side).isEmpty()
                    || (!isInAddingMode() && getMenu().isAlbumEditable());
        }

        if (enterSignModeButton != null) {
            enterSignModeButton.visible = getMenu().canSignAlbum();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);
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
        if (isInAddingMode() && hoveredSlot != null && !hoveredSlot.getItem()
                .isEmpty() && !(hoveredSlot.getItem().getItem() instanceof PhotographItem)) {
            return; // Do not render tooltips for greyed-out items
        }

        if (!isInAddingMode()) {
            for (Page page : pages) {
                if (page.photographWidget.isHoveredOrFocused()) {
                    page.photographWidget.renderTooltip(guiGraphics, x, y);
                    return;
                }

                if (getMenu().isAlbumEditable() && page.isMouseOver(page.noteArea, x, y)) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable("gui.exposure.album.note"));

                    if (!page.getNoteWidget().isFocused())
                        tooltip.add(Component.translatable("gui.exposure.album.left_click_to_edit"));

                    boolean hasText = page.noteWidget.left().map(box -> !box.getText().isEmpty()).orElse(false);
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
    public @NotNull List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltipLines = super.getTooltipFromContainerItem(stack);
        if (isInAddingMode() && hoveredSlot != null && hoveredSlot.getItem() == stack
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
        guiGraphics.blit(AlbumGUI.TEXTURE, leftPos, topPos, 0, 0, 0,
                imageWidth, imageHeight, 512, 512);

        if (enterSignModeButton != null && enterSignModeButton.visible) {
            guiGraphics.blit(AlbumGUI.TEXTURE, leftPos - 27, topPos + 14, 447, 0,
                    27, 28, 512, 512);
        }

        int currentSpreadIndex = getMenu().getCurrentSpreadIndex();
        drawPageNumbers(guiGraphics, currentSpreadIndex);

        if (isInAddingMode()) {
            AlbumPlayerInventorySlot firstSlot = getMenu().getPlayerInventorySlots().getFirst();
            int x = firstSlot.x - 8;
            int y = firstSlot.y - 18;
            guiGraphics.blit(AlbumGUI.TEXTURE, leftPos + x, topPos + y, 10, 0, 188, 176, 100, 512, 512);

            @Nullable Side pageBeingAddedTo = getMenu().getSideBeingAddedTo();
            for (Page page : pages) {
                if (page.side == pageBeingAddedTo) {
                    guiGraphics.blitSprite(PhotographSlotWidget.EMPTY_SPRITES.enabledFocused(),
                            page.photoArea.getX(), page.photoArea.getY(), page.photoArea.getWidth(), page.photoArea.getHeight());
                }
            }
        }
    }

    protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex) {
        Font font = Minecrft.get().font;

        String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
        String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);

        guiGraphics.drawString(font, leftPageNumber, leftPos + 71 + (8 - font.width(leftPageNumber) / 2),
                topPos + 167, Config.getColor(Config.Client.ALBUM_FONT_SECONDARY_COLOR), false);

        guiGraphics.drawString(font, rightPageNumber, leftPos + 212 + (8 - font.width(rightPageNumber) / 2),
                topPos + 167, Config.getColor(Config.Client.ALBUM_FONT_SECONDARY_COLOR), false);
    }


    // CONTROLS:

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInAddingMode()) {
            if (!isHoveringOverInventory(mouseX, mouseY)
                    && (!hasClickedOutside(mouseX, mouseY, leftPos, topPos, button) || getMenu().getCarried().isEmpty())) {
                clickButton(AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        for (Page page : pages) {
            if (getMenu().isAlbumEditable() && button == InputConstants.MOUSE_BUTTON_RIGHT && page.isMouseOver(page.noteArea, mouseX, mouseY)) {
                page.noteWidget.ifLeft(box -> {
                    box.setText(""); // Clear the note
                });
                return true;
            }
        }

        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        for (Page page : pages) {
            AbstractWidget noteWidget = page.getNoteWidget();
            if (noteWidget instanceof TextBlock textBlock && textBlock.mouseClicked(mouseX, mouseY, button)) {
                handled = true;
                break;
            }
        }

        for (Page page : pages) {
            if (page.getNoteWidget().isFocused() && !page.isMouseOver(page.noteArea, mouseX, mouseY)) {
                setFocused(null);
                return true;
            }
        }

        if (!(getFocused() instanceof TextBox)) {
            setFocused(null); // Clear focus on mouse click because it's annoying. But keep on textbox to type.
        }

        return handled;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isQuickCrafting && !getMenu().getCarried().isEmpty() && getMenu().getCarried().getCount() == 1) {
            isQuickCrafting = false; // Fixes weird issue with carried item not placing when dragging slightly
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        if (style == null)
            return false;

        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null)
            return false;
        else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String pageIndexStr = clickEvent.getValue();
            int pageIndex = Integer.parseInt(pageIndexStr) - 1;
            forcePage(pageIndex);
            return true;
        }

        boolean handled = super.handleComponentClicked(style);
        if (handled && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND)
            onClose();
        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isInAddingMode())
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        else
            return this.getFocused() != null && this.isDragging() && button == 0
                    && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    protected void clickButton(int buttonId) {
        getMenu().clickMenuButton(Minecrft.playerOrThrow(), buttonId);
        Minecrft.gameModeOrThrow().handleInventoryButtonClick(getMenu().containerId, buttonId);

        if (buttonId == AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON) {
            setFocused(null);
        }

        if (buttonId == AlbumMenu.PREVIOUS_PAGE_BUTTON || buttonId == AlbumMenu.NEXT_PAGE_BUTTON) {
            for (Page page : pages) {
                page.noteWidget
                        .ifLeft(TextBox::setCursorToEnd)
                        .ifRight(textBlock -> textBlock.setMessage(getNoteComponent(page.side)));
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isHoveringOverInventory(double mouseX, double mouseY) {
        if (!isInAddingMode()) {
            return false;
        }

        AlbumPlayerInventorySlot firstSlot = getMenu().getPlayerInventorySlots().getFirst();
        int x = firstSlot.x - 8;
        int y = firstSlot.y - 18;
        return isHovering(x, y, 176, 100, mouseX, mouseY);
    }

    protected boolean isHoveringOverSignElement(double mouseX, double mouseY) {
        return enterSignModeButton != null
                && enterSignModeButton.visible
                && isHovering(leftPos - 27, topPos + 14, 27, 28, mouseX, mouseY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton)
                && !isHoveringOverInventory(mouseX, mouseY)
                && !isHoveringOverSignElement(mouseX, mouseY);
    }

    @SuppressWarnings("UnusedReturnValue")
    protected boolean forcePage(int pageIndex) {
        try {
            int newSpreadIndex = pageIndex / 2;

            if (newSpreadIndex == getMenu().getCurrentSpreadIndex() || newSpreadIndex < 0
                    || newSpreadIndex > getMenu().getPages().size() / 2) {
                return false;
            }

            PagingDirection pagingDirection = newSpreadIndex < getMenu().getCurrentSpreadIndex()
                    ? PagingDirection.PREVIOUS : PagingDirection.NEXT;

            int pageChanges = 0; // Safeguard against infinite loop. Probably not needed. But I don't mind it.
            while (newSpreadIndex != getMenu().getCurrentSpreadIndex() || !pager.canChangePage(pagingDirection)) {
                if (pageChanges > 16) {
                    break;
                }

                pager.changePage(pagingDirection);
                pageChanges++;
            }
            return true;
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot force page: {}", e.toString());
        }
        return false;
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

                return widget.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        if (isInAddingMode() && (Minecrft.options().keyInventory.matches(keyCode, scanCode)
                || keyCode == InputConstants.KEY_ESCAPE)) {
            clickButton(AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON);
            return true;
        }

        return keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Page page : pages) {
            if (page.noteWidget.map(box -> box, block -> block).isFocused())
                return super.keyReleased(keyCode, scanCode, modifiers);
        }

        return keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }


    // MISC:

    @NotNull
    protected Component getNoteComponent(Side side) {
        return getMenu().getPage(side)
                .map(page -> Component.literal(page.note()))
                .orElse(Component.empty());
    }

    protected void enterSignMode() {
        if (isInAddingMode()) {
            clickButton(AlbumMenu.CANCEL_ADDING_PHOTO_BUTTON);
        }

        Minecrft.get().setScreen(new AlbumSigningScreen(this));
    }

    protected boolean isInAddingMode() {
        return getMenu().isInAddingPhotographMode();
    }

    protected void forEachPage(Consumer<Page> pageAction) {
        for (Page page : pages) {
            pageAction.accept(page);
        }
    }

    protected class Page {
        public final Side side;
        public final Rect2i pageArea;
        public final Rect2i photoArea;
        public final Rect2i noteArea;

        public final PhotographSlotWidget photographWidget;
        public final Either<TextBox, TextBlock> noteWidget;

        private Page(Side side, Rect2i pageArea, Rect2i photoArea, Rect2i noteArea,
                     PhotographSlotWidget photographWidget, Either<TextBox, TextBlock> noteWidget) {
            this.side = side;
            this.pageArea = pageArea;
            this.photoArea = photoArea;
            this.noteArea = noteArea;
            this.photographWidget = photographWidget;
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
