package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.inventory.LecternAlbumMenu;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import io.github.mortuusars.mortaar.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlock.class)
public abstract class LecternMixin {
    @Inject(method = "openScreen", at = @At(value = "HEAD"), cancellable = true)
    private void openScreen(Level level, BlockPos pos, Player player, CallbackInfo ci) {
        if (level.getBlockEntity(pos) instanceof LecternBlockEntity lecternBlockEntity
                && player instanceof ServerPlayer serverPlayer
                && (lecternBlockEntity.getBook().getItem() instanceof AlbumItem || lecternBlockEntity.getBook().getItem() instanceof SignedAlbumItem)) {
            exposure$open(serverPlayer, lecternBlockEntity);
            player.awardStat(Stats.INTERACT_WITH_LECTERN);
            ci.cancel();
        }
    }

    @Unique
    private void exposure$open(ServerPlayer player, LecternBlockEntity lecternBlockEntity) {
        ItemStack book = lecternBlockEntity.getBook();

        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return book.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                LecternBlockEntityAccessor accessor = (LecternBlockEntityAccessor) lecternBlockEntity;
                return new LecternAlbumMenu(containerId, playerInventory, accessor.getBookAccess(), accessor.getDataAccess());
            }
        };

        Platform.openMenu(player, menuProvider, buffer -> {
            ItemStack.STREAM_CODEC.encode(buffer, book);
        });
    }
}
