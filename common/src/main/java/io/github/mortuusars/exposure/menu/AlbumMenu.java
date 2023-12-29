package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumMenu extends AbstractContainerMenu {
    private final List<AlbumItem.Page> pages;
    private final boolean editing;

//    public static class AlbumContainerData implements ContainerData {
//        public static final int PROPERTIES_COUNT = 1;
//        public static final int CURRENT_PAGE_ID = 0;
//
//        @Override
//        public int get(int slotIndex) {
//            return 0;
//        }
//
//        @Override
//        public void set(int slotIndex, int value) {
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

        editing = true;

        List<AlbumItem.Page> albumPages = album.getItem().getPages(album.getStack());

        if (editing) {
            albumPages = new ArrayList<>(albumPages);
            while (albumPages.size() < 16) {
                albumPages.add(new AlbumItem.Page(ItemStack.EMPTY, Collections.emptyList()));
            }
        }

        pages = albumPages;

        /*
        give Dev exposure:album{Pages:[
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"view1"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_30717"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_28958"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Texture:"exposure:textures/block/flash.png"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_1043"}}},
            {Photo:{}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_6323"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Texture:"exposure:textures/block/lightroom_side.png"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_1043"}}},
        ]}
         */
    }

    public List<AlbumItem.Page> getPages() {
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
