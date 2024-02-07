package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilteredSlot extends Slot {
    private final Consumer<SlotChangedArgs> onItemChanged;
    private final int maxStackSize;
    private final int slot;
    private final Predicate<ItemStack> mayPlacePredicate;

    public FilteredSlot(Container container, int slot, int x, int y, int maxStackSize, Consumer<SlotChangedArgs> onItemChanged,
                        Predicate<ItemStack> mayPlacePredicate) {
        super(container, slot, x, y);
        Preconditions.checkArgument(maxStackSize > 0 && maxStackSize <= 64, maxStackSize + " is not valid. (1-64)");
        this.slot = slot;
        this.maxStackSize = maxStackSize;
        this.onItemChanged = onItemChanged;
        this.mayPlacePredicate = mayPlacePredicate;
    }

    public int getSlotId() {
        return slot;
    }

    @Override
    public int getMaxStackSize() {
        return Math.min(container.getMaxStackSize(), maxStackSize);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return mayPlacePredicate.test(stack);
    }

    @Override
    public void set(ItemStack stack) {
        ItemStack oldStack = getItem().copy();
        super.set(stack);
        onItemChanged.accept(new SlotChangedArgs(this, oldStack, getItem()));
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        ItemStack oldStack = getItem().copy();
        ItemStack removed = super.remove(amount);
        ItemStack newStack = getItem();
        onItemChanged.accept(new SlotChangedArgs(this, oldStack, newStack));
        return removed;
    }

    public record SlotChangedArgs(FilteredSlot slot, ItemStack oldStack, ItemStack newStack) {}
}
