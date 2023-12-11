package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record LoadExposureCommandS2CP(String id, String path, int size, boolean dither) implements IPacket<LoadExposureCommandS2CP> {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeUtf(path);
        buffer.writeInt(size);
        buffer.writeBoolean(dither);
    }

    public static LoadExposureCommandS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new LoadExposureCommandS2CP(buffer.readUtf(), buffer.readUtf(), buffer.readInt(), buffer.readBoolean());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.loadExposure(id, path, size, dither);
        return true;
    }
}
