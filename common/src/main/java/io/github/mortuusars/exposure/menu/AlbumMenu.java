package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumMenu extends AbstractContainerMenu {
    public static final int MAX_PHOTOGRAPH_SLOTS = 6;
    private final List<AlbumItem.Page> pages;
    private final boolean editing;
    private final DataSlot currentSpreadIndex = DataSlot.standalone();

    private final List<DataSlot> photographSlots = new ArrayList<>();

//    public class AlbumContainerData implements ContainerData {
//        public static final int PROPERTIES_COUNT = 1;
//        public static final int INDEX_CURRENT_SPREAD = 0;
//
//        @Override
//        public int get(int slotIndex) {
//            if (slotIndex == INDEX_CURRENT_SPREAD) return AlbumMenu.this.currentSpreadIndex;
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

    public AlbumMenu(int containerId, Inventory playerInventory, ItemAndStack<AlbumItem> album) {
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

        for (int i = 0; i < MAX_PHOTOGRAPH_SLOTS; i++) {
            DataSlot slot = DataSlot.standalone();
            slot.set(-1);
            photographSlots.add(slot);
            this.addDataSlot(slot);
        }

        this.addDataSlot(currentSpreadIndex);



        /* give Dev exposure:album{Pages:[
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

    public int getCurrentSpreadIndex() {
        return this.currentSpreadIndex.get();
    }

    public void setCurrentSpreadIndex(int index) {
        this.currentSpreadIndex.set(index);
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
        return new AlbumMenu(containerId, inventory, new ItemAndStack<>(buffer.readItem()));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
    }
}
