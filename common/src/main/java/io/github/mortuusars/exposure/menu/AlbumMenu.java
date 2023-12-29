package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumMenu extends AbstractContainerMenu {
    private final List<AlbumItem.AlbumPage> pages;

//    public static class AlbumContainerData implements ContainerData {
//        public static final int PROPERTIES_COUNT = 1;
//        public static final int CURRENT_PAGE_ID = 0;
//
//        @Override
//        public int get(int index) {
//            return 0;
//        }
//
//        @Override
//        public void set(int index, int value) {
//
//        }
//
//        @Override
//        public int getCount() {
//            return PROPERTIES_COUNT;
//        }
//    }

    public AlbumMenu(int containerId, Inventory playerInventory, ItemAndStack<AlbumItem> album/*, ContainerData containerData*/) {
        super(Exposure.MenuTypes.ALBUM.get(), containerId);

        pages = album.getItem().getPages(album.getStack());
    }

    public List<AlbumItem.AlbumPage> getPages() {
        return pages;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() instanceof AlbumItem
                || player.getOffhandItem().getItem() instanceof AlbumItem;
    }

    public static AlbumMenu fromBuffer(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new AlbumMenu(containerId, inventory, new ItemAndStack<>(buffer.readItem())/*, new SimpleContainerData(1)*/);
    }
}
