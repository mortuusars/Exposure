package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilmRollItem extends Item implements IFilmItem {
    public static final String FRAMES_TAG = "Frames";
    public static final String FRAME_COUNT_TAG = "FrameCount";
    public static final String FRAME_SIZE_TAG = "FrameSize";

    private final FilmType filmType;
    private final int defaultFrameSize;
    private final int defaultFrameCount;
    private final int barColor;

    public FilmRollItem(FilmType filmType, int defaultFrameCount, int defaultFrameSize, int barColor, Properties properties) {
        super(properties);
        this.filmType = filmType;
        this.defaultFrameCount = defaultFrameCount;
        this.defaultFrameSize = defaultFrameSize;
        this.barColor = barColor;
    }

    public FilmType getType() {
        return filmType;
    }

    public int getFrameCount(ItemStack filmStack) {
        if (filmStack.getTag() != null && filmStack.getOrCreateTag().contains(FRAME_COUNT_TAG, Tag.TAG_INT))
            return filmStack.getOrCreateTag().getInt(FRAME_COUNT_TAG);
        else
            return defaultFrameCount;
    }

    public int getFrameSize(ItemStack filmStack) {
        if (filmStack.getTag() != null && filmStack.getOrCreateTag().contains(FRAME_SIZE_TAG, Tag.TAG_INT))
            return filmStack.getOrCreateTag().getInt(FRAME_SIZE_TAG);
        else
            return defaultFrameSize;
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getExposedFramesCount(stack) > 0;
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + 12 * getExposedFramesCount(stack) / getFrameCount(stack), 13);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return barColor;
    }

    protected int getExposedFramesCount(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST) ?
                stack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND).size() : 0;
    }

    public List<ExposureFrame> getExposedFrames(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        List<ExposureFrame> frames = new ArrayList<>();

        for (Tag frameTag : filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND)) {
            frames.add(ExposureFrame.load((CompoundTag) frameTag));
        }

        return frames;
    }

    public void addFrame(ItemStack filmStack, ExposureFrame frame) {
        CompoundTag tag = filmStack.getOrCreateTag();

        if (!tag.contains(FRAMES_TAG, Tag.TAG_LIST)) {
            tag.put(FRAMES_TAG, new ListTag());
        }

        ListTag listTag = tag.getList(FRAMES_TAG, Tag.TAG_COMPOUND);

        if (listTag.size() >= getFrameCount(filmStack))
            throw new IllegalStateException("Cannot add more frames than film could fit. Size: " + listTag.size());

        listTag.add(frame.save(new CompoundTag()));
        tag.put(FRAMES_TAG, listTag);
    }

    public boolean canAddFrame(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return true;

        return filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND).size() < getFrameCount(filmStack);
    }

    public ItemAndStack<DevelopedFilmItem> develop(ItemStack filmStack) {
        DevelopedFilmItem developedItem = getType() == FilmType.COLOR ? Exposure.Items.DEVELOPED_COLOR_FILM.get()
                : Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get();

        ListTag framesTag = filmStack.getTag() != null ?
                filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND) : new ListTag();

        ItemStack developedItemStack = new ItemStack(developedItem);
        developedItemStack.getOrCreateTag().put(FRAMES_TAG, framesTag);
        return new ItemAndStack<>(developedItemStack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int exposedFrames = getExposedFramesCount(stack);
        if (exposedFrames > 0) {
            int totalFrames = getFrameCount(stack);
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.frame_count", exposedFrames, totalFrames)
                    .withStyle(ChatFormatting.GRAY));
        }

        int frameSize = getFrameSize(stack);
        if (frameSize != defaultFrameSize) {
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.frame_size",
                    Component.literal(String.format("%.1f", frameSize / 10f)))
                            .withStyle(ChatFormatting.GRAY));
        }
    }
}
