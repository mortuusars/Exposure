package io.github.mortuusars.exposure.fabric;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;

public class PlatformHelperImpl {
    public static boolean canShear(ItemStack stack) {
        return stack.getItem() instanceof ShearsItem;
    }

    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, BlockPos pos) {
        serverPlayer.openMenu(menuProvider);
    }

    public static void openLightroomMenu(ServerPlayer serverPlayer, LightroomBlockEntity lightroomBlockEntity, BlockPos pos) {
        LogUtils.getLogger().error("NOT IMPLEMENTED");
    }
}
