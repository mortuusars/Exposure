package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.menu.LecternAlbumMenu;
import io.github.mortuusars.exposure.util.ItemAndStack;
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
                && lecternBlockEntity.getBook().getItem() instanceof AlbumItem) {
            exposure$open(serverPlayer, lecternBlockEntity, lecternBlockEntity.getBook());
            player.awardStat(Stats.INTERACT_WITH_LECTERN);
            ci.cancel();
        }
    }

    @Unique
    private void exposure$open(ServerPlayer player, LecternBlockEntity lecternBlockEntity, ItemStack albumStack) {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return albumStack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                LecternBlockEntityAccessor accessor = (LecternBlockEntityAccessor) lecternBlockEntity;
                return new LecternAlbumMenu(containerId, lecternBlockEntity.getBlockPos(), playerInventory,
                        new ItemAndStack<>(albumStack), accessor.getBookAccess(), accessor.getDataAccess());
            }
        };

        PlatformHelper.openMenu(player, menuProvider, buffer -> {
            buffer.writeBlockPos(lecternBlockEntity.getBlockPos());
            buffer.writeItem(albumStack);
        });
    }
}
