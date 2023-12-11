package io.github.mortuusars.exposure.menu;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.logging.Level;

public class CameraAttachmentsContainer extends SimpleContainer {
    public CameraAttachmentsContainer(int size) {
        super(size);
    }

    public CameraAttachmentsContainer(ItemStack... items) {
        super(items);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack previousItem = getItem(slot);
        super.setItem(slot, stack);
        itemInSlotChanged(slot, previousItem, stack);
    }

    public void itemInSlotChanged(int slot, ItemStack previousItemStack, ItemStack newItemStack) {

    }
}
