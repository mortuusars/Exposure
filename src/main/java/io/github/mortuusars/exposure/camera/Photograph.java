package io.github.mortuusars.exposure.camera;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class Photograph {
    private final String id;
    private final int size;
    private final String note;

    public static final Photograph EMPTY = new Photograph("", 0, "");

    public Photograph(@NotNull String id, int size, @NotNull String note) {
        this.id = id;
        this.size = size;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public String getNote() {
        return note;
    }

    public void save(CompoundTag tag) {
        tag.putString("Id", id);
        tag.putInt("Size", size);
        tag.putString("Note", note);
    }

    public static Photograph load(CompoundTag tag) {
        String tagId = tag.getString("Id");
        Preconditions.checkState(tagId.length() > 0, "Id cannot be empty.");
        int tagSize = tag.getInt("Size");
        Preconditions.checkState(tagSize > 0, "Size cannot be less or equal to zero.");
        String tagNote = tag.getString("Note");

        return new Photograph(tagId, tagSize, tagNote);
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(size);
        buffer.writeUtf(note);
    }

    public static Photograph fromBuffer(FriendlyByteBuf buffer) {
        return new Photograph(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readUtf()
        );
    }
}
