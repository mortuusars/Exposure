package io.github.mortuusars.exposure.world.item;

import net.minecraft.world.item.ItemStack;

public interface AbstractAlbumItem {

    default boolean shouldPlayEquipAnimation(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

}
