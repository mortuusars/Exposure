package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ShowExposureS2CP(String path, boolean isTexture, boolean negative) implements IPacket<ShowExposureS2CP> {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(path);
        buffer.writeBoolean(isTexture);
        buffer.writeBoolean(negative);
    }

    public static ShowExposureS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new ShowExposureS2CP(buffer.readUtf(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.showExposure(this);
        return true;
    }
}
