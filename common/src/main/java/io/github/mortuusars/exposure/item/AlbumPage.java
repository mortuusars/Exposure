package io.github.mortuusars.exposure.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumPage {
    public static final String PHOTOGRAPH_TAG = "Photo";
    public static final String NOTE_TAG = "Note";
    private ItemStack photographStack;
    private List<Component> note;

    public AlbumPage(ItemStack photo, List<Component> note) {
        this.photographStack = photo;
        this.note = note;
    }

    public static AlbumPage fromTag(CompoundTag tag) {
        ItemStack photo = tag.contains(PHOTOGRAPH_TAG, Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound(PHOTOGRAPH_TAG)) : ItemStack.EMPTY;
        ListTag noteList = tag.getList(NOTE_TAG, Tag.TAG_COMPOUND);

        List<Component> note = new ArrayList<>();

        for (int j = 0; j < noteList.size(); j++) {
            String noteString = noteList.getString(j);
            note.add(j, noteString.isEmpty() ? Component.empty() : Component.Serializer.fromJson(noteString));
        }

        return new AlbumPage(photo, note);
    }

    public CompoundTag toTag(CompoundTag tag) {
        if (!photographStack.isEmpty())
            tag.put(PHOTOGRAPH_TAG, photographStack.save(new CompoundTag()));

        if (note.size() > 0) {
            ListTag noteList = new ListTag();
            for (Component component : note) {
                noteList.add(StringTag.valueOf(Component.Serializer.toJson(component)));
            }
            tag.put(NOTE_TAG, noteList);
        }

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

    public List<Component> getNote() {
        return note;
    }

    public void setNote(List<Component> note) {
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
