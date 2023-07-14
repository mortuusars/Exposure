package io.github.mortuusars.exposure.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemAndStack<T extends Item> {
    private final T item;
    private final ItemStack stack;

    @SuppressWarnings("unchecked")
    public ItemAndStack(ItemStack stack) {
        this.stack = stack;
        this.item = (T) stack.getItem();
    }

    public T getItem() {
        return item;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public String toString() {
        return "ItemAndStack{" +
                "stack=" + stack +
                '}';
    }
}
