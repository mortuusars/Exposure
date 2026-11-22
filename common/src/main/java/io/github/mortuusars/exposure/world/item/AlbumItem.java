package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.world.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumPage;
import io.github.mortuusars.exposure.world.inventory.AlbumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class AlbumItem extends Item {
    public AlbumItem(Properties properties) {
        super(properties);
    }

    public AlbumContent getContent(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.ALBUM_CONTENT, AlbumContent.EMPTY);
    }

    public void setContent(ItemStack stack, AlbumContent content) {
        stack.set(Exposure.DataComponents.ALBUM_CONTENT, content);
    }

    public void updatePage(ItemStack stack, int index, Function<Optional<AlbumPage>, AlbumPage> pageUpdater) {
        AlbumPage page = pageUpdater.apply(getContent(stack).getPage(index));
        setContent(stack, getContent(stack).toMutable().setPage(index, page).toImmutable());
    }

    public int getPhotographsCount(ItemStack stack) {
        return getContent(stack).pages().stream().filter(albumPage -> !albumPage.photograph().isEmpty()).toList().size();
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (player instanceof ServerPlayer serverPlayer) {
            int albumSlot = usedHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().getSelectedSlot();
            open(serverPlayer, itemStack, albumSlot);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(Blocks.LECTERN))
            return LecternBlock.tryPlaceBook(context.getPlayer(), level, blockPos, blockState,
                    context.getItemInHand()) ? (level.isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS) : InteractionResult.PASS; // this sucks
        return InteractionResult.PASS;
    }

    public void open(ServerPlayer player, ItemStack albumStack, int albumSlot) {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return albumStack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new AlbumMenu(containerId, playerInventory, albumSlot);
            }
        };

        PlatformHelper.openMenu(player, menuProvider, buffer -> {
            buffer.writeVarInt(albumSlot);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag flag) {
        if (Config.Client.ALBUM_PHOTOS_COUNT_TOOLTIP.get()) {
            int photographsCount = getPhotographsCount(stack);
            if (photographsCount > 0)
                tooltipComponents.accept(Component.translatable("item.exposure.album.tooltip.photos_count", photographsCount));
        }
    }

    public boolean shouldPlayEquipAnimation(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    public ItemStack sign(ItemStack albumStack, String title, String author) {
        ItemStack signedAlbumStack = new ItemStack(Exposure.Items.SIGNED_ALBUM.get());

        List<SignedAlbumPage> signedPages = getContent(albumStack).removeTrailingPages().pages()
                .stream()
                .map(AlbumPage::convertToSigned)
                .toList();

        SignedAlbumContent signedAlbumContent = new SignedAlbumContent(title, author, signedPages);
        signedAlbumStack.set(Exposure.DataComponents.SIGNED_ALBUM_CONTENT, signedAlbumContent);

        return signedAlbumStack;
    }
}
