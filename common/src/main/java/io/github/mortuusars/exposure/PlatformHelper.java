package io.github.mortuusars.exposure;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;

public class PlatformHelper {
    @ExpectPlatform
    public static boolean canShear(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void openLightroomMenu(ServerPlayer serverPlayer, LightroomBlockEntity lightroomBlockEntity, BlockPos pos) {
        throw new AssertionError();
    }
}
