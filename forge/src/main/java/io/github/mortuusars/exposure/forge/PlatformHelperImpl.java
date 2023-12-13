package io.github.mortuusars.exposure.forge;

import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.network.NetworkHooks;

public class PlatformHelperImpl {
    public static boolean canShear(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SHEARS_CARVE);
    }

    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, BlockPos pos) {
        NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
    }

    public static void openLightroomMenu(ServerPlayer serverPlayer, LightroomBlockEntity lightroomBlockEntity, BlockPos pos) {
        NetworkHooks.openScreen(serverPlayer, lightroomBlockEntity, pos);
    }
}
