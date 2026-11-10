package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.inventory.LecternAlbumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LecternAlbumScreen extends AlbumViewScreen implements MenuAccess<LecternAlbumMenu> {
    private final LecternAlbumMenu menu;

    private final ContainerListener listener = new ContainerListener() {
        public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
            LecternAlbumScreen.this.bookChanged();
        }

        public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
            if (dataSlotIndex == 0) {
                LecternAlbumScreen.this.pageChanged();
            }
        }
    };

    public LecternAlbumScreen(LecternAlbumMenu menu, Inventory playerInventory, Component title) {
        super(AlbumAccess.fromItem(menu.getBook()));
        this.menu = menu;
        this.pager.setChangeSound(null);
    }

    public @NotNull LecternAlbumMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void init() {
        super.init();

        if (Minecrft.playerOrThrow().mayBuild()) {
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                    .bounds(this.width / 2 - 100, topPos + 196, 98, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"),
                            b -> sendButtonClick(LecternAlbumMenu.BUTTON_TAKE_BOOK))
                    .bounds(this.width / 2 + 2, topPos + 196, 98, 20).build());
        }

        this.menu.addSlotListener(listener);
    }

    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }

    @Override
    protected void onSpreadChanged(int oldSpread, int newSpread) {
        super.onSpreadChanged(oldSpread, newSpread);
        sendButtonClick(LecternAlbumMenu.BUTTON_PAGE_JUMP_RANGE_START + newSpread * 2);
    }

    protected void pageChanged() {
        pager.changePage(menu.getPage() / 2);
    }

    protected void bookChanged() {
        ItemStack itemStack = this.menu.getBook();
        this.setAlbumAccess(AlbumAccess.fromItem(itemStack));
    }

    @Override
    protected void forcePage(int pageIndex) {
        sendButtonClick(LecternAlbumMenu.BUTTON_PAGE_JUMP_RANGE_START + pageIndex);
    }

    protected void sendButtonClick(int buttonId) {
        Minecrft.gameModeOrThrow().handleInventoryButtonClick(this.menu.containerId, buttonId);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int page = getMenu().getPage();
        if (page % 2 == 1 && isHovering(70, 167, 17, 7, mouseX, mouseY)) {
            sendButtonClick(LecternAlbumMenu.BUTTON_PAGE_JUMP_RANGE_START + page - 1);
        } else if (page % 2 == 0 && isHovering(210, 167, 17, 7, mouseX, mouseY)) {
            sendButtonClick(LecternAlbumMenu.BUTTON_PAGE_JUMP_RANGE_START + page + 1);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex, int mouseX, int mouseY) {
        super.drawPageNumbers(guiGraphics, currentSpreadIndex, mouseX, mouseY);

        int page = getMenu().getPage();
        String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
        String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);

        if (page % 2 == 1 && isHovering(70, 167, 17, 7, mouseX, mouseY)) {
            guiGraphics.drawString(font, leftPageNumber, leftPos + 71 + (8 - font.width(leftPageNumber) / 2),
                    topPos + 167, Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR), false);
        } else if (page % 2 == 0 && isHovering(210, 167, 17, 7, mouseX, mouseY)) {
            guiGraphics.drawString(font, rightPageNumber, leftPos + 212 + (8 - font.width(rightPageNumber) / 2),
                    topPos + 167, Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR), false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        int page = getMenu().getPage();

        if (page % 2 == 1 && isHovering(70, 167, 17, 7, x, y)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.exposure.album.lectern.set_current_page"), x, y);
        } else if (page % 2 == 0 && isHovering(210, 167, 17, 7, x, y)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.exposure.album.lectern.set_current_page"), x, y);
        }
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        mouseX -= this.leftPos;
        mouseY -= this.topPos;
        return mouseX >= (double)(x - 1) && mouseX < (double)(x + width + 1) && mouseY >= (double)(y - 1) && mouseY < (double)(y + height + 1);
    }
}