package io.github.mortuusars.exposure.item;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AlbumPage {
    public static final String PHOTOGRAPH_TAG = "Photo";
    public static final String NOTE_TAG = "Note";
    public static final String NOTE_COMPONENT_TAG = "NoteComponent";
    private ItemStack photographStack;
    private Either<String, Component> note;

    public AlbumPage(ItemStack photographStack, Either<String, Component> note) {
        this.photographStack = photographStack;
        this.note = note;
    }

    public static AlbumPage editable(ItemStack photographStack, String note) {
        return new AlbumPage(photographStack, Either.left(note));
    }

    public static AlbumPage signed(ItemStack photographStack, Component note) {
        return new AlbumPage(photographStack, Either.right(note));
    }

    public boolean isEditable() {
        return note.left().isPresent();
    }

    public static AlbumPage fromTag(CompoundTag tag, boolean editable) {
        ItemStack photographStack = tag.contains(PHOTOGRAPH_TAG, Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound(PHOTOGRAPH_TAG)) : ItemStack.EMPTY;

        if (editable) {
            String note;
            if (tag.contains(NOTE_TAG, Tag.TAG_STRING))
                note = tag.getString(NOTE_TAG);
            else if (tag.contains(NOTE_COMPONENT_TAG)) {
                @Nullable MutableComponent component = Component.Serializer.fromJson(tag.getString(NOTE_COMPONENT_TAG));
                note = component != null ? component.getString(512) : "";
            } else
                note = "";

            return editable(photographStack, note);
        } else {
            Component note;
            if (tag.contains(NOTE_COMPONENT_TAG, Tag.TAG_STRING))
                note = Component.Serializer.fromJson(tag.getString(NOTE_COMPONENT_TAG));
            else if (tag.contains(NOTE_TAG))
                note = Component.literal(tag.getString(NOTE_TAG));
            else
                note = Component.empty();

            return signed(photographStack, note);
        }
    }

    public CompoundTag toTag(CompoundTag tag) {
        if (!photographStack.isEmpty())
            tag.put(PHOTOGRAPH_TAG, photographStack.save(new CompoundTag()));

        note.ifLeft(string -> { if (!string.isEmpty()) tag.putString(NOTE_TAG, string);})
            .ifRight(component -> tag.putString(NOTE_COMPONENT_TAG, Component.Serializer.toJson(component)));

        return tag;
    }

    public ItemStack getPhotographStack() {
        return photographStack;
    }

    public ItemStack setPhotographStack(ItemStack photographStack) {
        ItemStack existingStack = this.photographStack;
        this.photographStack = photographStack;
        return existingStack;
    }

    public Either<String, Component> getNote() {
        return note;
    }

    public void setNote(Either<String, Component> note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AlbumPage) obj;
        return Objects.equals(this.photographStack, that.photographStack) &&
                Objects.equals(this.note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(photographStack, note);
    }

    @Override
    public String toString() {
        return "Page[" +
                "photo=" + photographStack + ", " +
                "note=" + note + ']';
    }
}
