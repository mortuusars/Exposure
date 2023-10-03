package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.camera.film.FrameData;
import io.github.mortuusars.exposure.camera.film.FilmType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface IFilmItem {
    FilmType getType();
    default int getDefaultFrameCount(ItemStack filmStack) {
        return 16;
    }

    default int getFrameCount(ItemStack filmStack) {
        if (filmStack.getTag() != null && filmStack.getOrCreateTag().contains("FrameCount", Tag.TAG_INT))
            return filmStack.getOrCreateTag().getInt("FrameCount");
        else
            return getDefaultFrameCount(filmStack);
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

    default List<FrameData> getExposedFrames(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains("Frames", Tag.TAG_LIST))
            return Collections.emptyList();

        List<FrameData> frames = new ArrayList<>();

        for (Tag frameTag : filmStack.getOrCreateTag().getList("Frames", Tag.TAG_COMPOUND)) {
            frames.add(FrameData.load((CompoundTag) frameTag));
        }

        return frames;
    }
}
