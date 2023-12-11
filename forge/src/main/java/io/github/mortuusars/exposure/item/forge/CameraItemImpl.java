package io.github.mortuusars.exposure.item.forge;

import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class CameraItemImpl {
    public static void openMenu(ServerPlayer serverPlayer, ItemStack cameraStack) {
        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return cameraStack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new CameraAttachmentsMenu(containerId, playerInventory, cameraStack);
            }
        }, buffer -> buffer.writeItem(cameraStack));
    }
}
