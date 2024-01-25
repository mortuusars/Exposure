package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AlbumItem extends Item {
    public static final String TAG_PAGES = "Pages";
    public static final String TAG_TITLE = "Title";
    public static final String TAG_AUTHOR = "Author";

    public AlbumItem(Properties properties) {
        super(properties);
    }

    public int getMaxPages() {
        return 16;
    }

    public boolean isEditable() {
        return true;
    }

    public Optional<AlbumPage> getPage(ItemStack albumStack, int index) {
        Preconditions.checkElementIndex(index, getMaxPages());
        CompoundTag tag = albumStack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(TAG_PAGES, Tag.TAG_LIST))
            return Optional.empty();

        ListTag pagesTag = getOrCreatePagesTag(albumStack);
        return pagesTag.size() - 1 >= index ?
                Optional.ofNullable(AlbumPage.fromTag(pagesTag.getCompound(index), isEditable())) : Optional.empty();
    }

    public void setPage(ItemStack albumStack, AlbumPage page, int index) {
        Preconditions.checkElementIndex(index, getMaxPages());
        ListTag pagesTag = getOrCreatePagesTag(albumStack);

        while (pagesTag.size() - 1 < index) {
            pagesTag.add(createEmptyPage().toTag(new CompoundTag()));
        }

        pagesTag.set(index, page.toTag(new CompoundTag()));
    }

    public AlbumPage createEmptyPage() {
        return new AlbumPage(ItemStack.EMPTY, isEditable() ? Either.left("") : Either.right(Component.empty()));
    }

    public List<AlbumPage> getPages(ItemStack albumStack) {
        CompoundTag tag = albumStack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(TAG_PAGES, Tag.TAG_LIST))
            return Collections.emptyList();

        ListTag pagesList = tag.getList(TAG_PAGES, Tag.TAG_COMPOUND);
        if (pagesList.isEmpty())
            return Collections.emptyList();

        List<AlbumPage> pages = new ArrayList<>();

        for (int i = 0; i < pagesList.size(); i++) {
            pages.add(AlbumPage.fromTag(pagesList.getCompound(i), isEditable()));
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
        ListTag list = tag.getList(TAG_PAGES, Tag.TAG_COMPOUND);
        tag.put(TAG_PAGES, list);
        return list;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (player instanceof ServerPlayer serverPlayer)
            open(serverPlayer, itemStack, isEditable());

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(Blocks.LECTERN))
            return LecternBlock.tryPlaceBook(context.getPlayer(), level, blockPos, blockState,
                    context.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        return InteractionResult.PASS;
    }

    public void open(ServerPlayer player, ItemStack albumStack, boolean editable) {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return albumStack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new AlbumMenu(containerId, playerInventory, new ItemAndStack<>(albumStack), editable);
            }
        };

        PlatformHelper.openMenu(player, menuProvider, buffer -> {
            buffer.writeItem(albumStack);
            buffer.writeBoolean(editable);
        });
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

    public ItemStack sign(ItemStack album, String title, String author) {
        if (!(album.getItem() instanceof AlbumItem))
            throw new IllegalArgumentException("Can only sign AlbumItem's. Provided: '" + album + "'.");
        else if (!isEditable())
            throw new IllegalArgumentException("Cannot sign fixed album.");

        ItemStack albumCopy = album.copy();
        ListTag pagesTag = getOrCreatePagesTag(albumCopy);

        for (int i = pagesTag.size() - 1; i >= 0; i--) {
            CompoundTag pageTag = pagesTag.getCompound(i);
            AlbumPage page = AlbumPage.fromTag(pageTag, isEditable());

            // Remove until we have page with content
            if (page.isEmpty())
                pagesTag.remove(i);
            else
                break;
        }

        for (int i = 0; i < pagesTag.size(); i++) {
            AlbumPage page = AlbumPage.fromTag(pagesTag.getCompound(i), isEditable());
            pagesTag.set(i, page.toSigned().toTag(new CompoundTag()));
        }

        ItemStack signedAlbum = new ItemStack(Exposure.Items.SIGNED_ALBUM.get());
        signedAlbum.setTag(albumCopy.getTag());
        signedAlbum.getOrCreateTag().putString(TAG_TITLE, title);
        signedAlbum.getOrCreateTag().putString(TAG_AUTHOR, author);
        return signedAlbum;
    }
}
