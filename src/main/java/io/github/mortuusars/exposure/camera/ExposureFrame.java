package io.github.mortuusars.exposure.camera;

import net.minecraft.nbt.CompoundTag;

public class ExposureFrame {
    public String id;

    public ExposureFrame(String id) {
        this.id = id;
    }

    public ExposureFrame(CompoundTag tag) {
        this(tag.getString("id"));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("id", id);
        return tag;
    }
}
