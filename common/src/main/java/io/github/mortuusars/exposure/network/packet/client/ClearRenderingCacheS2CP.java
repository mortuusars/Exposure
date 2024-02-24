package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ClearRenderingCacheS2CP implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("clear_rendering_cache");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static ClearRenderingCacheS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new ClearRenderingCacheS2CP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.clearRenderingCache();
        return true;
    }
}