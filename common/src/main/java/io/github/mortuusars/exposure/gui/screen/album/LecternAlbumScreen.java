package io.github.mortuusars.exposure.gui.screen.album;

import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.menu.LecternAlbumMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class LecternAlbumScreen extends AlbumScreen {
    private final LecternAlbumMenu menu;

    private final ContainerListener listener = new ContainerListener() {
        public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) { }

        public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
            if (dataSlotIndex == 1) {
                pager.setPage(value);
            }
        }
    };

    public LecternAlbumScreen(AlbumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.menu = (LecternAlbumMenu) menu;
        menu.addSlotListener(listener);
    }

    @Override
    protected void init() {
        super.init();

        if (player.mayBuild()) {
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                    .bounds(this.width / 2 - 100, topPos + 196, 98, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"), b -> sendButtonClick(LecternAlbumMenu.TAKE_BOOK_BUTTON))
                    .bounds(this.width / 2 + 2, topPos + 196, 98, 20).build());
        }
    }

    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }
}
