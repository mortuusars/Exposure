package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.data.ColorPalettes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public interface FilmItem {
    ExposureType getType();

    // -- Frames

    default int getDefaultMaxFrameCount(ItemStack stack) {
        return 16;
    }

    default int getDefaultFrameSize(ItemStack stack) {
        return Config.Server.DEFAULT_FRAME_SIZE.get();
    }

    default int getMaxFrameCount(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.FILM_FRAME_COUNT, getDefaultMaxFrameCount(stack));
    }

    default int getFrameSize(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.FILM_FRAME_SIZE, getDefaultFrameSize(stack));
    }

    default List<Frame> getStoredFrames(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.FILM_FRAMES, Collections.emptyList());
    }

    default int getStoredFramesCount(ItemStack stack) {
        return getStoredFrames(stack).size();
    }

    default boolean hasFrames(ItemStack stack) {
        return !getStoredFrames(stack).isEmpty();
    }

    default boolean hasFrameAt(ItemStack stack, int index) {
        return getStoredFrames(stack).size() > index;
    }

    default float getFullness(ItemStack stack) {
        return (float) getStoredFramesCount(stack) / getMaxFrameCount(stack);
    }

    default boolean isFull(ItemStack stack) {
        return getStoredFramesCount(stack) == getMaxFrameCount(stack);
    }
}
