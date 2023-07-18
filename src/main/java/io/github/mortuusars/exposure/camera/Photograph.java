package io.github.mortuusars.exposure.camera;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class Photograph {
    private final Either<String, ResourceLocation> idOrResource;
    private final List<Component> note;

    public static final Photograph EMPTY = new Photograph("", Collections.emptyList());

    public Photograph(@NotNull String id) {
        this(Either.left(id), Collections.emptyList());
    }

    public Photograph(@NotNull String id, List<Component> note) {
        this(Either.left(id), note);
    }

    public Photograph(@NotNull ResourceLocation resource, List<Component> note) {
        this(Either.right(resource), note);
    }

    public Photograph(Either<String, ResourceLocation> idOrResource, List<Component> note) {
        this.idOrResource = idOrResource;
        this.note = note;
    }

    public Either<String, ResourceLocation> getIdOrResource() {
        return idOrResource;
    }

    public List<Component> getNote() {
        return note;
    }

    public void save(CompoundTag tag) {
        if (idOrResource.left().isPresent())
            tag.putString("Id", idOrResource.left().get());
        else if (idOrResource.right().isPresent())
            tag.putString("Resource", idOrResource.right().get().toString());
        else {
            Exposure.LOGGER.error("Photograph '" + this + "' cannot be saved to tag:");
            return;
        }

        if (note.size() > 0) {
            ListTag noteTagList = new ListTag();
            for (Component component : note) {
                noteTagList.add(StringTag.valueOf(Component.Serializer.toJson(component)));
            }
            tag.put("Note", noteTagList);
        }
    }

    public static Photograph load(CompoundTag tag) {
        Either<String, ResourceLocation> idOrLocation;
        String tagId = tag.getString("Id");
        if (tagId.length() > 0)
            idOrLocation = Either.left(tagId);
        else {
            String resourceString = tag.getString("Resource");
            if (resourceString.length() > 0)
                idOrLocation = Either.right(new ResourceLocation(resourceString));
            else {
                Exposure.LOGGER.error("Photograph data was not loaded: either string 'Id' or location 'Resource' must be provided. Tag: " + tag);
                return Photograph.EMPTY;
            }
        }

        List<Component> note = Collections.emptyList();

        if (tag.contains("Note", Tag.TAG_LIST)) {
            ListTag noteTagList = tag.getList("Note", Tag.TAG_STRING);
            if (noteTagList.size() > 0) {
                note = new ArrayList<>();
                for (Tag noteLine : noteTagList) {
                    note.add(Component.Serializer.fromJson(noteLine.getAsString()));
                }
            }
        }

        return new Photograph(idOrLocation, note);
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        CompoundTag tag = new CompoundTag();
        this.save(tag);
        buffer.writeNbt(tag);
    }

    public static Photograph fromBuffer(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readAnySizeNbt();
        if (tag == null) {
            Exposure.LOGGER.error("Photograph was not loaded from buffer. Null was returned.");
            return EMPTY;
        }

        return load(tag);
    }

    @Override
    public String toString() {
        return "Photograph{" +
                "idOrResource=" + idOrResource +
                ", note='" + note + '\'' +
                '}';
    }
}
