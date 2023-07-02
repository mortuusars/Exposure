package io.github.mortuusars.exposure.item.attachment;

import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class CameraAttachments {
    public static final String FILM = "Film";
    public static final String LENS = "Lens";
    public static final String FILTER = "Filter";
    public static final String FLASH = "Flash";

    private final ItemStack cameraStack;

    public CameraAttachments(ItemStack cameraStack) {
        this.cameraStack = cameraStack;
    }

    public Optional<ItemAndStack<FilmItem>> getFilm() {
        ItemStack filmStack = getAttachment(FILM);
        if (filmStack.isEmpty())
            return Optional.empty();

        return Optional.of(new ItemAndStack<>(filmStack));
    }

    public void setFilm(ItemStack stack) {
        cameraStack.getOrCreateTag().put(FILM, stack.save(new CompoundTag()));
    }

    public ItemStack getLens() {
        return getAttachment(LENS);
    }

    public void setLens(ItemStack stack) {
        setAttachment(LENS, stack);
    }

    public ItemStack getAttachment(String type) {
        return cameraStack.getTag() != null && cameraStack.getTag()
                .contains(type, Tag.TAG_COMPOUND) ? ItemStack.of(cameraStack.getTag().getCompound(type)) :
                ItemStack.EMPTY;
    }

    public void setAttachment(String type, ItemStack stack) {
        cameraStack.getOrCreateTag().put(type, stack.save(new CompoundTag()));
    }
}
