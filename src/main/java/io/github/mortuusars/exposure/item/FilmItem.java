package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FilmItem extends Item {
    private static final String FRAMES_TAG = "Frames";

    private final int frameSize;
    private final int frameCount;

    public FilmItem(int frameSize, int frameCount, Properties properties) {
        super(properties);
        this.frameSize = frameSize;
        this.frameCount = frameCount;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getFrameCount() {
        return frameCount;
    }

    protected ItemStack createEmptyFrames(ItemStack film) {
        ListTag framesTag = new ListTag();

        for (int frame = 0; frame < getFrameCount(); frame++) {
            framesTag.add(frame, new CompoundTag());
        }

        film.getOrCreateTag().put(FRAMES_TAG, framesTag);
        return film;
    }

    public ItemStack setFrame(ItemStack filmStack, int slot, ExposureFrame frame) {
        Preconditions.checkArgument(filmStack.getItem() instanceof FilmItem,  filmStack + " is not a FilmItem!");
        Preconditions.checkArgument(slot >= 0 && slot < getFrameCount(), slot + " is out of range. Frames: " + getFrameCount());
//        Preconditions.checkArgument(frame.width <= getFrameSize() && frame.height <= getFrameSize(),
//                 frame + " ExposureFrame size is larger than maximum frame size for this film. Frame size: " + getFrameSize());


        CompoundTag tag = filmStack.getOrCreateTag();

        if (!tag.contains(FRAMES_TAG, Tag.TAG_LIST))
            createEmptyFrames(filmStack);

        ListTag frames = tag.getList(FRAMES_TAG, Tag.TAG_COMPOUND);
        if (!frames.setTag(slot, frame.save(new CompoundTag())))
            throw new IllegalStateException("ExposureFrame was not saved to film.");

        return filmStack;
    }

    public int getEmptyFrame(ItemStack film) {
        if (!film.getOrCreateTag().contains(FRAMES_TAG, Tag.TAG_LIST))
            createEmptyFrames(film);

        CompoundTag tag = film.getOrCreateTag();
        ListTag frames = tag.getList(FRAMES_TAG, Tag.TAG_COMPOUND);

        for (int frame = 0; frame < frames.size(); frame++) {
            if (frames.getCompound(frame).isEmpty())
                return frame;
        }

        return -1;
    }

    public List<ExposureFrame> getFrames(ItemStack film) {
        List<ExposureFrame> frames = new ArrayList<>();

        CompoundTag tag = film.getOrCreateTag();
        if (tag.contains(FRAMES_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(FRAMES_TAG, Tag.TAG_COMPOUND);
            for (Tag frameTag : list) {
                frames.add(new ExposureFrame(((CompoundTag) frameTag)));
            }
        }

        return frames;
    }
}
