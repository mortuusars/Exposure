package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AlbumItem extends Item {
    public static final class Page {
        public static final String PHOTOGRAPH_TAG = "Photo";
        public static final String NOTE_TAG = "Note";
        private ItemStack photographStack;
        private List<Component> note;

        public Page(ItemStack photo, List<Component> note) {
            this.photographStack = photo;
            this.note = note;
        }

        public static Page fromTag(CompoundTag tag) {
            ItemStack photo = tag.contains(PHOTOGRAPH_TAG, Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound(PHOTOGRAPH_TAG)) : ItemStack.EMPTY;
            ListTag noteList = tag.getList(NOTE_TAG, Tag.TAG_COMPOUND);

            List<Component> note = new ArrayList<>();

            for (int j = 0; j < noteList.size(); j++) {
                String noteString = noteList.getString(j);
                note.add(j, noteString.isEmpty() ? Component.empty() : Component.Serializer.fromJson(noteString));
            }

            return new Page(photo, note);
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
            var that = (Page) obj;
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

    public static final String PAGES_TAG = "Pages";

    public AlbumItem(Properties properties) {
        super(properties);
    }

    public List<Page> getPages(ItemStack albumStack) {
        CompoundTag tag = albumStack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(PAGES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        ListTag pagesList = tag.getList(PAGES_TAG, Tag.TAG_COMPOUND);
        if (pagesList.isEmpty())
            return Collections.emptyList();

        List<Page> pages = new ArrayList<>();

        for (int i = 0; i < pagesList.size(); i++) {
            pages.add(Page.fromTag(pagesList.getCompound(i)));
        }

        return pages;
    }

    public ItemStack setPhotoOnPage(ItemStack albumStack, ItemStack photoStack, int pageIndex) {
        CompoundTag tag = albumStack.getOrCreateTag();
        ListTag list = tag.getList(PAGES_TAG, Tag.TAG_COMPOUND);

        ItemStack existingStack = ItemStack.EMPTY;

        Page page;

        if (pageIndex < list.size()) {
            page = Page.fromTag(list.getCompound(pageIndex));
            existingStack = page.setPhotographStack(photoStack);
        } else {
            page = new Page(photoStack, Collections.emptyList());
            while (list.size() <= pageIndex) {
                list.add(new CompoundTag());
            }
        }

        list.set(pageIndex, page.toTag(new CompoundTag()));
        albumStack.getOrCreateTag().put(PAGES_TAG, list);

        return existingStack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (player instanceof ServerPlayer serverPlayer)
            openMenu(serverPlayer, itemStack);

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    protected void openMenu(ServerPlayer player, ItemStack albumStack) {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return albumStack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new AlbumMenu(containerId, playerInventory, new ItemAndStack<>(albumStack));
            }
        };

        PlatformHelper.openMenu(player, menuProvider, buffer -> buffer.writeItem(albumStack));
    }
}
