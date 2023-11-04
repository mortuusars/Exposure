package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public interface IFilmItem {
    FilmType getType();
    default int getDefaultMaxFrameCount(ItemStack filmStack) {
        return 16;
    }

    default int getMaxFrameCount(ItemStack filmStack) {
        if (filmStack.getTag() != null && filmStack.getOrCreateTag().contains("FrameCount", Tag.TAG_INT))
            return filmStack.getOrCreateTag().getInt("FrameCount");
        else
            return getDefaultMaxFrameCount(filmStack);
    }

    default boolean hasExposedFrame(ItemStack filmStack, int index) {
        if (index < 0 || !filmStack.hasTag() || !filmStack.getOrCreateTag().contains("Frames", Tag.TAG_LIST))
            return false;

        ListTag list = filmStack.getOrCreateTag().getList("Frames", Tag.TAG_COMPOUND);
        return index < list.size();
    }

    default int getExposedFramesCount(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains("Frames", Tag.TAG_LIST) ?
                stack.getOrCreateTag().getList("Frames", Tag.TAG_COMPOUND).size() : 0;
    }

    default ListTag getExposedFrames(ItemStack filmStack) {
        return filmStack.getTag() != null ? filmStack.getTag().getList("Frames", Tag.TAG_COMPOUND) : new ListTag();
    }
}
