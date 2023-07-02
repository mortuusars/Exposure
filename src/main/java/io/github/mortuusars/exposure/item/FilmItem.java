package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.GUI;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilmItem extends Item {
    private static final String FRAMES_TAG = "Frames";

    private final FilmType filmType;
    private final int frameSize;
    private final int frameCount;

    public FilmItem (FilmType filmType, int frameSize, int frameCount, Properties properties) {
        super(properties);
        this.filmType = filmType;
        this.frameSize = frameSize;
        this.frameCount = frameCount;
    }

    public FilmType getType() {
        return filmType;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public List<ExposureFrame> getFrames(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        List<ExposureFrame> frames = new ArrayList<>();

        for (Tag frameTag : filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND)) {
            frames.add(new ExposureFrame((CompoundTag) frameTag));
        }

        return frames;
    }

    public void addFrame(ItemStack filmStack, ExposureFrame frame) {
        CompoundTag tag = filmStack.getOrCreateTag();

        if (!tag.contains(FRAMES_TAG, Tag.TAG_LIST)) {
            tag.put(FRAMES_TAG, new ListTag());
        }

        ListTag listTag = tag.getList(FRAMES_TAG, Tag.TAG_COMPOUND);

        if (listTag.size() >= getFrameCount())
            throw new IllegalStateException("Cannot add more frames than film could have. Size: " + listTag.size());

        listTag.add(frame.save(new CompoundTag()));
        tag.put(FRAMES_TAG, listTag);
    }

    public boolean canAddFrame(ItemStack filmStack) {
        if (!filmStack.hasTag() || !filmStack.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            return true;

        return filmStack.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND).size() < getFrameCount();
    }


//    public ItemStack setFrame(ItemStack film, int slot, ExposureFrame frame) {
//        Preconditions.checkArgument(film.getItem() instanceof FilmItem,  film + " is not a FilmItem!");
//        Preconditions.checkArgument(slot >= 0 && slot < getFrameCount(), slot + " is out of range. Frames: " + getFrameCount());
//
//        ListTag frames = getFramesTag(film);
//        if (!frames.setTag(slot, frame.save(new CompoundTag())))
//            throw new IllegalStateException("ExposureFrame was not saved to film.");
//
//        return film;
//    }
//
//    public int getEmptyFrame(ItemStack film) {
//        ListTag frames = getFramesTag(film);
//
//        for (int frame = 0; frame < frames.size(); frame++) {
//            if (frames.getCompound(frame).isEmpty())
//                return frame;
//        }
//
//        return -1;
//    }
//
//    public List<ExposureFrame> getFrames(ItemStack film) {
//        List<ExposureFrame> frames = new ArrayList<>();
//
//        for (Tag frameTag : getFramesTag(film)) {
//            frames.add(new ExposureFrame(((CompoundTag) frameTag)));
//        }
//
//        return frames;
//    }
//
//    protected ListTag getFramesTag(ItemStack film) {
//        Preconditions.checkArgument(film.getItem() instanceof FilmItem, film + " is not a FilmItem.");
//
//        CompoundTag tag = film.getOrCreateTag();
//        if (!tag.contains(FRAMES_TAG, Tag.TAG_LIST))
//            createEmptyFrames(film);
//
//        return film.getOrCreateTag().getList(FRAMES_TAG, Tag.TAG_COMPOUND);
//    }
//
//    @SuppressWarnings("UnusedReturnValue")
//    protected ItemStack createEmptyFrames(ItemStack film) {
//        ListTag framesTag = new ListTag();
//
//        for (int frame = 0; frame < getFrameCount(); frame++) {
//            framesTag.add(frame, new CompoundTag());
//        }
//
//        film.getOrCreateTag().put(FRAMES_TAG, framesTag);
//        return film;
//    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack film = player.getItemInHand(hand);

        if (level.isClientSide)
            GUI.showExposureViewScreen(film);

        return InteractionResultHolder.success(film);
    }
}
