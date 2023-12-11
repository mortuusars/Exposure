package io.github.mortuusars.exposure.block.forge;

import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class LightroomBlockImpl {
    public static void openMenu(ServerPlayer serverPlayer, LightroomBlockEntity lightroomBlockEntity, @NotNull BlockPos pos) {
        NetworkHooks.openScreen(serverPlayer, lightroomBlockEntity, pos);
    }
}
