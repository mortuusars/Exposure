package io.github.mortuusars.exposure.world.item.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * ItemStacks cannot be used in DataComponentType because when the stack is changed
 * - it can change in more than one place and cause unwanted side effects.
 * So this is an "immutable" wrapper around the ItemStack.
 * But because it's still possible to mutate the ItemStack stored in this class - care should be taken in how we use it.
 * {@link StoredItemStack#getForReading()} should be used when ItemStack is needed for read-only tasks such as checking item type, count, etc.
 * This avoids unnecessary copying of the stack.
 * {@link StoredItemStack#getCopy()} should be used when ItemStack will be changed in any way.
 */
public class StoredItemStack {
    public static final StoredItemStack EMPTY = new StoredItemStack(ItemStack.EMPTY);

    public static final Codec<StoredItemStack> CODEC = ItemStack.CODEC.xmap(StoredItemStack::new, StoredItemStack::getForReading);
    public static final StreamCodec<RegistryFriendlyByteBuf, StoredItemStack> STREAM_CODEC = ItemStack.STREAM_CODEC.map(
            StoredItemStack::new,
            StoredItemStack::getForReading
    );

    private final ItemStack stack;

    public StoredItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getForReading() {
        return stack;
    }

    public ItemStack getCopy() {
        return stack.copy();
    }

    public <T extends Item> ItemAndStack<T> getItemAndStackCopy() {
        return new ItemAndStack<>(getCopy());
    }

    public Item getItem() {
        return getForReading().getItem();
    }

    public boolean isEmpty() {
        return getForReading().isEmpty();
    }

    public <T, R> Optional<R> mapIf(Class<T> clazz, BiFunction<T, ItemStack, R> func) {
        if (getForReading().getItem().getClass().equals(clazz)) {
            return Optional.ofNullable(func.apply(clazz.cast(getForReading().getItem()), getForReading()));
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StoredItemStack other) {
            return ItemStack.isSameItemSameComponents(getForReading(), other.getForReading());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getForReading().hashCode();
    }

    @Override
    public String toString() {
        return "ItemStackBox{" +
                "stack=" + getForReading() +
                '}';
    }
}
