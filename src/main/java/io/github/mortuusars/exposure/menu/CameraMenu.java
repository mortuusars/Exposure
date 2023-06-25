package io.github.mortuusars.exposure.menu;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.menu.inventory.CameraItemStackHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CameraMenu extends AbstractContainerMenu {
    public boolean initialized = false;

    private final ItemStack cameraStack;
    private final int slotMatchingItem;

    public CameraMenu(int containerId, Inventory playerInventory, ItemStack cameraStack) {
        super(Exposure.MenuTypes.CAMERA.get(), containerId);
        this.cameraStack = cameraStack;
        this.slotMatchingItem = playerInventory.findSlotMatchingItem(cameraStack);

        IItemHandler itemStackHandler = new CameraItemStackHandler(playerInventory.player, cameraStack);

        addSlot(new SlotItemHandler(itemStackHandler, CameraItem.FILM, 35, 29));
        addSlot(new SlotItemHandler(itemStackHandler, CameraItem.LENS, 116, 57));
        addSlot(new SlotItemHandler(itemStackHandler, CameraItem.FILTER, 125, 14));

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
    public void initializeContents(int pStateId, List<ItemStack> pItems, ItemStack pCarried) {
        super.initializeContents(pStateId, pItems, pCarried);
        initialized = true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (slotIndex < CameraItem.SLOTS.size()) {
                if (!this.moveItemStackTo(slotStack, CameraItem.SLOTS.size(), this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else {
                for (int i = 0; i < CameraItem.SLOTS.size(); i++) {
                    Slot cameraSlot = this.slots.get(i);
                    if (cameraSlot.mayPlace(itemstack)) {
                        if (!this.moveItemStackTo(slotStack, i, i + 1, false))
                            return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public static CameraMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CameraMenu(containerId, playerInventory, buffer.readItem());
    }

    @Override
    public void slotsChanged(Container pContainer) {
        super.slotsChanged(pContainer);
    }


}
