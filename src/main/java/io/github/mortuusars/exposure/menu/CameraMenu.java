package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.menu.inventory.CameraItemStackHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CameraMenu extends AbstractContainerMenu {
    public static final int FILM_SLOT = 0;
    public static final int LENS_SLOT = 1;
    public static final int FILTER_SLOT = 2;

    public CameraMenu(int containerId, Inventory playerInventory, ItemStack cameraStack) {
        super(Exposure.MenuTypes.CAMERA.get(), containerId);

        IItemHandler itemStackHandler = new CameraItemStackHandler(playerInventory.player, cameraStack);

        addSlot(new SlotItemHandler(itemStackHandler, FILM_SLOT, 35, 29));
        addSlot(new SlotItemHandler(itemStackHandler, LENS_SLOT, 116, 57));
        addSlot(new SlotItemHandler(itemStackHandler, FILTER_SLOT, 125, 14));

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, (column + row * 9) + 9, column * 18 + 8, 84 + row * 18));
            }
        }

        //Hotbar
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(playerInventory, slot, slot * 18 + 8, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public static CameraMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CameraMenu(containerId, playerInventory, buffer.readItem());
    }
}
