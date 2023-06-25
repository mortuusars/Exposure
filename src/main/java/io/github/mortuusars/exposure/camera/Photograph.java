package io.github.mortuusars.exposure.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.camera.film.FilmSize;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class Photograph {
    private String id;
    private int size;
    private String title;
    private String description;

    public static final Photograph EMPTY = new Photograph("", 1, "", "");

    public Photograph(@NotNull String id, int size, @NotNull String title, @NotNull String description) {
        this.id = id;
        this.size = size;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void save(CompoundTag tag) {
        tag.putString("Id", id);
        tag.putInt("Size", size);
        tag.putString("Title", title);
        tag.putString("Description", description);
    }

    public static Photograph load(CompoundTag tag) {
        String tagId = tag.getString("Id");
        Preconditions.checkState(tagId.length() > 0, "Id cannot be empty.");
        int tagSize = tag.getInt("Size");
        Preconditions.checkState(tagSize > 0, "Size cannot be less or equal to zero.");
        String tagTitle = tag.getString("Title");
        String tagDescription = tag.getString("Description");

        return new Photograph(tagId, tagSize, tagTitle, tagDescription);
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(size);
        buffer.writeUtf(title);
        buffer.writeUtf(description);
    }

    public static Photograph fromBuffer(FriendlyByteBuf buffer) {
        return new Photograph(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readUtf()
        );
    }
}
