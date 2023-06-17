package io.github.mortuusars.exposure.camera;

import net.minecraft.nbt.CompoundTag;

public class ExposureFrame {
    public String id;
//    public int width;
//    public int height;

    public ExposureFrame(String id/*, int width, int height*/) {
        this.id = id;
//        this.width = width;
//        this.height = height;
    }

    public ExposureFrame(CompoundTag tag) {
        this(tag.getString("id")/*,
                tag.getInt("width"),
                tag.getInt("height")*/);
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("id", id);
//        tag.putInt("width", width);
//        tag.putInt("height", height);
        return tag;
    }

    public static ExposureFrame load(CompoundTag tag) {
        return new ExposureFrame(tag);
    }
}
