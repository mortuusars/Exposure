package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class LecternAlbumMenu extends AlbumMenu {
    public static final int TAKE_BOOK_BUTTON = 99;

    private final Container lectern;
    private final BlockPos lecternPos;
    private final Level level;

    public LecternAlbumMenu(int containerId, BlockPos lecternPos, Inventory playerInventory,
                            ItemAndStack<AlbumItem> album, Container lectern, ContainerData lecternData) {
        this(Exposure.MenuTypes.LECTERN_ALBUM.get(), containerId, lecternPos, playerInventory, album, lectern, lecternData);
    }

    protected LecternAlbumMenu(MenuType<? extends AbstractContainerMenu> type, int containerId, BlockPos lecternPos,
                               Inventory playerInventory, ItemAndStack<AlbumItem> album, Container lectern, ContainerData lecternData) {
        super(type, containerId, playerInventory, new ItemAndStack<>(album.getStack()), false);
        checkContainerSize(lectern, 1);
        checkContainerDataCount(lecternData, 1);
        this.lecternPos = lecternPos;
        this.lectern = lectern;
        this.level = playerInventory.player.level();
        this.addSlot(new Slot(lectern, 0, -999, -999) {
            @Override
            public void setChanged() {
                super.setChanged();
                LecternAlbumMenu.this.slotsChanged(this.container);
            }
        });
        this.addDataSlots(lecternData);
        setCurrentSpreadIndex(lecternData.get(0));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == TAKE_BOOK_BUTTON) {
            if (!player.mayBuild())
                return false;

            ItemStack albumStack = this.lectern.removeItemNoUpdate(0);
            this.lectern.setChanged();
            if (!player.getInventory().add(albumStack))
                player.drop(albumStack, false);

            return true;
        }

        return super.clickMenuButton(player, id);
    }

    @Override
    public void setCurrentSpreadIndex(int spreadIndex) {
        spreadIndex = Math.max(0, spreadIndex);
        super.setCurrentSpreadIndex(spreadIndex);
        setData(1, spreadIndex);
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockEntity(lecternPos) instanceof LecternBlockEntity lecternBlockEntity
                && lecternBlockEntity.getBook().getItem() instanceof AlbumItem
                && Container.stillValidBlockEntity(lecternBlockEntity, player);
    }

    public static LecternAlbumMenu fromBuffer(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new LecternAlbumMenu(containerId, buffer.readBlockPos(), inventory,
                new ItemAndStack<>(buffer.readItem()), new SimpleContainer(1), new SimpleContainerData(1));
    }
}
