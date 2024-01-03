package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumItem extends Item {
    public static final String PAGES_TAG = "Pages";

    public AlbumItem(Properties properties) {
        super(properties);
    }

    public List<AlbumPage> getPages(ItemStack albumStack) {
        CompoundTag tag = albumStack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(PAGES_TAG, Tag.TAG_LIST))
            return Collections.emptyList();

        ListTag pagesList = tag.getList(PAGES_TAG, Tag.TAG_COMPOUND);
        if (pagesList.isEmpty())
            return Collections.emptyList();

        List<AlbumPage> pages = new ArrayList<>();

        for (int i = 0; i < pagesList.size(); i++) {
            pages.add(AlbumPage.fromTag(pagesList.getCompound(i)));
        }

        return pages;
    }

    public void addPage(ItemStack albumStack, AlbumPage page) {
        ListTag pages = getOrCreatePagesTag(albumStack);
        pages.add(page.toTag(new CompoundTag()));
    }

    public void addPage(ItemStack albumStack, AlbumPage page, int index) {
        ListTag pages = getOrCreatePagesTag(albumStack);
        pages.add(index, page.toTag(new CompoundTag()));
    }

    protected ListTag getOrCreatePagesTag(ItemStack albumStack) {
        CompoundTag tag = albumStack.getOrCreateTag();
        ListTag list = tag.getList(PAGES_TAG, Tag.TAG_COMPOUND);
        tag.put(PAGES_TAG, list);
        return list;
    }

    public ItemStack setPhotoOnPage(ItemStack albumStack, ItemStack photoStack, int pageIndex) {
        CompoundTag tag = albumStack.getOrCreateTag();
        ListTag list = tag.getList(PAGES_TAG, Tag.TAG_COMPOUND);

        ItemStack existingStack = ItemStack.EMPTY;

        AlbumPage page;

        if (pageIndex < list.size()) {
            page = AlbumPage.fromTag(list.getCompound(pageIndex));
            existingStack = page.setPhotographStack(photoStack);
        } else {
            page = new AlbumPage(photoStack, Collections.emptyList());
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        List<AlbumPage> albumPages = getPages(stack);
        int photosCount = 0;
        for (AlbumPage albumPage : albumPages) {
            if (!albumPage.getPhotographStack().isEmpty())
                photosCount++;
        }

        if (photosCount > 0)
            tooltipComponents.add(Component.translatable("item.exposure.album.tooltip.photos_count", photosCount));
    }
}
