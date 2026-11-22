package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.*;

/**
 * ItemStacks gotten from this class should NOT be modified under any circumstances.
 * Copy ItemStack and set with {@link Attachment#set(ItemStack, ItemStack)}.
 */
public record Attachment<T extends Item>(Identifier id,
                                         DataComponentType<StoredItemStack> component,
                                         Predicate<ItemStack> itemPredicate,
                                         Class<T> itemType,
                                         Supplier<Integer> maxCount,
                                         Optional<SoundEffect> insertedSound,
                                         Optional<SoundEffect> removedSound) {
    public static final Attachment<FilmRollItem> FILM = new Attachment<>(
            Exposure.resource("film"),
            Exposure.DataComponents.FILM,
            stack -> stack.getItem() instanceof FilmRollItem,
            FilmRollItem.class,
            () -> 1,
            new SoundEffect(Exposure.SoundEvents.FILM_ADVANCE, 0.9F, 1F),
            new SoundEffect(Exposure.SoundEvents.FILM_REMOVED, 0.7F, 1F));
    public static final Attachment<Item> FLASH = new Attachment<>(
            Exposure.resource("flash"),
            Exposure.DataComponents.FLASH,
            stack -> stack.is(Exposure.Tags.Items.FLASHES),
            Item.class,
            () -> 1,
            new SoundEffect(Exposure.SoundEvents.CAMERA_GENERIC_CLICK, 0.6F, 1.15F),
            new SoundEffect(Exposure.SoundEvents.CAMERA_GENERIC_CLICK, 0.35F, 0.95F));
    public static final Attachment<Item> LENS = new Attachment<>(
            Exposure.resource("lens"),
            Exposure.DataComponents.LENS,
            stack -> stack.is(Exposure.Tags.Items.LENSES),
            Item.class,
            () -> 1,
            new SoundEffect(Exposure.SoundEvents.LENS_INSERT),
            new SoundEffect(Exposure.SoundEvents.LENS_REMOVE));
    public static final Attachment<Item> FILTER = new Attachment<>(
            Exposure.resource("filter"),
            Exposure.DataComponents.FILTER,
            stack -> stack.is(Exposure.Tags.Items.FILTERS),
            Item.class,
            () -> 1,
            new SoundEffect(Exposure.SoundEvents.FILTER_INSERT),
            new SoundEffect(Exposure.SoundEvents.FILTER_REMOVE, 0.5F));

    public Attachment(Identifier id,
                      DataComponentType<StoredItemStack> component,
                      Predicate<ItemStack> itemPredicate,
                      Class<T> itemType,
                      Supplier<Integer> maxCount,
                      SoundEffect insertedSound,
                      SoundEffect removedSound) {
        this(id, component, itemPredicate, itemType, maxCount, Optional.of(insertedSound), Optional.of(removedSound));
    }

    public Attachment(Identifier id,
                      DataComponentType<StoredItemStack> component,
                      Predicate<ItemStack> itemPredicate,
                      Class<T> itemType,
                      Supplier<Integer> maxCount) {
        this(id, component, itemPredicate, itemType, maxCount, Optional.empty(), Optional.empty());
    }

    public boolean matches(ItemStack stack) {
        return itemPredicate.test(stack);
    }

    public boolean isEmpty(ItemStack stack) {
        StoredItemStack storedItemStack = get(stack);
        return storedItemStack.isEmpty() || !itemType.isInstance(storedItemStack.getItem());
    }

    public boolean isPresent(ItemStack stack) {
        return !isEmpty(stack);
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public StoredItemStack get(ItemStack stack) {
        return stack.getOrDefault(component, StoredItemStack.EMPTY);
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public Attachment<T> ifPresent(ItemStack stack, BiConsumer<T, ItemStack> ifPresent) {
        StoredItemStack storedItemStack = get(stack);
        if (itemType.isInstance(storedItemStack.getItem())) {
            ifPresent.accept(itemType.cast(storedItemStack.getItem()), storedItemStack.getForReading());
        }
        return this;
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public Attachment<T> ifPresent(ItemStack stack, Consumer<ItemStack> ifPresent) {
        StoredItemStack storedItemStack = get(stack);
        if (itemType.isInstance(storedItemStack.getItem())) {
            ifPresent.accept(storedItemStack.getForReading());
        }
        return this;
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public Attachment<T> orElse(ItemStack stack, Runnable orElse) {
        StoredItemStack storedItemStack = get(stack);
        if (!itemType.isInstance(storedItemStack.getItem())) {
            orElse.run();
        }
        return this;
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public Attachment<T> ifPresentOrElse(ItemStack stack, BiConsumer<T, ItemStack> ifPresent, Runnable orElse) {
        return ifPresent(stack, ifPresent).orElse(stack, orElse);
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public <R> Optional<R> map(ItemStack stack, Function<ItemStack, R> mappingFunc) {
        return mapOrElse(stack, (st) -> Optional.of(mappingFunc.apply(st)), Optional::empty);
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public <R> Optional<R> map(ItemStack stack, BiFunction<T, ItemStack, R> mappingFunc) {
        return mapOrElse(stack, (it, st) -> Optional.of(mappingFunc.apply(it, st)), Optional::empty);
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public <R> R mapOrElse(ItemStack stack, Function<ItemStack, R> ifPresentMappingFunc, Supplier<R> orElseSupplier) {
        StoredItemStack storedItemStack = get(stack);
        return itemType.isInstance(storedItemStack.getItem())
                ? ifPresentMappingFunc.apply(storedItemStack.getForReading())
                : orElseSupplier.get();
    }

    /**
     * ItemStacks gotten from this method should NOT be modified under any circumstances.
     */
    public <R> R mapOrElse(ItemStack stack, BiFunction<T, ItemStack, R> ifPresentMappingFunc, Supplier<R> orElseSupplier) {
        StoredItemStack storedItemStack = get(stack);
        return itemType.isInstance(storedItemStack.getItem())
                ? ifPresentMappingFunc.apply(itemType.cast(storedItemStack.getItem()), storedItemStack.getForReading())
                : orElseSupplier.get();
    }

    public Attachment<T> set(ItemStack stack, ItemStack attachment) {
        if (attachment.isEmpty()) {
            stack.remove(component);
        } else {
            stack.set(component, new StoredItemStack(attachment));
        }
        return this;
    }

    public void playInsertSoundSided(Entity entity) {
        insertedSound().ifPresent(sound ->
                Sound.playUniqueSided(Integer.toString(entity.getId()), entity, sound, SoundSource.PLAYERS));
    }

    public void playRemoveSoundSided(Entity entity) {
        removedSound().ifPresent(sound ->
                Sound.playUniqueSided(Integer.toString(entity.getId()), entity, sound, SoundSource.PLAYERS));
    }

    @Override
    public String toString() {
        return "Attachment{id='" + id + "'}";
    }
}
