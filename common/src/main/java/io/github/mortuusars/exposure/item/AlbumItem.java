package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

public class AlbumItem extends Item {
    public record AlbumPage(ItemStack photo, Component[] note) {
        public static final String PHOTOGRAPH_TAG = "Photo";
        public static final String NOTE_TAG = "Note";
    }

    public static final String PAGES_TAG = "Pages";

    public AlbumItem(Properties properties) {
        super(properties);
    }

    public List<AlbumPage> getPages(ItemStack albumStack) {
        CompoundTag tag = albumStack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(PAGES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        ListTag pagesTag = tag.getList(PAGES_TAG, Tag.TAG_COMPOUND);
        if (pagesTag.isEmpty())
            return Collections.emptyList();

        List<AlbumPage> pages = new ArrayList<>();

        for (int i = 0; i < pagesTag.size(); i++) {
            CompoundTag page = pagesTag.getCompound(i);
            ItemStack photoStack = ItemStack.of(page.getCompound(AlbumPage.PHOTOGRAPH_TAG));

            ListTag noteList = page.getList(AlbumPage.NOTE_TAG, Tag.TAG_STRING);
            Component[] note = new Component[noteList.size()];

            for (int j = 0; j < noteList.size(); j++) {
                String noteString = noteList.getString(j);
                note[j] = noteString.isEmpty() ? Component.empty() : Component.Serializer.fromJson(noteString);
            }

            pages.add(new AlbumPage(photoStack, note));
        }

        return pages;
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
